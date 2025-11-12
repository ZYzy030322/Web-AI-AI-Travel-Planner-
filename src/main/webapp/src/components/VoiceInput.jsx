import React, { useState, useRef, useEffect } from 'react';

const VoiceInput = ({ onResult }) => {
  const [isListening, setIsListening] = useState(false);
  const [realTimeTranscript, setRealTimeTranscript] = useState('');
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const transcriptTimerRef = useRef(null);
  const finalResultTimerRef = useRef(null);

  // 定期获取实时识别结果
  useEffect(() => {
    if (isListening) {
      const fetchRealTimeResult = async () => {
        try {
          const response = await fetch('/api/voice/realtime/result');
          if (response.ok) {
            const data = await response.json();
            if (data.success) {
              // 只显示最新的识别结果，而不是累积显示
              setRealTimeTranscript(data.text || '');
            }
          }
        } catch (error) {
          console.error('获取实时识别结果失败:', error);
        }
      };

      transcriptTimerRef.current = setInterval(fetchRealTimeResult, 500);
    } else {
      if (transcriptTimerRef.current) {
        clearInterval(transcriptTimerRef.current);
        transcriptTimerRef.current = null;
      }
    }

    return () => {
      if (transcriptTimerRef.current) {
        clearInterval(transcriptTimerRef.current);
        transcriptTimerRef.current = null;
      }
      if (finalResultTimerRef.current) {
        clearTimeout(finalResultTimerRef.current);
        finalResultTimerRef.current = null;
      }
    };
  }, [isListening]);

  const startListening = async () => {
    try {
      // 首先尝试使用新的实时语音识别API
      const response = await fetch('/api/voice/realtime/start', {
        method: 'POST'
      });

      const data = await response.json();
      if (data.success) {
        setIsListening(true);
        setRealTimeTranscript('');
        return;
      }

      // 如果新的API失败，则回落到旧的方式
      fallbackToOldMethod();
    } catch (error) {
      console.error('启动实时语音识别失败:', error);
      fallbackToOldMethod();
    }
  };

  const fallbackToOldMethod = async () => {
    try {
      // 检查浏览器是否支持录音API
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        alert('您的浏览器不支持录音功能');
        return;
      }

      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorderRef.current = new MediaRecorder(stream);
      audioChunksRef.current = [];

      mediaRecorderRef.current.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorderRef.current.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' });
        
        // 上传到服务器进行语音识别
        const formData = new FormData();
        formData.append('audio', audioBlob, 'recording.wav');

        try {
          const response = await fetch('/api/voice/recognize', {
            method: 'POST',
            body: formData,
          });

          if (response.ok) {
            const recognizedText = await response.text();
            // 无论结果是否为空都传递给父组件
            console.log('传递语音识别结果到父组件(传统方式):', recognizedText || '');
            onResult(recognizedText || '');
          } else {
            console.error('语音识别失败，HTTP状态码:', response.status);
            throw new Error('语音识别失败');
          }
        } catch (error) {
          console.error('语音识别错误:', error);
          alert('语音识别失败: ' + error.message);
        }

        stream.getTracks().forEach(track => track.stop());
      };

      mediaRecorderRef.current.start();
      setIsListening(true);
      setRealTimeTranscript('');
    } catch (error) {
      console.error('录音启动失败:', error);
      alert('无法访问麦克风: ' + error.message);
    }
  };

  const stopListening = async () => {
    if (isListening) {
      // 先清除 transcript 更新定时器
      if (transcriptTimerRef.current) {
        clearInterval(transcriptTimerRef.current);
        transcriptTimerRef.current = null;
      }
      
      try {
        // 尝试停止新的实时语音识别
        const response = await fetch('/api/voice/realtime/stop', {
          method: 'POST'
        });

        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            setIsListening(false);
            
            // 延迟获取最终识别结果，确保服务端处理完成
            finalResultTimerRef.current = setTimeout(async () => {
              try {
                // 获取最终识别结果
                const resultResponse = await fetch('/api/voice/realtime/result');
                if (resultResponse.ok) {
                  const resultData = await resultResponse.json();
                  console.log('获取到实时识别结果:', resultData); // 调试日志
                  if (resultData.success) {
                    // 无论结果是否为空都传递给父组件
                    console.log('传递语音识别结果到父组件(实时方式):', resultData.text || '');
                    onResult(resultData.text || '');
                  } else {
                    console.log('实时识别结果不成功:', resultData.message);
                    // 即使不成功也传递空字符串给父组件
                    onResult('');
                  }
                } else {
                  console.error('获取最终识别结果失败，HTTP状态码:', resultResponse.status);
                  // 传递空字符串给父组件
                  onResult('');
                }
              } catch (error) {
                console.error('获取最终识别结果失败:', error);
                // 传递空字符串给父组件
                onResult('');
              } finally {
                if (finalResultTimerRef.current) {
                  clearTimeout(finalResultTimerRef.current);
                  finalResultTimerRef.current = null;
                }
              }
            }, 500); // 延迟500毫秒以确保服务端处理完成
            
            return;
          }
        }
      } catch (error) {
        console.error('停止实时语音识别失败:', error);
      }

      // 回退到旧的方法
      if (mediaRecorderRef.current) {
        mediaRecorderRef.current.stop();
      }
      setIsListening(false);
    }
  };

  // 新增功能：将识别结果填入输入框
  const handleFillInput = () => {
    onResult(realTimeTranscript || '');
  };

  return (
    <div className="voice-input">
      <div className="form-group">
        <button
          className={`btn ${isListening ? 'btn-secondary' : ''}`}
          onClick={isListening ? stopListening : startListening}
        >
          {isListening ? '停止录音' : '语音输入'}
        </button>
        {/* 新增按钮：将识别结果填入输入框 */}
        {(realTimeTranscript || isListening) && (
          <button
            className="btn btn-primary"
            onClick={handleFillInput}
            style={{ marginLeft: '10px' }}
          >
            使用识别结果
          </button>
        )}
      </div>

      {(realTimeTranscript || isListening) && (
        <div className="form-group">
          <label>识别结果：</label>
          <div className="result-card">
            <span style={{ color: '#666' }}>{realTimeTranscript || '正在识别中...'}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default VoiceInput;