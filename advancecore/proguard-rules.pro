# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.ss.sys.ces.* {*;}
-keep class com.ss.android.**{*;}
-keep class com.pgl.sys.ces.* {*;}

-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}
-keep class com.bun.miitmdid.core.** {*;}
-keep class com.advance.supplier.** {*;}

#oaid 混淆部分
-keep class com.bun.miitmdid.** {*;}
-dontwarn XI.**
-keep class  XI.** {*;}

-keep interface com.advance.**{*;}
-keep class com.advance.**{
      public <methods>;#保持该类下所有的共有方法不被混淆
      public *;#保持该类下所有的共有内容不被混淆
      public <init>(java.lang.String);#保持该类的String类型的构造方法
      abstract <methods>; #保持抽象方法
      protected <methods>;
}

#使用Java的基本规则来保护特定类不被混淆，比如用extends，implement等这些Java规则，如下：保持Android底层组件和类不要混淆
  -keep public class * extends android.app.Activity
  -keep public class * extends android.app.Application
  -keep public class * extends android.app.Service
  -keep public class * extends android.content.BroadcastReceiver
  -keep public class * extends android.content.ContentProvider
  -keep public class * extends android.view.View

  -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }

    -keep class * implements Android.os.Parcelable {
    	# 保持Parcelable不被混淆
        public static final Android.os.Parcelable$Creator *;
    }

