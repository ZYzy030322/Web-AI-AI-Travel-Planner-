package com.travelplanner.service;

import com.travelplanner.model.TravelPlan;
import com.travelplanner.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TravelPlanService {
    
    @Autowired
    private TravelPlanRepository travelPlanRepository;
    
    /**
     * 保存旅行计划
     * @param username 用户名
     * @param travelPlan 旅行计划内容
     * @return 保存的TravelPlan对象
     */
    public TravelPlan saveTravelPlan(String username, String travelPlan) {
        TravelPlan plan = new TravelPlan(username, travelPlan);
        return travelPlanRepository.save(plan);
    }
    
    /**
     * 根据用户名获取旅行计划历史
     * @param username 用户名
     * @return 旅行计划列表
     */
    public List<TravelPlan> getTravelPlansByUsername(String username) {
        return travelPlanRepository.findByUsernameOrderByCreatedAtDesc(username);
    }
    
    /**
     * 根据用户名删除所有旅行计划
     * @param username 用户名
     * @return 删除的记录数
     */
    public int deleteTravelPlansByUsername(String username) {
        List<TravelPlan> plans = travelPlanRepository.findByUsernameOrderByCreatedAtDesc(username);
        int count = plans.size();
        travelPlanRepository.deleteAll(plans);
        return count;
    }
    
    /**
     * 根据ID和用户名删除单个旅行计划
     * @param id 旅行计划ID
     * @param username 用户名
     * @return 删除是否成功
     */
    public boolean deleteTravelPlanByIdAndUsername(Long id, String username) {
        Optional<TravelPlan> plan = travelPlanRepository.findById(id);
        if (plan.isPresent() && plan.get().getUsername().equals(username)) {
            travelPlanRepository.deleteById(id);
            return true;
        }
        return false;
    }
}