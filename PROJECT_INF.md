# 项目结构说明


```
AAndroid_AdvanceSDK
├── app                     # Demo 示例工程，展示如何集成和使用 SDK
├── advancecore             # SDK 核心库 (必须引入)，包含策略执行核心逻辑
├── advancemry              # 依赖引入 Mercury 变现 SDK 适配库 (需与核心库同时引入)
│
├── # --- 广告平台适配库 (Adapter) ---
├── advancebd               # aar引入 百度 (Baidu) 适配库   
├── advanceks               # aar引入 快手 (KS) 适配库
├── advanceoppo             # aar引入 OPPO 适配库
├── advancevivo             # aar引入 Vivo 适配库
├── advancexiaomi           # aar引入 小米 (Xiaomi) 适配库
├── advancesigmob           # aar引入 Sigmob 适配库
├── advancecsjoppo          # aar引入 穿山甲-OPPO 特定渠道适配库
├── advancecsj              # 依赖引入 穿山甲 (CSJ) 适配库     
├── advancetanx             # 依赖引入 Tanx (阿里) 适配库
├── advancegdt              # 依赖引入 优量汇 (GDT/腾讯) 适配库
├── advancehuawei           # 依赖引入 华为 (Huawei) 适配库
├── advancehonor            # 依赖引入 荣耀 (Honor) 适配库
│
├── doc                     # 项目相关集成文档
├── build.gradle            # 项目根构建文件
├── settings.gradle         # 模块依赖配置文件
└── publish.gradle          # SDK 发布/打包相关配置
```