@echo off
echo 开始测试语音识别功能...

echo 说明：
echo 1. 本测试需要有效的讯飞语音识别API密钥
echo 2. 请先在讯飞开放平台（https://www.xfyun.cn/）注册账号并创建应用
echo 3. 获取应用的APPID、API Key和API Secret

echo.
echo 设置环境变量方法：
echo 方法1：在命令行中设置（每次运行前都需要设置）
echo    set XFYUN_APP_ID=你的讯飞应用ID
echo    set XFYUN_API_KEY=你的讯飞API Key
echo    set XFYUN_API_SECRET=你的讯飞API Secret
echo    mvn exec:java -Dexec.mainClass="com.travelplanner.service.VoiceRecognitionServiceTest"

echo.
echo 方法2：在系统环境变量中设置（一次性设置，永久有效）
echo    在Windows系统中：
echo      1. 右键"此电脑" - "属性" - "高级系统设置"
echo      2. 点击"环境变量"
echo      3. 在"系统变量"或"用户变量"中添加以下三个变量：
echo         XFYUN_APP_ID = 你的讯飞应用ID
echo         XFYUN_API_KEY = 你的讯飞API Key
echo         XFYUN_API_SECRET = 你的讯飞API Secret
echo      4. 确定保存后重启命令行窗口
echo      5. 运行测试：mvn exec:java -Dexec.mainClass="com.travelplanner.service.VoiceRecognitionServiceTest"

echo.
echo 运行测试命令：
echo mvn exec:java -Dexec.mainClass="com.travelplanner.service.VoiceRecognitionServiceTest"

echo.
echo 运行单元测试命令：
echo mvn test -Dtest=VoiceRecognitionServiceUnitTest

echo.
echo 请确保已配置有效的讯飞API密钥后再运行测试。
pause