# ExpenseMate Proguard Rules

# Keep Room generated code and entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
}

# Keep DataStore & Kotlin Coroutines
-keepattributes *Annotation*,InnerClasses,EnclosingMethod
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep WorkManager workers
-keep public class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep public class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Compose rules
-keep class androidx.compose.material3.** { *; }
