# CryptoVault — сборка APK непосредственно на Android

## 1. Что нужно
- Android-телефон с Android 8+.
- AndroidIDE (или совместимая среда Gradle на Android).
- JDK 17.
- Android SDK Platform 35.
- Интернет для первого Gradle sync.
- GitHub Personal Access Token с минимальным правом `read:packages`, если зависимости Trust Wallet Core загружаются из GitHub Packages.

**Никому не передавайте seed, private key или GitHub token.**

## 2. Распаковать проект
Распакуйте ZIP в обычную папку, например:
`/storage/emulated/0/CryptoVault`

В AndroidIDE выберите **Open existing project** и укажите папку, где находится `settings.gradle.kts`.

## 3. GitHub Packages
Если Gradle просит авторизацию для `maven.pkg.github.com`, создайте `local.properties`:

```properties
gpr.user=ВАШ_GITHUB_USERNAME
gpr.key=ВАШ_GITHUB_TOKEN
```

Токену достаточно `read:packages`.

Не добавляйте `local.properties` в Git и не отправляйте его другим людям.

## 4. Синхронизация
В AndroidIDE выполните Gradle Sync.

Если среда предлагает установить отсутствующий SDK, установите Android SDK 35.

## 5. Debug APK
В терминале проекта:

```bash
./gradlew assembleDebug
```

Если AndroidIDE требует свой aapt2:

```bash
./gradlew -Pandroid.aapt2FromMavenOverride=$HOME/.androidide/aapt2 assembleDebug
```

APK будет здесь:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 6. Установка
Установите APK только на свой тестовый телефон. При необходимости разрешите установку приложений из используемого файлового менеджера.

## 7. Release APK
Для реального распространения используйте собственный release keystore:

```bash
keytool -genkeypair -v   -keystore cryptovault-release.jks   -alias cryptovault   -keyalg RSA   -keysize 4096   -validity 10000
```

**Храните keystore и пароль отдельно от исходников.**

После настройки `signingConfig` проекта:

```bash
./gradlew assembleRelease
```

Результат:

```text
app/build/outputs/apk/release/app-release.apk
```

## 8. Перед mainnet
1. Создайте новый тестовый кошелёк.
2. Убедитесь, что seed показывается только локально и не попадает в Logcat.
3. Проверьте получение адресов.
4. Сначала используйте тестовую сеть.
5. На mainnet отправьте минимальную сумму.
6. Проверьте transaction hash в независимом block explorer.
7. Только после этого переходите к большей сумме.

## 9. Никогда
- не вставляйте seed в чат;
- не вставляйте private key в чат;
- не храните seed в Git;
- не отключайте биометрию ради удобства;
- не используйте неизвестные APK;
- не используйте кошелёк с существенными средствами до независимого security audit.

## 10. Если сборка падает
Скопируйте сюда **только текст ошибки Gradle**. Не отправляйте `local.properties`, токены, seed или private keys.
