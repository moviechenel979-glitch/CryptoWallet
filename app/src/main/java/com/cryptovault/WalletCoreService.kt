package com.cryptovault

import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet

data class WalletAddresses(val btc: String, val eth: String, val bnb: String)

object WalletCoreService {
    init { System.loadLibrary("TrustWalletCore") }

    fun create(): Pair<String, WalletAddresses> {
        val wallet = HDWallet(128, "")
        return wallet.mnemonic() to derive(wallet)
    }

    fun restore(mnemonic: String): WalletAddresses {
        require(mnemonic.trim().split(Regex("\\s+")).size >= 12) { "Неверная seed-фраза" }
        return derive(HDWallet(mnemonic.trim(), ""))
    }

    private fun derive(wallet: HDWallet) = WalletAddresses(
        wallet.getAddressForCoin(CoinType.BITCOIN),
        wallet.getAddressForCoin(CoinType.ETHEREUM),
        wallet.getAddressForCoin(CoinType.SMARTCHAIN)
    )

    fun bitcoinPrivateKey(mnemonic: String): ByteArray =
        HDWallet(mnemonic, "").getKeyForCoin(CoinType.BITCOIN).data()

    fun ethereumPrivateKey(mnemonic: String): ByteArray {
        val wallet = HDWallet(mnemonic, "")
        return wallet.getKeyForCoin(CoinType.ETHEREUM).data()
    }
}
