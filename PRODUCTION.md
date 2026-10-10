# CryptoVault Production Status

## What is production-hardened in code

- [x] Self-custody: seed encrypted with AES-256-GCM via Android Keystore
- [x] `allowBackup=false`, cleartext traffic disabled, FLAG_SECURE
- [x] Biometric / device credential before revealing seed
- [x] Explicit user confirmation before sign + broadcast
- [x] ERC-20 recipient separated from token contract (critical fix)
- [x] TransactionPolicyV3 invariants before every EVM signature
- [x] Private keys zeroed in `finally` after signing
- [x] HTTPS-only RPC policy
- [x] R8 minify + resource shrink on release
- [x] ProGuard keeps for Wallet Core / protobuf

## Required before real funds

- [ ] **Independent security audit** (mandatory)
- [ ] Own release keystore in GitHub Secrets (`RELEASE_KEYSTORE_BASE64`, passwords)
- [ ] Hardware-backed key attestation review on target devices
- [ ] Bug bounty / external review of Bitcoin + EVM paths
- [ ] Legal / compliance review for your jurisdiction
- [ ] Test on Sepolia / Bitcoin testnet with real device flows

## Secrets for signed release CI

| Secret | Purpose |
|--------|---------|
| `GPR_USER` / `GPR_KEY` | Trust Wallet Core packages |
| `RELEASE_KEYSTORE_BASE64` | base64 of `.jks` / `.keystore` |
| `RELEASE_STORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias |
| `RELEASE_KEY_PASSWORD` | key password |

## Disclaimer

No software wallet is "safe" without continuous audit and operational discipline.
This project is **not** a substitute for a professional audit.
