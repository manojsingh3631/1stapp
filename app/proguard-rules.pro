# General
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# Retrofit / OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.managers.* { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Data models (keep all fields/ctors for serialization)
-keep class com.financeapp.calculator.data.model.** { *; }
-keep class com.financeapp.calculator.data.api.dto.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
