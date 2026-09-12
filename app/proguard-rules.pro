# Keep protobuf generated message metadata used by Wallet Core bindings.
-keep class wallet.core.jni.proto.** { *; }
-keep class wallet.core.jni.** { *; }
-dontwarn com.google.protobuf.**
