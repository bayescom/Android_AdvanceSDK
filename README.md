[![](https://jitpack.io/v/bayescom/Android_AdvanceSDK.svg)](https://jitpack.io/#bayescom/Android_AdvanceSDK)

# 使用说明


## SDK使用


优先参考`app`示例工程代码，具体使用文档可参考[线上文档](https://www.bayescom.com/docsify/docs/#/advance/android/start/start)


## 私有化部署改动内容

将 Advance SDK转自用，可能需要进行替换更新内容如下：

1.请求策略地址：全局搜索`AdvanceConfig`类名，修改其中的`AdvanceSdkRequestUrl` 和 `AdvanceSdkRequestUrlHttps`链接地址为自己的请求地址即可，也就是[**Stella**](https://github.com/bayescom/EasyAds-Pro_Stella)项目搭建的私有化服务的请求地址：http://yourdomain.com/stella

2.异常上报地址：全局搜索`AdvanceConfig`类名，修改其中的`SDK_ERR_REPORT_URL` 和 `SDK_ERR_REPORT_URL_HTTPS`链接地址为自己的请求地址即可，也就是[**Stella**](https://github.com/bayescom/EasyAds-Pro_Stella)项目搭建的私有化服务的事件上报请求地址：http://yourdomain.com/sdkevent

3.请求加密key：全局搜索`AdvanceSecurityCore`类名，修改其中的`sKey`成员变量为自己的key，key需要是16位字符串


## 项目结构说明

AdvanceSDK主要包含两部分，策略执行核心库（advancelib）和adapter（advancexxx）适配库。其中advancemry为Mercury变现SDK得 adapter处理库，**由于advancelib 和 advancemry共用了一套基础方法库，必须同时引入**；其他advancexxx 库按需引入项目使用。

注意：整体工程已迁移至AndroidX，所以开发者也必须基于AndroidX来引入并使用SDK。

## 已适配SDK广告位情况说明

√代表支持，-代表不支持或无对应广告位类型

| 平台      | 	开屏 | 激励视频 | 横幅(Banner)| 插屏(弹窗) | 原生模板信息流 | 原生自渲染信息流 | 全屏视频 |Draw视频信息流|是否支持bidding
|:--------|:---------------|  :---------------|  :---------------|  :---------------|  :---------------|  :---------------|:--------| :--------|   :---------------|
| 倍业mercury        |√ |√ |√ |√ |√ |√ | -|- |√
| 优量汇(腾讯) |√ |√ |√ |√ |√ |√ |√ |- |√
| 穿山甲      |√ |√ |√ |√|√|√ |√ |√ |需媒体自行申请
| 百度        |√ |√ |- | √ | √ |√| √ |- |√
| 快手        |√ |√ | - | √ | √|√ | √|√|√
| tanx        |√|√|- |√ |√ |√| -|- |√
| TapTap       |√|√|√ |√ |- |-| -|- |√
| Sigmob       |√|√|- |√ |- |√| -|- |√
| oppo       |√|√|√ |√ |√ |√| -|- |√


## 使用环境

AndroidStudio 推荐版本：Android Studio Dolphin | 2021.3.1 Patch 1

gradle 推荐版本：5.6.4

demo工程已迁移至AndroidX
