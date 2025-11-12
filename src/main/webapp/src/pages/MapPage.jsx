import React, { useEffect, useRef, useState } from 'react';
// 移除对MapSearch的导入
import './MapPage.css';

const MapPage = () => {
  const mapRef = useRef(null);
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [startPoint, setStartPoint] = useState('');
  const [endPoint, setEndPoint] = useState('');
  const mapInstance = useRef(null);
  const drivingInstance = useRef(null);
  const geolocationInstance = useRef(null);

  useEffect(() => {
    // 配置高德地图安全密钥
    window._AMapSecurityConfig = {
      securityJsCode: '5cc65972557af47a852ea919167b7ff2',
    };

    // 动态加载高德地图API
    const loadAMap = () => {
      if (window.AMap) {
        initMap();
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://webapi.amap.com/loader.js';
      script.async = true;
      script.onload = () => {
        window.AMapLoader.load({
          key: 'c9e975e2b488df23d4cf6b3071616a79', // 你的应用key
          version: '2.0',
          plugins: ['AMap.Scale', 'AMap.HawkEye', 'AMap.ToolBar', 'AMap.ControlBar', 'AMap.Driving', 'AMap.Geolocation']
        })
          .then((AMap) => {
            initMap(AMap);
          })
          .catch((e) => {
            console.error('高德地图加载失败:', e);
          });
      };
      document.head.appendChild(script);
    };

    // 初始化地图
    const initMap = (AMapInstance = window.AMap) => {
      if (!mapRef.current) return;

      const map = new AMapInstance.Map(mapRef.current, {
        zoom: 11,
        center: [116.397428, 39.90923],
      });

      mapInstance.current = map;

      // 添加控件
      const scale = new AMapInstance.Scale({
        visible: true
      });
      
      const toolbar = new AMapInstance.ToolBar({
        visible: true,
        position: {
          top: '110px',
          right: '40px'
        }
      });
      
      const controlBar = new AMapInstance.ControlBar({
        visible: true,
        position: {
          top: '10px',
          right: '10px'
        }
      });
      
      const overview = new AMapInstance.HawkEye({
        visible: true
      });
      
      map.addControl(scale);
      map.addControl(toolbar);
      map.addControl(controlBar);
      map.addControl(overview);

      // 初始化定位插件
      AMapInstance.plugin(['AMap.Geolocation'], () => {
        const geolocation = new AMapInstance.Geolocation({
          enableHighAccuracy: true, // 是否使用高精度定位
          timeout: 10000,           // 超过10秒后停止定位
          maximumAge: 60000,        // 定位结果缓存时间
          convert: true,            // 自动偏移坐标
          showButton: false,        // 不显示默认按钮
          panToLocation: true,      // 定位成功后自动移动到定位位置
          zoomToAccuracy: true      // 定位成功后调整地图级别
        });
        
        map.addControl(geolocation);
        geolocationInstance.current = geolocation;
      });

      // 初始化驾车路线规划插件
      AMapInstance.plugin(['AMap.Driving'], () => {
        const driving = new AMapInstance.Driving({
          map: map,
          panel: 'route-panel' // 路线面板DOM容器ID
        });
        drivingInstance.current = driving;
      });
    };

    loadAMap();

    // 清理函数
    return () => {
      // 如果需要清理地图实例，可以在这里处理
    };
  }, []);

  // 移除handleSearch函数，因为不再需要处理搜索结果
  
  // 规划路线函数
  const planRoute = () => {
    if (!startPoint || !endPoint) {
      alert('请输入起点和终点');
      return;
    }

    if (!drivingInstance.current) {
      alert('路线规划插件未初始化完成，请稍后再试');
      return;
    }

    const points = [
      { keyword: startPoint, city: '全国' },
      { keyword: endPoint, city: '全国' }
    ];

    // 获取起终点规划线路
    drivingInstance.current.search(points, (status, result) => {
      if (status === 'complete') {
        // 查询成功
        console.log('路线规划成功:', result);
      } else {
        console.log('获取驾车数据失败：' + result);
        alert('路线规划失败，请检查起点和终点是否正确');
      }
    });
  };

  // 定位函数
  const locate = () => {
    if (!geolocationInstance.current) {
      alert('定位插件未初始化完成，请稍后再试');
      return;
    }

    geolocationInstance.current.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        // 定位成功
        console.log('定位成功:', result);
        // 可以在这里处理定位成功后的逻辑
      } else {
        // 定位失败
        console.error('定位失败:', result.message);
        alert(`定位失败: ${result.message}`);
      }
    });
  };

  return (
    <div className="map-page">
      <h2>地图浏览</h2>
      {/* 移除MapSearch组件 */}
      
      {/* 路线规划输入区域 */}
      <div className="route-inputs">
        <h3>路线规划</h3>
        <div className="input-group">
          <label>起点：
            <input 
              type="text" 
              value={startPoint}
              onChange={(e) => setStartPoint(e.target.value)}
              placeholder="请输入起点" 
            />
          </label>
        </div>
        <div className="input-group">
          <label>终点：
            <input 
              type="text" 
              value={endPoint}
              onChange={(e) => setEndPoint(e.target.value)}
              placeholder="请输入终点" 
            />
          </label>
        </div>
        <div className="button-group">
          <button onClick={planRoute}>规划路线</button>
          <button onClick={locate} className="locate-button">定位</button>
        </div>
      </div>
      
      <div className="map-container-wrapper">
        <div id="container" ref={mapRef} className="map-container"></div>
        <div id="route-panel" className="route-panel"></div>
      </div>
    </div>
  );
};

export default MapPage;