# Wallet Core / protobuf
-keep class wallet.core.jni.proto.** { *; }
-keep class wallet.core.jni.** { *; }
-keep class wallet.core.java.** { *; }
-dontwarn com.google.protobuf.**
-dontwarn wallet.core.**

# Keep our security-critical models used via reflection-free paths
-keep class com.cryptovault.PreparedEvmTransfer { *; }
-keep class com.cryptovault.BitcoinSendPlan { *; }
-keep class com.cryptovault.WalletAddresses { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
