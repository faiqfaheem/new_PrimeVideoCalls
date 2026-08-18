# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- Retrofit + Gson ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson-specific: keep model/data classes used for JSON (de)serialization
-keep class com.axis.vpn.tools.prankvideocall.**.model.** { *; }
-keep class com.axis.vpn.tools.prankvideocall.**.data.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# ---- Koin ----
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# ---- Kotlin coroutines ----
-dontwarn kotlinx.coroutines.**

# ---- CameraX ----
-dontwarn androidx.camera.**

# ---- Glide ----
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule


# ---- General: keep line numbers for crash reports ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile