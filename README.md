# CryptoVault v19 — release candidate security increment

This increment wires the architecture toward a release candidate:
- exact Wallet Core Bitcoin legacy SigningInput shape based on official Android tests
- Bitcoin signing result/txid model
- dApp WalletConnect request policy
- phishing / origin checks
- explicit confirmation boundary
- production release checklist

The app must still be independently audited before real-money deployment.

## Сборка APK непосредственно на Android

Подробная русская инструкция находится в `BUILD_ON_ANDROID_RU.md`. Быстрый вариант — AndroidIDE: открыть проект как существующий Gradle project и выполнить `./gradlew assembleDebug`. Для Trust Wallet Core требуется GitHub Packages credential (`gpr.user`/`gpr.key`) в локальном `local.properties`.
