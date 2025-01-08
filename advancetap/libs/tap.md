# TapTap SDK引入特殊集成说明
***
<span style="background-color: #297497"><font  color=#FFFFF> 集成tap SDK需要进行额外的配置才可以运行，主要为以下4点：</font></span>

* 1.适配最高SDK版本，如果你的APP项目配置`minSdkVersion<21`，则需要在`AndroidManifest.xml ` 中添加 `tools:overrideLibrary` 相关内容，防止运行时检查版本不通过，具体添加内容：
```
<uses-sdk tools:overrideLibrary="com.tapsdk.tapad" />
```
* 2 **非常重要：** 开屏位置展现taptap广告，需要保证初始化AdvanceSplash时传入了Activity信息，否则影响taptap广告展现
* 3.**非常重要：参考demo引入内容**，查看tap需要的三方库和版本，与APP当前引入内容是否冲突。


gradle引入参考：

```

  //(可选)tap广告SDK -- start --
    implementation("io.github.bayescom:advance-tap:${advanceVersion}_3.16.3.20")
 
        //taptap sdk 需要用到的三方库 --start 
        implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
        implementation 'io.reactivex.rxjava2:rxjava:2.0.1'
        implementation 'com.squareup.okhttp3:okhttp:3.12.1'
        //建议使用此版本glide 
        implementation "com.github.bumptech.glide:glide:4.9.0"
        
        implementation "com.android.support:appcompat-v7:28.0.0"
        implementation "com.android.support:support-annotations:28.0.0"
        implementation "com.android.support:support-v4:28.0.0"
        implementation 'com.android.support:recyclerview-v7:28.0.0'
        //taptap sdk 需要用到的三方库 --end
    
    //引入Advance SDK --------end--------

    }
