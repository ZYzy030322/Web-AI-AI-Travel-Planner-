import React from 'react';
import './ItineraryDisplay.css';

const ItineraryDisplay = ({ plan }) => {
  // 如果plan中包含llmResponse，说明解析失败，直接显示原始响应
  if (plan.llmResponse) {
    return (
      <div className="itinerary-display card">
        <h3>您的旅行计划</h3>
        <pre>{plan.llmResponse}</pre>
      </div>
    );
  }

  return (
    <div className="itinerary-display card">
      <h3>您的旅行计划</h3>
      
      <div className="plan-summary">
        <h4>行程概览</h4>
        {plan.destination && (
          <p><strong>目的地：</strong>{plan.destination}</p>
        )}
        {plan.duration && (
          <p><strong>天数：</strong>{plan.duration} 天</p>
        )}
        {plan.budget && (
          <p><strong>预算：</strong>{plan.budget}</p>
        )}
        {plan.travelers && (
          <p><strong>旅客类型：</strong>{plan.travelers}</p>
        )}
        {plan.summary && (
          <p><strong>摘要：</strong>{plan.summary}</p>
        )}
      </div>

      {plan.itineraryItems && plan.itineraryItems.length > 0 && (
        <div className="daily-itinerary">
          <h4>详细行程安排</h4>
          {plan.itineraryItems.map((item, index) => (
            <div key={index} className="itinerary-item">
              <h5>{item.day ? `第${item.day}天` : `项目${index + 1}`}</h5>
              {item.time && <p><strong>时间：</strong>{item.time}</p>}
              {item.activity && <p><strong>活动：</strong>{item.activity}</p>}
              {item.location && <p><strong>地点：</strong>{item.location}</p>}
              {item.description && <p><strong>描述：</strong>{item.description}</p>}
            </div>
          ))}
        </div>
      )}

      {plan.accommodation && (
        <div className="accommodation-info">
          <h4>住宿建议</h4>
          <p>{plan.accommodation}</p>
        </div>
      )}

      {plan.transportation && (
        <div className="transportation-info">
          <h4>交通建议</h4>
          <p>{plan.transportation}</p>
        </div>
      )}

      {plan.tips && plan.tips.length > 0 && (
        <div className="tips-info">
          <h4>旅行小贴士</h4>
          <ul>
            {plan.tips.map((tip, index) => (
              <li key={index}>{tip}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default ItineraryDisplay;