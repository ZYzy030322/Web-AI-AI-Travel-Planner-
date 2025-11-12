package com.travelplanner.controller;

import com.travelplanner.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
@CrossOrigin
public class LLMController {
    
    @Autowired
    private LLMService llmService;
    
    /**
     * 向大模型提问（文本输入）
     * @param question 用户问题
     * @return 大模型回答
     */
    @PostMapping("/ask")
    public Map<String, String> askLLM(@RequestBody Map<String, String> requestBody) {
        String question = requestBody.get("question");
        String response = llmService.getLLMResponse(question);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }
    
    /**
     * 通过语音识别后向大模型提问
     * @param audioText 语音识别后的文本
     * @return 大模型回答
     */
    @PostMapping("/ask-by-voice")
    public Map<String, String> askLLMByVoice(@RequestBody Map<String, String> requestBody) {
        String audioText = requestBody.get("audioText");
        String response = llmService.getLLMResponse(audioText);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }
}