# CryptoVault — Production Checklist

Status legend: `[x]` done in this repo · `[~]` partial / needs human · `[ ]` external / cannot automate

## 1. Cryptography & keys
- [x] HD wallet via Trust Wallet Core (BIP39/BIP44)
- [x] Seed encrypted AES-256-GCM in Android Keystore
- [x] StrongBox preferred when available
- [x] Private keys zeroed after use (`fill(0)`)
- [x] Seed never sent to network / logs (`SecretLeakGuard`)
- [x] Biometric gate before seed reveal & sign
- [x] `allowBackup=false` + backup/data-extraction rules

## 2. Transaction safety
- [x] Explicit confirmation before every broadcast
- [x] ERC-20 recipient ≠ token contract (critical fix)
- [x] `TransactionPolicyV3` invariants pre-sign
- [x] HTTPS-only RPC policy
- [x] Gas limit upper bound
- [x] Mainnet acknowledgement checkbox
- [x] Default network = Sepolia (testnet-first)
- [x] Address validation (EVM format)

## 3. App hardening
- [x] `FLAG_SECURE` (no screenshots/recents leak)
- [x] Release: minify + shrink + `debuggable=false`
- [x] ProGuard keeps for Wallet Core / strip logs
- [x] Cleartext traffic disabled + network security config
- [x] Basic debugger / emulator signals in `AppIntegrity`
- [x] Version 1.0.0 + `BuildConfig.PRODUCTION`

## 4. UX / product
- [x] Modern dark UI (portfolio, send, receive, settings)
- [x] Transaction review panel before send
- [x] Danger CTA for irreversible actions
- [x] Snackbar feedback
- [x] Seed display with word indices

## 5. CI / release
- [x] GitHub Actions: debug + release APK
- [x] Release keystore via Actions secrets
- [x] Unit tests in CI
- [x] Artifact upload
- [~] Play App Signing / store listing (human)
- [~] Rotate keystore passwords after handoff (human)

## 6. External (required for real funds)
- [ ] Independent security audit
- [ ] Bug bounty
- [ ] Legal / compliance review
- [ ] Device QA matrix (Pixel / Samsung / etc.)
- [ ] Mainnet canary with dust amounts only after audit

---
**Honest line:** Items in §6 are mandatory before treating this as a production wallet for significant funds. Code hardening does not replace an audit.
