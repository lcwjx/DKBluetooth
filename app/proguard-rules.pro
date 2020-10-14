# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/shiwu/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
#-------------------------------------------定制化区域----------------------------------------------
#---------------------------------1.实体类---------------------------------
-keep public class * extends com.jsbd.btservice.model.GsonBean

#-------------------------------------------------------------------------
-keep class com.jsbd.btservice.config.BluetoothConstants {*; }

-keep class com.jsbd.btservice.observer.extral** {*;}
-keep class com.jsbd.btservice.callback.** {*;}

-keep class com.jsbd.btservice.utils.** {*;}
-keep class com.jsbd.btservice.utils.PermissionRequestUtils$PermissionGrant {*;}

-keep class com.jsbd.btservice.db.config.** {*; }
-keep class com.jsbd.btservice.db.dao.** {*; }
-keep class com.jsbd.btservice.database.** {*; }
-keep class com.jsbd.btservice.db.gen.** {*; }

-keep class com.jsbd.btservice.service.BTBinder {*;}
-keep class com.jsbd.btservice.service.BTService {*;}

-keep class com.jsbd.btservice.IBTService { *; }
-keep class com.jsbd.btservice.IBTService$Stub { *; }

-keep class com.jsbd.btservice.IA2dpCallback { *; }
-keep class com.jsbd.btservice.IA2dpCallback$Stub { *; }

-keep class com.jsbd.btservice.IGapCallback { *; }
-keep class com.jsbd.btservice.IGapCallback$Stub { *; }

-keep class com.jsbd.btservice.IGlobalCallback { *; }
-keep class com.jsbd.btservice.IGlobalCallback$Stub { *; }

-keep class com.jsbd.btservice.IHfpCallback { *; }
-keep class com.jsbd.btservice.IHfpCallback$Stub { *; }

-keep class com.jsbd.btservice.IPbapCallback { *; }
-keep class com.jsbd.btservice.IPbapCallback$Stub { *; }

-keep class com.jsbd.btservice.A2dpMedia { *; }
-keep class com.jsbd.btservice.A2dpSetting { *; }
-keep class com.jsbd.btservice.AvrcpMedia { *; }
-keep class com.jsbd.btservice.CallLog { *; }
-keep class com.jsbd.btservice.Contact { *; }
-keep class com.jsbd.btservice.Device { *; }
-keep class com.jsbd.btservice.HandsetCall { *; }

#---------------------------------2.第三方包-------------------------------
#第三方包混淆
#yecon mtk 83**／82** framework层的基础功能库
-dontwarn com.android.ex.carousel.**
-keep class com.android.ex.carousel.** { *; }

-dontwarn com.android.**
-keep class com.android.** { *; }

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }

-dontwarn com.android.internal.policy.impl.**
-keep class com.android.internal.policy.impl.** { *; }

-dontwarn com.autochips.**
-keep class com.autochips.** { *; }

-dontwarn android.inputsource.**
-keep class android.inputsource.** { *; }

-dontwarn com.android.org.bouncycastle.**
-keep class com.android.org.bouncycastle.** { *; }

-dontwarn com.google.common.**
-keep class com.google.common.** { *; }

-dontwarn javax.**
-keep class javax.** { *; }

-dontwarn com.android.server.**
-keep class com.android.server.** { *; }

-dontwarn android.**
-keep class android.** { *; }

-dontwarn android.support.**
-keep class android.support.** { *; }

-dontwarn com.android.internal.telephony.**
-keep class com.android.internal.telephony.** { *; }

-dontwarn com.yecon.**
-keep class com.yecon.** { *; }

-dontwarn dalvik.**
-keep class dalvik.** { *; }

-dontwarn java.**
-keep class java.** { *; }

-dontwarn libcore.**
-keep class libcore.** { *; }

-dontwarn org.**
-keep class org.** { *; }

#bugly混淆 http://bugly.qq.com/androidfast
-keep public class com.tencent.bugly.** {*;}


#科大讯飞
-dontwarn com.iflytek.**
-keepattributes Signature

-keep class com.iflytek.** {*;}

#友盟统计
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep public class com.idea.fifaalarmclock.app.R$*{
    public static final int *;
}

-keep public class com.umeng.fb.ui.ThreadView {
}

-dontwarn org.apache.commons.**

-keep public class * extends com.umeng.**

-keep class com.umeng.** {*; }
-dontwarn com.umeng.**

#gson
-keepattributes Signature

-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class * implements java.io.Serializable {*;}

