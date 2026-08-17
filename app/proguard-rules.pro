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

# --- Custom ProGuard Rules ---

# Keep line numbers for Crashlytics/stack traces
-keepattributes SourceFile,LineNumberTable

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.health.calculator.bmi.tracker.data.model.** { *; }

# Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.AndroidEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.HiltAndroidApp class *

# Coil
-keep class coil.** { *; }

# Google Drive API
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn org.apache.http.**
-dontwarn org.ietf.jgss.**
-dontwarn sun.misc.Unsafe
-dontwarn javax.naming.**
-dontwarn javax.net.ssl.**