# ProGuard rules for GhestYar

# Room Database rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Compose rules
-keep class androidx.compose.** { *; }

# Keep data models
-keep class com.iliyateam.ghestyar.data.** { *; }
