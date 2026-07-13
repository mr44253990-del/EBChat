# Add project specific ProGuard rules here
# For EBChat

# Keep data models
-keep class com.ebchat.data.model.** { *; }
-keepclassmembers class com.ebchat.data.model.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# General
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes *Annotation*

# Keep R classes
-keepclassmembers class **.R$* { public static <fields>; }
