# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ejimenez/Android/sdk/tools/proguard/proguard-android.txt
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

# We only want obfuscation
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes LineNumberTable
-keepattributes SourceFile

# Sdk
-keep public interface com.zendesk.** { *; }
-keep public class com.zendesk.** { *; }
-dontwarn java.awt.**

# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.app.Notification
-dontwarn android.support.**

# Gson
-keep interface com.google.gson.** { *; }
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


# OkHttp
-keep class com.squareup.okhttp3.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.* { *; }
-dontwarn okhttp3.*
-dontwarn okio.**
-dontwarn okhttp3.internal.http.*
-dontwarn okhttp3.internal.Platform
-dontwarn okhttp3.logging.HttpLoggingInterceptor

#Picasso
-dontwarn com.squareup.okhttp.**

#Evernote Job
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.util.GcmAvailableHelper

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver

#CC Objects
-keep class org.caloriecloud.android.model.** { *; }

#RN
-dontwarn com.facebook.fbui.textlayoutbuilder.proxy.StaticLayoutProxy
-dontwarn javax.annotation.**
-dontwarn okhttp3.internal.huc.*
-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep class com.facebook.react.bridge.queue.NativeRunnable { *; }
-keep class com.facebook.react.cxxbridge.ModuleRegistryHolder { *; }
-keep class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * extends com.facebook.react.bridge.NativeModule { *; }
-keepclassmembers,includedescriptorclasses class * { native <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.UIProp <fields>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }

# React Native

# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
 @com.facebook.proguard.annotations.DoNotStrip *;
 @com.facebook.common.internal.DoNotStrip *;
}

-keepclassmembers @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
 void set*(***);
 *** get*();
}

-keep class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * extends com.facebook.react.bridge.NativeModule { *; }
-keepclassmembers,includedescriptorclasses class * { native <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.UIProp <fields>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }

-keep class com.facebook.** { *; }
-dontwarn com.facebook.react.**