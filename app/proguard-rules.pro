# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5

-verbose

-keepattributes *Annotation*

-keep class com.teambition.talk.BuildConfig { *; }
-keep class com.teambition.talk.entity.** { *; }
-keep class com.teambition.talk.client.data.** { *; }

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

# R.java
-keep public class com.teambition.talk.R$*{
    public static final int *;
}

#友盟
-keep public class * extends com.umeng.**
-keep class com.umeng.** { *; }
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
#http://bbs.umeng.com/thread-12797-1-1.html
#-dontwarn u.aly.**

#gson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

#support包
-dontwarn android.support.**

#support-v4
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

#appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

#support-design
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

#cardview
-keep class android.support.v7.widget.RoundRectDrawable { *; }

#retrofit
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

#otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

#Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

#Sqlite
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

# simpleframework
-dontwarn com.bea.xml.stream.**
-dontwarn org.simpleframework.xml.**
-keep class org.simpleframework.xml.**{ *; }
-keepclassmembers,allowobfuscation class * {
    @org.simpleframework.xml.* <fields>;
    @org.simpleframework.xml.* <init>(...);
}

# RxJava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}

# Stetho
-keep class com.facebook.stetho.** { *; }

#ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# ActiveAndroid
-keep class com.activeandroid.** { *; }
-keep class com.activeandroid.**.** { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

# RoundedImageView
-dontwarn com.makeramen.roundedimageview.**

#SelectableRoundedImageView
-dontwarn com.joooonho.SelectableRoundedImageView.**

#Pinyin4j
-dontwarn demo.**

#Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.exceptions.* { *; }
-keep class io.realm.internal.async.BadVersionException { *; }
-keep class io.realm.internal.OutOfMemoryError { *; }
-keep class io.realm.internal.TableSpec { *; }
-keep class io.realm.internal.ColumnType { *; }
-dontwarn javax.**
-dontwarn io.realm.**

# Parcel library
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class org.parceler.Parceler$$Parcels

#腾讯 bugly
-keep public class com.tencent.bugly.**{*;}