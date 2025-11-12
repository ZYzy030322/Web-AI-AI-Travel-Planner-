import React, { useState, useEffect } from 'react';
import TravelPlanner from './pages/TravelPlanner';
import UserProfile from './pages/UserProfile';
import LoginPage from './pages/LoginPage';
import MapPage from './pages/MapPage';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('planner');
  const [currentUser, setCurrentUser] = useState(null);
  const [travelPlan, setTravelPlan] = useState(null);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(true);

  // 页面加载时检查用户登录状态
  useEffect(() => {
    const checkLoginStatus = async () => {
      try {
        const response = await fetch('/api/users/check-status', {
          credentials: 'include'
        });
        
        if (response.ok) {
          const userData = await response.json();
          setCurrentUser(userData);
        }
      } catch (error) {
        console.error('检查登录状态失败:', error);
      } finally {
        setLoading(false);
      }
    };

    checkLoginStatus();
  }, []);

  const handleLogin = (user) => {
    setCurrentUser(user);
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setActiveTab('planner');
    setTravelPlan(null);
    setUserInput('');
  };

  // 如果还在检查登录状态，显示加载提示
  if (loading) {
    return (
      <div className="app">
        <header className="app-header">
          <h1>AI 旅行规划师</h1>
        </header>
        <main className="app-main">
          <div style={{ textAlign: 'center', padding: '2rem' }}>
            <p>正在加载中...</p>
          </div>
        </main>
      </div>
    );
  }

  // 如果用户未登录，显示登录页面
  if (!currentUser) {
    return (
      <div className="app">
        <header className="app-header">
          <h1>AI 旅行规划师</h1>
        </header>
        <main className="app-main">
          <LoginPage onLogin={handleLogin} />
        </main>
      </div>
    );
  }

  // 用户已登录，显示主应用界面
  return (
    <div className="app">
      <header className="app-header">
        <h1>AI 旅行规划师</h1>
        <div className="user-info">
          欢迎, {currentUser.username}!
          <button className="btn btn-secondary" onClick={handleLogout} style={{ marginLeft: '1rem' }}>
            退出登录
          </button>
        </div>
        <nav className="main-nav">
          <button 
            className={activeTab === 'planner' ? 'active' : ''}
            onClick={() => setActiveTab('planner')}
          >
            智能行程规划
          </button>
          <button 
            className={activeTab === 'map' ? 'active' : ''}
            onClick={() => setActiveTab('map')}
          >
            地图浏览
          </button>
          <button 
            className={activeTab === 'profile' ? 'active' : ''}
            onClick={() => setActiveTab('profile')}
          >
            个人中心
          </button>
        </nav>
      </header>

      <main className="app-main">
        {activeTab === 'planner' && <TravelPlanner 
          travelPlan={travelPlan} 
          setTravelPlan={setTravelPlan}
          userInput={userInput}
          setUserInput={setUserInput}
        />}
        {activeTab === 'map' && <MapPage />}
        {activeTab === 'profile' && <UserProfile />}
      </main>
    </div>
  );
}

export default App;