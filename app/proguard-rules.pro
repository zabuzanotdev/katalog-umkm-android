# Aturan R8 untuk build release (minify dimatikan, file ini disiapkan
# supaya aman kalau nanti minify diaktifkan).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.zaacoo.katalog.**$$serializer { *; }
-keepclassmembers class com.zaacoo.katalog.** {
    *** Companion;
}
-keepclasseswithmembers class com.zaacoo.katalog.** {
    kotlinx.serialization.KSerializer serializer(...);
}
