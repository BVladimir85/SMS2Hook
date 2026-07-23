# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson model classes used by Room TypeConverters
-keep class com.smswebhook.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
