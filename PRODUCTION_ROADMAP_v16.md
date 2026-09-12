# CryptoVault v16 roadmap

Completed in this increment:
- transaction policy v2;
- secret leak guard;
- EVM RPC chain health check;
- deterministic Bitcoin UTXO selection/fee/change planning;
- Bitcoin planning tests.

Still required before production:
1. Wire Bitcoin PSBT/transaction serialization and Wallet Core signing after validating the exact Android protobuf API.
2. Add real UTXO/indexer adapters with TLS, timeouts, retries and response validation.
3. Wire biometric gate directly into decrypt/sign path, not merely the UI.
4. Integrate WalletConnect/Reown SDK with projectId supplied by the operator.
5. Add malicious dApp simulation/review and spender/approval warnings.
6. Add end-to-end testnet suites.
7. Run independent security audit.
