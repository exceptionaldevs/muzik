# RxAndroid
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}
-dontwarn sun.misc.**


-keep interface com.exceptional.musiccore.engine.JXAudioObject { *; }
-keep interface com.exceptional.musiccore.engine.JXAudioObject$** { *; }
-keep enum com.exceptional.musiccore.engine.JXAudioObject$** { *; }

-keep class com.exceptional.musiccore.engine.exoplayer.ExoPlayerPlayer
-keepclassmembers class com.exceptional.musiccore.engine.exoplayer.ExoPlayerPlayer {
	<init>(android.content.Context, android.net.Uri);
	<init>(android.content.Context, android.net.Uri,android.content.SourceResolver);
}

-keep class com.exceptional.musiccore.engine.legacymp.MediaPlayerPlayer
-keepclassmembers class com.exceptional.musiccore.engine.legacymp.MediaPlayerPlayer {
	<init>(android.content.Context, android.net.Uri);
	<init>(android.content.Context, android.net.Uri,android.content.SourceResolver);
}
# Retrofit 1.X

-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn rx.**

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

# Retrofit 2.X
## https://square.github.io/retrofit/ ##
# If in your rest service interface you use methods with Callback argument.
-keepattributes Exceptions

# If your rest service methods throw custom exceptions, because you've defined an ErrorHandler.
-keepattributes Signature

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

-keep class com.exceptional.musiccore.lfm.models.** { *; }
