# CryptoVault — сборка APK прямо на Android

## Важно перед использованием реальных средств

CryptoVault — self-custody кошелёк. Seed/private key не должен передаваться разработчику, в чат, GitHub или в `local.properties`.

Перед первым пополнением mainnet рекомендуется сначала собрать **debug APK**, восстановить тестовый кошелёк и проверить отправку на небольшой сумме. Для публичного релиза нужен независимый security audit.

## Вариант A — AndroidIDE

AndroidIDE умеет открывать существующие Gradle Android-проекты и собирать APK непосредственно на телефоне. Официальная документация описывает `Open existing project`, синхронизацию Gradle и `assembleDebug`. Однако исходный проект AndroidIDE сейчас помечен как **не поддерживаемый**, поэтому устанавливайте APK только из доверенного источника и используйте его на свой риск.

Официальные источники:
- https://github.com/AndroidIDEOfficial/AndroidIDE
- https://docs.androidide.com/user/getting-started.html

### 1. Установить AndroidIDE

Скачайте AndroidIDE только из официального GitHub Releases / F-Droid / официального сайта.

При первом запуске установите предложенные:
- JDK 17;
- Android SDK;
- platform/build tools.

Для этого проекта нужен Android SDK 35.

### 2. Распаковать проект

Скопируйте на телефон:

`CryptoVault-v20-android-build.zip`

Распакуйте, например, в:

`/storage/emulated/0/CryptoVault`

В AndroidIDE выберите:

`Open existing project` → папка `CryptoVault`.

### 3. Дать Gradle доступ к Trust Wallet Core

Проект использует `com.trustwallet:wallet-core:4.7.2`. Пакет опубликован Trust Wallet в GitHub Packages.

Создайте GitHub Personal Access Token с минимальным правом `read:packages`.

НЕ помещайте token в исходники и НЕ коммитьте его в Git.

В корне проекта создайте файл `local.properties`:

```properties
sdk.dir=/путь/к/Android/Sdk

gpr.user=ВАШ_GITHUB_USERNAME
gpr.key=ВАШ_GITHUB_TOKEN
```

Если AndroidIDE сам задаёт `sdk.dir`, оставьте существующую строку без изменений.

Пример без реального секрета:

```properties
sdk.dir=/data/data/com.itsaky.androidide/files/usr/lib/android-sdk
gpr.user=my-github-user
gpr.key=ghp_REPLACE_ME
```

### 4. Синхронизация

В AndroidIDE дождитесь успешного Gradle Sync.

Если появляется ошибка `401/403` при загрузке `wallet-core`, почти всегда проблема в GitHub Packages credentials.

Проверьте:
- username GitHub правильный;
- token не просрочен;
- у token есть `read:packages`;
- нет лишних пробелов вокруг значения;
- token не находится в кавычках.

### 5. Сначала собрать debug APK

В терминале AndroidIDE из корня проекта:

```bash
./gradlew assembleDebug
```

Если AndroidIDE использует собственный `aapt2`, при необходимости:

```bash
./gradlew -Pandroid.aapt2FromMavenOverride=$HOME/.androidide/aapt2 assembleDebug
```

Готовый файл обычно находится здесь:

`app/build/outputs/apk/debug/app-debug.apk`

Его можно установить непосредственно из AndroidIDE.

### 6. Проверка перед mainnet

После установки:

1. Создайте новый кошелёк.
2. Запишите seed **только офлайн**.
3. Убедитесь, что адреса BTC/ETH/BNB отображаются правильно.
4. Перезапустите приложение.
5. Проверьте, что кошелёк восстанавливается.
6. На testnet проверьте receive/send.
7. Проверьте экран review перед подписью.
8. Проверьте, что без биометрии/device credential подпись не выполняется.
9. Только после этого переходите к mainnet.

## Release APK

Для локальной release-сборки:

```bash
./gradlew assembleRelease
```

Файл:

`app/build/outputs/apk/release/app-release-unsigned.apk`

Этот APK **не подписан вашим release-ключом** и не следует распространять как официальный релиз.

## Подписать APK

Создайте собственный keystore на устройстве или, предпочтительнее, на отдельной защищённой машине.

Пример:

```bash
keytool -genkeypair \\
  -keystore cryptovault-release.jks \\
  -alias cryptovault \\
  -keyalg RSA \\
  -keysize 4096 \\
  -validity 10000
```

Затем подпишите APK через `apksigner`, входящий в Android Build Tools:

```bash
apksigner sign \\
  --ks cryptovault-release.jks \\
  --ks-key-alias cryptovault \\
  --out CryptoVault-release.apk \\
  app/build/outputs/apk/release/app-release-unsigned.apk
```

Проверка:

```bash
apksigner verify --verbose CryptoVault-release.apk
```

**Не храните keystore и пароли в GitHub или в ZIP проекта.**

## Если AndroidIDE не собирает проект

Скопируйте сюда **полный текст ошибки Gradle**. Не отправляйте:
- seed;
- private key;
- GitHub token;
- пароль keystore.

По тексту ошибки можно определить, это проблема SDK, JDK, Gradle, GitHub Packages или конкретного Kotlin-файла.

## Почему wallet-core требует credentials

Проект подключает пакет Trust Wallet Core из GitHub Packages. На странице пакета Trust Wallet опубликованы Android AAR и исходники версии 4.7.2. В репозитории уже доступна версия 4.7.3, но этот проект фиксирует 4.7.2 для воспроизводимости сборки; обновление версии следует делать отдельным проверяемым изменением.

## Что означает «готов для реальных средств»

Приложение может подписывать mainnet-транзакции только после явного действия пользователя. Автоматическая отправка средств без подтверждения не предусмотрена.

Перед реальным использованием обязательно:
- независимый security audit;
- проверка APK hash/signature;
- тест на нескольких Android-устройствах;
- проверка восстановления из seed;
- проверка BTC/EVM mainnet на малой сумме;
- проверка RPC и chain ID;
- проверка transaction simulation/review;
- резервная копия seed офлайн.
