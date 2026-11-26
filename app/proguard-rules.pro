# Reglas de R8/ProGuard para Spentz Trackr (Fase 7).
# Hilt, Room, OkHttp y WorkManager traen consumer rules; aquí van las de
# Retrofit + kotlinx.serialization, que se resuelven por reflexión/generics.

# Retrofit: conservar anotaciones y firmas genéricas de las interfaces.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Suspension functions usadas por Retrofit.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# kotlinx.serialization: serializers generados por el plugin.
-keepclassmembers class **$$serializer {
    *** descriptor;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Enums persistidos por nombre (Room) y DTOs tolerantes a campos nuevos.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# No romper las clases de datos usadas por reflexión de librerías.
-dontwarn javax.annotation.**
-dontwarn kotlinx.serialization.**
# Anotaciones de Error Prone que arrastra Tink (security-crypto) y no se empaquetan.
-dontwarn com.google.errorprone.annotations.**
