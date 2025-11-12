package com.travelplanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.model.TravelPlan;
import com.travelplanner.service.LLMService;
import com.travelplanner.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin
public class PlanController {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成旅行计划
     * @param userRequest 用户的旅行需求
     * @return 生成的旅行计划
     */
    @PostMapping
    public Map<String, Object> generatePlan(@RequestBody String userRequest, HttpSession session) {
        // 使用大模型服务生成旅行计划
        String prompt = "请根据以下用户需求生成一个详细的旅行计划:\n" + userRequest + 
                       "\n请按照以下格式返回结果，只返回JSON格式数据，不要包含其他内容:" +
                       "\n1. 返回一个包含以下字段的JSON对象:" +
                       "\n   - destination: 目的地" +
                       "\n   - duration: 天数" +
                       "\n   - budget: 预算" +
                       "\n   - travelers: 旅客类型（如家庭、情侣、朋友等）" +
                       "\n   - summary: 旅行摘要" +
                       "\n   - itineraryItems: 行程项目数组，每个项目包含:" +
                       "\n     * day: 第几天" +
                       "\n     * time: 时间段" +
                       "\n     * activity: 活动内容" +
                       "\n     * location: 地点" +
                       "\n     * description: 详细描述" +
                       "\n   - accommodation: 住宿建议" +
                       "\n   - transportation: 交通建议" +
                       "\n   - tips: 旅行小贴士" +
                       "\n\n用户需求是：" + userRequest;
        
        String llmResponse = llmService.getLLMResponse(prompt);
        
        // 创建一个包含LLM响应的计划
        Map<String, Object> plan = new HashMap<>();
        plan.put("userRequest", userRequest);
        
        // 尝试解析大模型返回的JSON数据
        try {
            // 清理响应数据，提取有效的JSON部分
            String cleanResponse = extractJsonFromString(llmResponse);
            JsonNode jsonNode = objectMapper.readTree(cleanResponse);
            
            // 提取各个字段
            if (jsonNode.has("destination")) {
                plan.put("destination", jsonNode.get("destination").asText());
            }
            if (jsonNode.has("duration")) {
                plan.put("duration", jsonNode.get("duration").asInt());
            }
            if (jsonNode.has("budget")) {
                plan.put("budget", jsonNode.get("budget").asText());
            }
            if (jsonNode.has("travelers")) {
                plan.put("travelers", jsonNode.get("travelers").asText());
            }
            if (jsonNode.has("summary")) {
                plan.put("summary", jsonNode.get("summary").asText());
            }
            if (jsonNode.has("itineraryItems")) {
                plan.put("itineraryItems", objectMapper.convertValue(jsonNode.get("itineraryItems"), List.class));
            }
            if (jsonNode.has("accommodation")) {
                plan.put("accommodation", jsonNode.get("accommodation").asText());
            }
            if (jsonNode.has("transportation")) {
                plan.put("transportation", jsonNode.get("transportation").asText());
            }
            if (jsonNode.has("tips")) {
                plan.put("tips", objectMapper.convertValue(jsonNode.get("tips"), List.class));
            }
        } catch (Exception e) {
            System.err.println("解析大模型响应失败: " + e.getMessage());
            e.printStackTrace();
            // 如果解析失败，将原始响应放入llmResponse字段
            plan.put("llmResponse", llmResponse);
        }
        
        return plan;
    }
    
    /**
     * 保存旅行计划到数据库
     * @param planData 旅行计划数据
     * @param session HTTP会话
     * @return 保存结果
     */
    @PostMapping("/save")
    public Map<String, Object> savePlan(@RequestBody Map<String, Object> planData, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从会话中获取用户名
            String username = (String) session.getAttribute("username");
            if (username == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }
            
            // 将计划数据转换为字符串
            String planJson = objectMapper.writeValueAsString(planData);
            
            // 保存到数据库
            TravelPlan savedPlan = travelPlanService.saveTravelPlan(username, planJson);
            
            result.put("success", true);
            result.put("message", "旅行计划保存成功");
            result.put("planId", savedPlan.getId());
        } catch (Exception e) {
            System.err.println("保存旅行计划失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "保存旅行计划失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取当前用户的旅行计划历史
     * @param session HTTP会话
     * @return 旅行计划列表
     */
    @GetMapping("/history")
    public List<TravelPlan> getPlanHistory(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ArrayList<>();
        }
        
        return travelPlanService.getTravelPlansByUsername(username);
    }
    
    /**
     * 删除当前用户的所有旅行计划历史
     * @param session HTTP会话
     * @return 删除结果
     */
    @DeleteMapping("/history")
    public Map<String, Object> deletePlanHistory(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从会话中获取用户名
            String username = (String) session.getAttribute("username");
            if (username == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }
            
            // 删除用户的所有旅行计划
            int deletedCount = travelPlanService.deleteTravelPlansByUsername(username);
            
            result.put("success", true);
            result.put("message", "成功删除 " + deletedCount + " 条旅行计划记录");
        } catch (Exception e) {
            System.err.println("删除旅行计划历史失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除旅行计划历史失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除指定ID的旅行计划
     * @param id 旅行计划ID
     * @param session HTTP会话
     * @return 删除结果
     */
    @DeleteMapping("/history/{id}")
    public Map<String, Object> deletePlanById(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从会话中获取用户名
            String username = (String) session.getAttribute("username");
            if (username == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }
            
            // 删除指定ID的旅行计划
            boolean deleted = travelPlanService.deleteTravelPlanByIdAndUsername(id, username);
            
            if (deleted) {
                result.put("success", true);
                result.put("message", "旅行计划删除成功");
            } else {
                result.put("success", false);
                result.put("message", "旅行计划不存在或无权限删除");
            }
        } catch (Exception e) {
            System.err.println("删除旅行计划失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除旅行计划失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从字符串中提取有效的JSON部分
     * @param input 包含JSON的字符串
     * @return 提取的JSON字符串
     */
    private String extractJsonFromString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 查找JSON的开始和结束位置
        int jsonStart = input.indexOf("{");
        int jsonEnd = input.lastIndexOf("}") + 1;
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return input.substring(jsonStart, jsonEnd);
        }
        
        return input;
    }
}