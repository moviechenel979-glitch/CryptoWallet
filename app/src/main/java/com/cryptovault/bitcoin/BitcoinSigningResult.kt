package com.cryptovault.bitcoin

data class BitcoinSigningResult(
    val encodedTransactionHex: String,
    val txid: String,
    val feeSats: Long
) {
    init {
        require(encodedTransactionHex.startsWith("0x") || encodedTransactionHex.matches(Regex("[0-9a-fA-F]+")))
        require(txid.length in 64..66)
        require(feeSats >= 0)
    }
}
