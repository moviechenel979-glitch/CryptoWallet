# CryptoVault production notes

See **[PRODUCTION_CHECKLIST.md](./PRODUCTION_CHECKLIST.md)** for the full status matrix.

## Signed release secrets

| Secret | Purpose |
|--------|---------|
| `GPR_USER` / `GPR_KEY` | Trust Wallet Core (GitHub Packages) |
| `RELEASE_KEYSTORE_BASE64` | base64-encoded keystore |
| `RELEASE_STORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias |
| `RELEASE_KEY_PASSWORD` | key password |

## Install

1. Download `cryptovault-debug-apk` or `cryptovault-release-apk` from Actions artifacts.
2. Prefer **testnet (Sepolia)** until external audit is done.
3. Never share seed phrase or keystore passwords.

## Disclaimer

Code hardening ≠ security audit. Do not store significant funds until §6 of the checklist is complete.
