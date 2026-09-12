# CryptoVault v20 — Release Candidate

- Version: `1.0.0-rc20`
- Mainnet BTC signing/broadcast pipeline retained from v17.
- Added EIP-1559 Wallet Core signing methods for native EVM and ERC-20 transfers.
- Added EIP-1559 fee policy.
- Added ERC-20 unlimited approval guard.
- Added address-poisoning heuristic.
- Added HTTPS-only network policy and Android Network Security Config.
- Added release R8/shrinkResources configuration.
- Fixed BTC broadcast so network I/O is not performed on the UI thread.
- Added unit tests for EIP-1559 fee policy and ERC-20 approval decoding.
- Merged release/security documentation from v18/v19.

## Verification note
The environment used to assemble this source archive does not contain a configured Android Gradle toolchain or the user's private GitHub Packages credentials, so a clean release APK build was not falsely claimed as verified here.