#两个gson解析的model仓库
-keep public class com.wills.core.handler.model.** {*;}
-keep public class com.conglaiwangluo.withme.model.** {*;}
-keep public class com.conglaiwangluo.withme.update.** {*;}
-keep public class com.conglaiwangluo.dblib.** {*;}


#greenDao 3
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static final java.lang.String TABLENAME;
}

-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static final java.lang.String TABLENAME;
}

-keep class **$Properties

-keep class data.db.dao.*$Properties {
    public static <fields>;
}
-keepclassmembers class data.db.dao.** {
    public static final <fields>;
}



#PINGYIN4J
-dontwarn net.soureceforge.pinyin4j.**

-dontwarn demo.**

-keep class net.sourceforge.pinyin4j.** { *;}

-keep class demo.** { *;}

#gif-drawable
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

#微信分享SDK
-dontwarn com.tencent.mm.**
-keep class com.tencent.mm.**{*;}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

#QQ分享混淆
-dontwarn com.tencent.**
-keep class com.tencent.**{*;}

#微博分享混淆
-dontwarn com.weibo.sdk.android.WeiboDialog
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-keep public class android.net.http.SslError{
     *;
}
-keep public class android.webkit.WebViewClient{
    *;
}
-keep public class android.webkit.WebChromeClient{
    *;
}
-keep public interface android.webkit.WebChromeClient$CustomViewCallback {
    *;
}
-keep public interface android.webkit.ValueCallback {
    *;
}
-keep class * implements android.webkit.WebChromeClient {
    *;
}

#百度定位
-dontwarn com.baidu.**
-keep class com.baidu.** { *; }

#avos代码混淆
-keepattributes Signature
-dontwarn com.jcraft.jzlib.**
-keep class com.jcraft.jzlib.**  { *;}

-dontwarn sun.misc.**
-keep class sun.misc.** { *;}

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *;}

-dontwarn sun.security.**
-keep class sun.security.** { *; }

-dontwarn com.google.**
-keep class com.google.** { *;}

-dontwarn com.avos.**
-keep class com.avos.** { *;}

-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

-dontwarn org.apache.**
-keep class org.apache.** { *;}

-dontwarn org.jivesoftware.smack.**
-keep class org.jivesoftware.smack.** { *;}

-dontwarn com.loopj.**
-keep class com.loopj.** { *;}

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep interface com.squareup.okhttp.** { *; }

-dontwarn okio.**

-dontwarn org.xbill.**
-keep class org.xbill.** { *;}

-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**

-ignorewarnings
-dontwarn com.alibaba.**
-keep class com.alibaba.**
-keepclassmembers class com.alibaba.** {
    *;
}
-keep class com.taobao.**
-keepclassmembers class com.taobao.** {
    *;
}

-dontwarn com.google.common.**
-dontwarn com.amap.api.**
-dontwarn net.jcip.annotations.**

-keepattributes Annotation,EnclosingMethod,Signature,InnerClasses

-keep class com.duanqu.**
-keepclassmembers class com.duanqu.** {
    *;
}

-dontwarn org.apache.http.**
-keepclassmembers class org.apache.http.** {
    *;
}
-dontwarn com.taobao.update.**

-dontwarn android.util.**

-dontwarn com.google.auto.factory.**

-dontwarn com.taobao.tae.sdk.callback.**

-ignorewarnings
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }

#广告平台
-keep class com.talkingdata.sdk.** {*;}
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.** {  public protected *;}

#高德地图
#定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
#2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}


#网易云信
-dontwarn com.netease.**
-keep class com.netease.** {*;}
-keep interface com.netease.** {*;}
-keepclassmembers class com.netease.** {
    *;
}

-dontwarn com.conglai.netease.**
-keep class com.conglai.netease.** {*;}
-keep interface com.conglai.netease.** {*;}
-keepclassmembers class com.conglai.netease.** {
    *;
}

#alipay
#-libraryjars libs/alipaySDK-20161009.jar

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}



#-------------------------------------------------------------------------

#---------------------------------3.与js互相调用的类------------------------



#-------------------------------------------------------------------------

#---------------------------------4.反射相关的类和方法-----------------------

-keepattributes Signature
-keepattributes EnclosingMethod

#----------------------------------------------------------------------------
#---------------------------------------------------------------------------------------------------

#-------------------------------------------基本不用动区域--------------------------------------------
#---------------------------------基本指令区----------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2

#support v4
-keep public class android.support.v4.widget.** { *; }
-keep public class * extends android.support.v4.**

#support v7
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

#support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-dontwarn org.apache.http.**

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
#----------------------------------------------------------------------------

#---------------------------------webview------------------------------------
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}
#----------------------------------------------------------------------------
#----------------------------------------------------------------------------------