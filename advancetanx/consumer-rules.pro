-dontwarn com.alibaba.fastjson.**

-keep class com.alibaba.fastjson.**{*;}
-keep class com.bumptech.glide.**{*;}

-keep class com.alimm.tanx.core.utils.**{*;}
-keep class com.alimm.tanx.ui.util.**{*;}
-keep class com.alimm.tanx.ui.player.**{*;}
-keep class com.alimm.tanx.ui.player.ui.TanxPlayerView{*;}
-keep class com.alimm.tanx.ui.bridge.TanxJsBridge{*;}
#-keep class com.alimm.tanx.core.model.**{*;}
#-keep class com.alimm.tanx.core.ad.model.**{*;}
#-keep class com.alimm.tanx.core.ad.feed.FeedAdModel.**{*;}

# 自动曝光数据的防混淆
-keep public class * extends com.alimm.tanx.core.ad.model.BaseModel
    -keep class com.alimm.tanx.core.ad.model.BaseModel{*;}
-keep class * implements com.alimm.tanx.core.ad.model.IModel{
    <fields>;
    <methods>;
}

-keep class com.alimm.tanx.core.web.**{*;}
-keep class com.alimm.tanx.core.ad.event.track.interaction.InteractionUpload{*;}
-keep class com.alimm.tanx.core.ad.loader.**{*;}


-keep class com.alimm.tanx.core.ut.**{*;}
-keep class com.alimm.tanx.core.ut.impl.TanxBaseUt{*;}
-keep class com.alimm.tanx.core.ad.ad.splash.TanxSplashAd{*;}
-keep class com.alimm.tanx.ui.TanxSdk{*;}
-keep class com.alimm.tanx.ui.TanxConfig{*;}
-keep class com.alimm.tanx.ui.TanxConfig$Builder{*;}

-keep class com.alimm.tanx.ui.image.ImageConfig{*;}

-keep class com.alimm.tanx.ui.TanxSdkManager{*;}
-keep class com.alimm.tanx.core.TanxCoreManager{*;}

-keep class com.alimm.tanx.core.orange.OrangeManager{*;}
-keep class com.alimm.tanx.core.orange.OrangeSwitchConstants{*;}
-keep class com.alimm.tanx.core.TanxCoreSdk{
    <fields>;
    <methods>;
}
-keep class com.alimm.tanx.core.request.TanxAdSlot$Builder {
    *;
}

-keep public class * extends com.alimm.tanx.core.ad.bean.BaseBean
    -keep class com.alimm.tanx.core.net.bean.RequestBean{*;}
-keep class com.alimm.tanx.core.request.AdRequest{*;}
-keep class com.alimm.tanx.core.net.bean.RequestBean$Builder{*;}



-keep class com.alimm.tanx.core.TanxInitListener{*;}
-keep class com.alimm.tanx.core.net.NetWorkManager{*;}
-keep class com.alimm.tanx.core.constant.**{*;}
-keep class com.alimm.tanx.core.ad.view.**{*;}


-keep class com.alimm.tanx.core.ad.ad.splash.SplashAdCacheManager{*;}
-keep class com.alimm.tanx.ui.image.ImageLoader{*;}
-keep class com.alimm.tanx.core.ad.ad.splash.RsDownloadStatus{*;}
-keep class com.alimm.tanx.core.click.ViewClickListener{*;}
-keep class com.alimm.tanx.core.ad.ad.splash.model.SplashAdModel{*;}
-keep class com.alimm.tanx.core.ad.ad.splash.model.SplashAdModel{*;}
-keep class com.alimm.tanx.core.ad.ad.splash.TanxSplashAd{*;}

-keep public interface com.alimm.tanx.**{*;}
-keep public interface  com.alimm.tanx.core.ad.ad.splash.listener.ITanxSplashInteractionListener{*;}




# 有进程间通信,保证service相关不被混淆
-keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.os.IInterface
    # 保留Parcelable序列化类不被混淆
-keep public class * implements android.os.Parcelable {*;}

# 指定不混淆所有的JNI方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# LogField的防混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(...);
    **[] $VALUES;
}
-keep public class com.alibaba.mtl.log.model.LogField {public *;}
-keep public class com.alibaba.analytics.core.model.LogField{*;}

# 自动曝光数据的防混淆
-keep class * implements java.io.Serializable{
    <fields>;
    <methods>;
}


#2024-03-12 新增整合


# 有进程间通信,保证service相关不被混淆
-keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.os.IInterface
    # 保留Parcelable序列化类不被混淆
-keep public class * implements android.os.Parcelable {*;}

# 指定不混淆所有的JNI方法
-keepclasseswithmembernames class * {
    native <methods>;
}


-dontwarn com.alibaba.fastjson.**

-keep class com.alibaba.fastjson.**{*;}
-keep class com.bumptech.glide.**{*;}

-keep class com.alimm.tanx.**{*;}



# 自动曝光数据的防混淆
-keep class * implements java.io.Serializable{
     <fields>;
    <methods>;
}
