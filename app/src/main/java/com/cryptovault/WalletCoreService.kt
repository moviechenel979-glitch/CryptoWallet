package com.cryptovault

import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet

data class WalletAddresses(val btc: String, val eth: String, val bnb: String)

object WalletCoreService {
    init {
        System.loadLibrary("TrustWalletCore")
    }

    fun create(): Pair<String, WalletAddresses> {
        val wallet = HDWallet(128, "")
        return wallet.mnemonic() to derive(wallet)
    }

    fun restore(mnemonic: String): WalletAddresses {
        val cleaned = mnemonic.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
        val words = cleaned.split(" ")
        require(words.size in 12..24) { "Seed phrase must be 12–24 words" }
        require(words.all { it.all { ch -> ch in 'a'..'z' } }) { "Invalid seed word characters" }
        return try {
            derive(HDWallet(cleaned, ""))
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid BIP39 seed phrase", e)
        }
    }

    private fun derive(wallet: HDWallet) = WalletAddresses(
        wallet.getAddressForCoin(CoinType.BITCOIN),
        wallet.getAddressForCoin(CoinType.ETHEREUM),
        wallet.getAddressForCoin(CoinType.SMARTCHAIN)
    )

    fun bitcoinPrivateKey(mnemonic: String): ByteArray =
        HDWallet(mnemonic.trim(), "").getKeyForCoin(CoinType.BITCOIN).data()

    fun ethereumPrivateKey(mnemonic: String): ByteArray =
        HDWallet(mnemonic.trim(), "").getKeyForCoin(CoinType.ETHEREUM).data()
}
