# Proguard rules for TODO LIST
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.sami.todolist.backup.** { *; }
-keep class com.sami.todolist.data.model.** { *; }
-keep class com.sami.todolist.data.entity.** { *; }
