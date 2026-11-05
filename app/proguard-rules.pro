# Proguard Rules for MIND

# Keep Room entities & database
-keep class com.mind.app.data.entity.** { *; }
-keep class com.mind.app.data.local.** { *; }
-keep class com.example.data.entity.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.data.dao.** { *; }

# Keep Kotlin serialization / reflection
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep models & domain objects
-keep class com.example.domain.model.** { *; }
-keep class com.example.domain.repository.** { *; }
-keep class com.example.data.backup.** { *; }
