# Web版AI旅行规划师

## 直接下载Docker镜像并运行

1. 下载release中tag为"将本地镜像保存为文件（导出镜像）"的tar包，保存到本地。

2. 加载Docker镜像：
   ```bash
   docker load -i /path/to/你的镜像文件.tar
   ```
   注意：将 `/path/to/你的镜像文件.tar` 替换为你实际保存tar包的路径。

3. 运行容器：
   ```bash
   # 后台运行，映射 8080 端口，命名为 mycontainer
   docker run -d -p 8080:8080 --name mycontainer myapp:v1
   ```
   其中 `myapp:v1` 是你导入的镜像名称和版本。

   或者你也可以在Docker Desktop中运行该镜像。

## 使用阿里云镜像快速启动

### 1. 登录阿里云Container Registry
```bash
docker login --username=nick4081431095 crpi-2ha1tzzb1mphmj06.cn-hangzhou.personal.cr.aliyuncs.com
```
密码: （在提交作业的pdf文档里）

### 2. 从Registry中拉取镜像
```bash
docker pull crpi-2ha1tzzb1mphmj06.cn-hangzhou.personal.cr.aliyuncs.com/aitravelplan/aitravelplanner:latest
```

### 3. 在本地Docker运行
```bash
docker run -p 8080:8080 crpi-2ha1tzzb1mphmj06.cn-hangzhou.personal.cr.aliyuncs.com/aitravelplan/aitravelplanner:latest
```
注意：必须指定端口号，否则前端页面无法访问（我也不知道为什么。别的端口号没试过，不知道行不行）。

## 项目简介

Web版AI旅行规划师是一款基于人工智能技术的旅行规划应用，旨在简化旅行规划过程。用户可以通过语音或文字输入旅行需求，AI会自动生成详细的旅行路线和建议，并提供实时旅行辅助。

## 核心功能

### 1. 智能行程规划
- 用户可以通过语音（或文字）输入旅行目的地、日期、预算、同行人数、旅行偏好
- AI自动生成个性化的旅行路线，包括交通、住宿、景点、餐厅等详细信息

### 2. 费用预算与管理
- AI进行预算分析和费用估算
- 记录旅行开销（支持语音输入）

### 3. 用户管理与数据存储
- 注册登录系统，用户可以保存和管理多份旅行计划
- 云端行程同步，方便多设备查看和修改

## 技术栈

### 后端技术
- Java 8
- Spring Boot 2.7.18
- Maven
- PostgreSQL数据库

### 前端技术
- React 18
- Vite
- HTML5/CSS3/JavaScript

### 第三方服务集成
- 语音识别：科大讯飞语音识别API
- 地图服务：高德地图API
- AI大模型：科大讯飞星火认知大模型API（可替换为其他大语言模型API）


## 快速开始

### 环境要求
- JDK 1.8
- Node.js 16+
- Maven 3.8+
- Docker (可选，用于容器化部署)

### 配置API密钥

在运行项目之前，需要配置以下API密钥：

1. 讯飞语音识别API（用于语音输入功能）
   - XFYUN_APP_ID
   - XFYUN_API_KEY
   - XFYUN_API_SECRET

2. 高德地图API（暂时不用，在前端连接地图api）
   - AMAP_KEY

3. 大语言模型API（用于AI行程规划）
   - LLM_APP_ID
   - LLM_API_KEY
   - LLM_API_SECRET

可以通过以下方式配置：
1. 设置环境变量
2. 修改 `src/main/resources/application.yml` 文件中的值
3. 修改 `src/main/webapp/components/MapSearch.jsx`中安全密钥和key

### 本地运行

1. 克隆项目到本地：
   ```bash
   git clone <项目地址>
   cd AITravelPlanner
   ```

2. 构建项目：
   ```bash
   mvn clean package
   ```

3. 运行应用：
   ```bash
   java -jar target/*.jar
   ```

4. 访问应用：
   打开浏览器访问 http://localhost:8080（本项目部署在8080端口，可在docker修改运行的端口号）

### Docker部署

项目已经包含了Dockerfile文件，可以直接用于构建和运行容器。

1. 确保已安装Docker并正常运行
2. 构建Docker镜像：
   ```bash
   docker build -t aitravelplanner .
   ```

3. 运行容器：
   ```bash
   docker run -p 8080:8080 aitravelplanner
   ```

4. 访问应用：
   打开浏览器访问 http://localhost:8080

注意：Dockerfile中的EXPOSE指令仅声明了容器的期望端口，要从主机访问容器中的服务，必须使用`-p`参数进行端口映射。

## 开发指南

### 项目结构
```
AITravelPlanner/
├── src/
│   ├── main/
│   │   ├── java/com/travelplanner/
│   │   │   ├── controller/     # 控制器层
│   │   │   ├── model/          # 数据模型
│   │   │   ├── service/        # 业务逻辑层
│   │   │   ├── websocket/      # WebSocket处理
│   │   │   ├── config/         # 配置类
│   │   │   └── Application.java # 启动类
│   │   ├── resources/
│   │   │   ├── static/         # 前端静态资源
│   │   │   └── application.yml # 配置文件
│   │   └── webapp/
│   │       ├── src/            # 前端源代码
│   │       └── dist/           # 前端构建产物
├── pom.xml                     # Maven配置文件
├── package.json                # 前端依赖配置
├── vite.config.js              # Vite配置
├── Dockerfile                  # Docker配置
└── README.md                   # 项目说明文档
```

### 前端开发

1. 进入前端目录：
   ```bash
   cd src/main/webapp
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm run dev
   ```

4. 构建生产版本：
   ```bash
   npm run build
   ```

### 后端开发

使用标准的Spring Boot开发流程，通过Maven管理依赖。

## 测试

### 测试语音识别功能

项目提供了一个测试类用于验证语音识别服务是否正常工作：

1. 确保已配置讯飞API密钥环境变量
2. 运行测试：
   ```bash
   mvn exec:java -Dexec.mainClass="com.travelplanner.service.VoiceRecognitionServiceTest"
   ```

或者直接运行测试脚本：
```bash
test-voice-recognition.bat
```

测试将使用项目资源目录中的`iat/16k_10.pcm`音频文件进行语音识别测试。

## 安全说明

为了保护API密钥安全，项目采用了以下措施：
1. 所有API密钥默认值为空，需要用户自行配置
2. 支持通过环境变量配置API密钥，避免硬编码
3. 前端代码中的API密钥也已清空，需要用户自行配置

## 常见问题

### 1. 如何配置API密钥？
可以通过设置环境变量或直接修改配置文件的方式配置API密钥。

### 2. 如何测试语音识别功能？
确保已配置讯飞API密钥后，运行测试脚本或使用Maven命令进行测试。

### 3. 如何自定义AI大模型？
项目支持更换不同的大语言模型，只需修改相关配置和实现对应的接口即可。

### 4. 语音识别错误
当使用语音识别功能时，可能会遇到以下错误：
- 错误代码10165，提示"invalid handle"：这通常是由于WebSocket连接建立时序问题导致的。请确保在WebSocket连接完全建立（onOpen回调触发）后再发送第一帧音频数据，并且首帧音频数据的status必须设置为0。
- WebSocket连接失败，错误代码-1，提示"Software caused connection abort: socket write error"：这通常是由于网络连接不稳定或服务器端主动关闭连接导致的。建议检查网络连接状态，并确保API密钥配置正确。
