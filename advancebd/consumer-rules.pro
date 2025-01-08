
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-ignorewarnings

-keep class com.baidu.mobads.** { *; }
-keep class com.bun.miitmdid.core.** {*;}

-keep class com.style.widget.** {*;}
-keep class com.component.** {*;}

-keep class com.baidu.ad.magic.flute.** {*;}
-keep class com.baidu.mobstat.forbes.** {*;}

#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}


#FBReader native层调用java代码配置
-keep class org.geometerplus.zlibrary.core.library.ZLibrary {*;}
-keep class org.geometerplus.zlibrary.core.filesystem.ZLFile {*;}
-keep interface org.geometerplus.zlibrary.text.model.ZLTextModel {*;}
-keep class org.geometerplus.zlibrary.text.model.CachedCharStorageException {*;}
-keep class org.geometerplus.zlibrary.core.encodings.Encoding {*;}
-keep class org.geometerplus.zlibrary.core.encodings.EncodingConverter {*;}
-keep class org.geometerplus.zlibrary.core.encodings.JavaEncodingCollection {*;}
-keep class org.geometerplus.fbreader.formats.NativeFormatPlugin {*;}
-keep class org.geometerplus.fbreader.formats.PluginCollection {*;}
-keep class org.geometerplus.fbreader.Paths {*;}
-keep class org.geometerplus.fbreader.book.Book {*;}
-keep class org.geometerplus.fbreader.book.Tag {*;}
-keep class org.geometerplus.fbreader.bookmodel.NativeBookModel {*;}
-keep public class com.baidu.novel.android.common.util.Util { *; }

#保持okhttp不被混淆
-dontwarn com.baidu.searchbox.novel.okhttp3.**
-keep class com.baidu.searchbox.novel.okhttp3.**{ *; }
-keep interface com.baidu.searchbox.novel.okhttp3.**{ *; }

#gson
-keepclassmembers,allowobfuscation class * {
 @com.baidu.searchbox.novel.gson.annotations.SerializedName <fields>;
}

# 单针对该类做keep处理
-keep public class com.baidu.android.common.util.Util { *; }

# 自定义控件
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class **.R$* {
 public static <fields>;
}

-keep class com.baidu.searchbox.reader.ReaderPluginApi{*;}
#UBC
-keep class com.baidu.ubc.* {*;}

#不能混淆RecyclerView的内部类，SDK有使用
-keep class android.support.v7.widget.RecyclerView {*;}
-keepnames class android.support.v7.widget.RecyclerView$* {
    public <fields>;
    public <methods>;
}
-keep class android.support.v7.widget.LinearLayoutManager {*;}
-keep class android.support.v7.widget.PagerSnapHelper {*;}
-keep class android.support.v4.view.ViewCompat {*;}
-keep class android.support.v4.util.LongSparseArray {*;}
-keep class android.support.v4.util.ArraySet {*;}
-keep class android.support.v4.view.accessibility.AccessibilityNodeInfoCompat {*;}