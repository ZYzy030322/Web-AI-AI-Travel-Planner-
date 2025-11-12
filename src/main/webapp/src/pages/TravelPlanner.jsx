import React, { useState } from 'react';
import VoiceInput from '../components/VoiceInput';
import ItineraryDisplay from '../components/ItineraryDisplay';
import './TravelPlanner.css';

const TravelPlanner = ({ travelPlan, setTravelPlan, userInput, setUserInput }) => {
  const [isGenerating, setIsGenerating] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const handleGeneratePlan = async () => {
    if (!userInput.trim()) {
      alert('请输入您的旅行计划');
      return;
    }

    setIsGenerating(true);
    try {
      // 调用后端API生成旅行计划
      const response = await fetch('/api/plans', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',  // 添加凭据支持
        body: JSON.stringify(userInput),
      });

      if (response.ok) {
        const plan = await response.json();
        setTravelPlan(plan);
      } else {
        throw new Error('生成旅行计划失败');
      }
    } catch (error) {
      console.error('Error generating travel plan:', error);
      alert('生成旅行计划时出错: ' + error.message);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSavePlan = async () => {
    if (!travelPlan) {
      alert('没有可保存的旅行计划');
      return;
    }

    setIsSaving(true);
    try {
      // 调用后端API保存旅行计划
      const response = await fetch('/api/plans/save', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',  // 添加凭据支持
        body: JSON.stringify(travelPlan),
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success) {
          alert('旅行计划保存成功');
        } else {
          alert('保存失败: ' + result.message);
        }
      } else {
        throw new Error('保存旅行计划失败');
      }
    } catch (error) {
      console.error('Error saving travel plan:', error);
      alert('保存旅行计划时出错: ' + error.message);
    } finally {
      setIsSaving(false);
    }
  };

  const handleVoiceInput = (text) => {
    console.log('接收到语音识别结果:', text); // 添加调试日志
    setUserInput(text || '');
  };

  return (
    <div className="travel-planner">
      <div className="card">
        <h2>智能行程规划</h2>
        <div className="form-group">
          <label htmlFor="userInput">告诉我您的旅行计划：</label>
          <textarea
            id="userInput"
            className="form-control"
            rows="4"
            placeholder="例如：我想去日本，5天，预算1万元，喜欢美食和动漫，带孩子"
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
          />
        </div>

        <VoiceInput onResult={handleVoiceInput} />

        <button 
          className="btn" 
          onClick={handleGeneratePlan} 
          disabled={isGenerating}
        >
          {isGenerating ? '正在生成...' : '生成旅行计划'}
        </button>
      </div>

      {travelPlan && (
        <div className="plan-result">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>旅行计划</h3>
              <button 
                className="btn" 
                onClick={handleSavePlan} 
                disabled={isSaving}
                style={{ marginLeft: '1rem' }}
              >
                {isSaving ? '正在保存...' : '保存计划'}
              </button>
            </div>
            <ItineraryDisplay plan={travelPlan} />
          </div>
        </div>
      )}
    </div>
  );
};

export default TravelPlanner;