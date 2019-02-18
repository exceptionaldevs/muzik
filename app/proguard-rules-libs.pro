# Proguard for libraries used by us.
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-dontwarn rx.internal.util.unsafe.**

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

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-keep class android.support.v8.renderscript.** { *; }

# vector-compat
-keep class com.wnafee.vector.** { *; }

-keepattributes SourceFile,LineNumberTable

-keep public class * extends java.lang.Exception