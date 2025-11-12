import React, { useState } from 'react';
import './TravelPlanner.css';

const LoginPage = ({ onLogin }) => {
  const [isRegistering, setIsRegistering] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    // 简单的验证
    if (!username || !password) {
      setError('请填写所有字段');
      return;
    }
    
    try {
      const response = await fetch('/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // 添加凭据支持
        body: JSON.stringify({ username, password }),
      });
      
      if (response.ok) {
        const userData = await response.json();
        onLogin(userData);
      } else {
        const errorData = await response.json();
        setError(errorData.message || '登录失败');
      }
    } catch (err) {
      setError('网络错误，请稍后再试');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    // 简单的验证
    if (!username || !password) {
      setError('请填写所有字段');
      return;
    }
    
    if (password !== confirmPassword) {
      setError('密码不匹配');
      return;
    }
    
    try {
      const response = await fetch('/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // 添加凭据支持
        body: JSON.stringify({ username, password }),
      });
      
      if (response.ok) {
        const userData = await response.json();
        onLogin(userData);
      } else {
        const errorData = await response.json();
        setError(errorData.message || '注册失败');
      }
    } catch (err) {
      setError('网络错误，请稍后再试');
    }
  };

  return (
    <div className="container">
      <div className="card" style={{ maxWidth: '400px', margin: '2rem auto' }}>
        <h2 style={{ textAlign: 'center' }}>
          {isRegistering ? '用户注册' : '用户登录'}
        </h2>
        
        {error && (
          <div className="result-card" style={{ backgroundColor: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={isRegistering ? handleRegister : handleLogin}>
          <div className="form-group">
            <label htmlFor="username">用户名:</label>
            <input
              type="text"
              id="username"
              className="form-control"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">密码:</label>
            <input
              type="password"
              id="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码"
            />
          </div>
          
          {isRegistering && (
            <div className="form-group">
              <label htmlFor="confirmPassword">确认密码:</label>
              <input
                type="password"
                id="confirmPassword"
                className="form-control"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="请再次输入密码"
              />
            </div>
          )}
          
          <button type="submit" className="btn" style={{ width: '100%', marginBottom: '1rem' }}>
            {isRegistering ? '注册' : '登录'}
          </button>
        </form>
        
        <div style={{ textAlign: 'center' }}>
          <button 
            className="btn btn-secondary" 
            onClick={() => {
              setIsRegistering(!isRegistering);
              setError('');
            }}
            style={{ width: '100%' }}
          >
            {isRegistering ? '已有账户？直接登录' : '没有账户？立即注册'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;