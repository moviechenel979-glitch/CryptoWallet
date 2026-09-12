# CryptoVault security gate

Перед mainnet release:
- провести внешний security audit;
- threat model для seed, backup, biometric unlock и deep links;
- запретить логирование private key/seed/подписанных payload с секретами;
- проверять chainId, recipient, token contract, spender, amount, gas и calldata;
- показывать checksum/полный адрес и предупреждение о новых адресах;
- address poisoning / phishing detection;
- симуляция транзакции до подписи;
- rate limits и lockout для unlock;
- проверка APK/dependencies/SBOM;
- backup/restore tests и migration tests;
- fuzzing парсеров QR/WalletConnect;
- только testnet до аудита;
- не встраивать API keys в исходники/ресурсы;
- не отправлять seed/private key на сервер.
