import React, { useEffect, useRef } from 'react';

const MapSearch = ({ onSearch }) => {
  const inputRef = useRef(null);

  useEffect(() => {
    // 动态加载高德地图API
    const loadAMap = () => {
      if (window.AMap) {
        initAutoComplete();
        return;
      }

      // TODO 配置安全密钥
      window._AMapSecurityConfig = {
        securityJsCode: '', // 请配置高德地图安全密钥
      };

      const script = document.createElement('script');
      script.src = 'https://webapi.amap.com/maps?v=2.0&key=&plugin=AMap.AutoComplete'; // 请配置高德地图API Key
      script.async = true;
      script.onload = () => {
        initAutoComplete();
      };
      document.head.appendChild(script);
    };

    const initAutoComplete = () => {
      if (!inputRef.current || !window.AMap) return;

      // 输入提示
      const auto = new window.AMap.AutoComplete({
        input: inputRef.current
      });

      // 添加搜索结果选择事件
      auto.on('select', (e) => {
        if (onSearch && e.poi) {
          onSearch(e.poi);
        }
      });
    };

    loadAMap();
  }, [onSearch]);

  return (
    <div className="map-search">
      <div className="input-item">
        <div className="input-item-prepend">
          <span className="input-item-text" style={{ width: '8rem' }}>请输入关键字</span>
        </div>
        <input 
          ref={inputRef}
          type="text" 
          placeholder="搜索地点"
        />
      </div>
    </div>
  );
};

export default MapSearch;