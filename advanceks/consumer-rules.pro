
#-keep class com.kwad.sdk.** { *;}
#-keep class com.ksad.download.** { *;}
#-keep class com.kwai.filedownloader.** { *;}

-keep class org.chromium.** {*;}
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.**{ *; }
-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**