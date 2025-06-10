# OPPO SDK引入特殊集成说明
***
<span style="background-color: #297497"><font  color=#FFFFF> 集成Oppo SDK需要确保按照oppo文档的要求来引入SDK：</font></span>

* 1.oppo文档中的嵌入注意事项需要媒体端配置正确，主要涉及：权限说明、隐私设置、版本兼容 ，[点击查看oppo文档地址](https://u.oppomobile.com/main/index.html#/main/download)
* 2.重要：如果有接入开屏位置，需要媒体测试能正常渲染出广告，因为oppo侧要求必须在**全屏下渲染**开屏，这个要求可能会和现有开屏页面的设置不一定相符合。
* 3.重要：oppoSDK**强要求引入额外依赖项**，否则可能导致广告无法正常展示出来，建议参考下方引入以确保所有广告位能正常展示 ，gradle引入配置参考：

```
{
  		//(可选)oppo 广告SDK -- start --
 //oppo adapter引入，包含oppoSDK的aar文件（纯净版SDK）以及适配adapter内容。
   implementation("io.github.bayescom:advance-oppo:${advanceVersion}_700")
    	
 
    /oppo sdk 需要用到的三方库 --start  ---- 注意查看APP自身是否有相关重复引入，避免产生冲突 
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    implementation 'androidx.multidex:multidex:2.0.0'
    implementation 'com.squareup.okio:okio:2.5.0'
    
    implementation 'org.jetbrains.kotlin:kotlin-android-extensions-runtime:1.3.72'
    implementation 'android.arch.persistence:db-framework:1.1.1'//410版本新增
    implementation 'androidx.palette:palette:1.0.0'//490版本新增
    
    //由于可能与接入业务产生冲突，故由开发者决定依赖的版本，wechat sdk区分wechat-sdk-android与wechat-sdk-android-without-mta,由开发者决定依赖版本
    implementation "com.tencent.mm.opensdk:wechat-sdk-android-without-mta:5.5.8"//注意630版本对微信OpenSDK有依赖，接入流程可参考官网文档附录。

    implementation 'androidx.appcompat:appcompat:1.0.2'//注意500版本之后强要求依赖
    implementation 'androidx.recyclerview:recyclerview:1.0.0'//注意500版本之后强要求依赖
        //oppo sdk 需要用到的三方库 --end
       
      //(可选)oppo广告SDK -- end --

    }
