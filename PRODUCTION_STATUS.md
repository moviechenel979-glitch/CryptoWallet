# Production status

## Completed architecture
- self-custody key model
- encrypted seed storage
- EVM native/ERC20 transaction pipeline
- Bitcoin transaction/signing pipeline increment
- explicit confirmation gate
- transaction policy
- biometric boundary
- EIP-1559 data model
- dApp signing review policy
- ERC20 approval warnings
- address-poisoning heuristic
- release hardening flags

## Still required before real-money release
1. independent cryptographic/security audit
2. reproducible release build
3. Android device matrix testing
4. WalletConnect/Reown project configuration and end-to-end testing
5. hardware-backed keystore verification across target devices
6. full mainnet integration tests with tiny controlled amounts
7. formal recovery/import/export test suite
8. penetration testing and dependency/SBOM review

Never put a real seed phrase into source code, Git, crash reports, screenshots, or chat.
