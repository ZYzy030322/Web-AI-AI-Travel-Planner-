import React, { useState, useEffect } from 'react';
import ItineraryDisplay from '../components/ItineraryDisplay';

const UserProfile = () => {
  const [travelPlans, setTravelPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState(null);

  useEffect(() => {
    fetchTravelPlanHistory();
  }, []);

  const fetchTravelPlanHistory = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/plans/history', {
        credentials: 'include'  // 添加凭据支持
      });

      if (response.ok) {
        const plans = await response.json();
        setTravelPlans(plans);
      } else {
        throw new Error('获取旅行计划历史失败');
      }
    } catch (error) {
      console.error('获取旅行计划历史错误:', error);
      alert('获取旅行计划历史时出错: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePlanSelect = (planJson) => {
    try {
      const plan = JSON.parse(planJson);
      setSelectedPlan(plan);
    } catch (e) {
      console.error('解析计划数据失败:', e);
      alert('无法解析旅行计划数据');
    }
  };

  const clearTravelPlanHistory = async () => {
    if (!window.confirm('确定要清空所有旅行计划历史吗？此操作不可恢复。')) {
      return;
    }

    try {
      const response = await fetch('/api/plans/history', {
        method: 'DELETE',
        credentials: 'include'
      });

      const result = await response.json();

      if (result.success) {
        // 清空本地状态
        setTravelPlans([]);
        setSelectedPlan(null);
        alert('旅行计划历史已清空');
      } else {
        throw new Error(result.message || '清空旅行计划历史失败');
      }
    } catch (error) {
      console.error('清空旅行计划历史错误:', error);
      alert('清空旅行计划历史时出错: ' + error.message);
    }
  };

  const deleteSinglePlan = async (planId) => {
    if (!window.confirm('确定要删除这个旅行计划吗？此操作不可恢复。')) {
      return;
    }

    try {
      const response = await fetch(`/api/plans/history/${planId}`, {
        method: 'DELETE',
        credentials: 'include'
      });

      const result = await response.json();

      if (result.success) {
        // 从本地状态中移除该计划
        setTravelPlans(travelPlans.filter(plan => plan.id !== planId));
        // 如果正在查看的计划被删除，则关闭详情视图
        if (selectedPlan && selectedPlan.id === planId) {
          setSelectedPlan(null);
        }
        alert('旅行计划已删除');
      } else {
        throw new Error(result.message || '删除旅行计划失败');
      }
    } catch (error) {
      console.error('删除旅行计划错误:', error);
      alert('删除旅行计划时出错: ' + error.message);
    }
  };

  return (
    <div className="user-profile">
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>旅行计划历史</h2>
          {travelPlans.length > 0 && (
            <button 
              className="btn btn-secondary" 
              onClick={clearTravelPlanHistory}
              disabled={loading}
            >
              清空历史记录
            </button>
          )}
        </div>
        
        {loading ? (
          <p>正在加载旅行计划历史...</p>
        ) : travelPlans.length === 0 ? (
          <p>暂无旅行计划历史</p>
        ) : (
          <div>
            <ul className="plan-history-list">
              {travelPlans.map((plan, index) => (
                <li key={plan.id} className="plan-history-item">
                  <div>
                    <strong>计划 #{index + 1}</strong> - 创建于 {new Date(plan.createdAt).toLocaleString()}
                  </div>
                  <div>
                    <button 
                      className="btn btn-secondary" 
                      onClick={() => handlePlanSelect(plan.travelPlan)}
                      style={{ marginRight: '0.5rem' }}
                    >
                      查看详情
                    </button>
                    <button 
                      className="btn btn-danger" 
                      onClick={() => deleteSinglePlan(plan.id)}
                    >
                      删除
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {selectedPlan && (
        <div className="plan-result">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>旅行计划详情</h3>
              <button 
                className="btn btn-secondary" 
                onClick={() => setSelectedPlan(null)}
              >
                关闭
              </button>
            </div>
            <ItineraryDisplay plan={selectedPlan} />
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;