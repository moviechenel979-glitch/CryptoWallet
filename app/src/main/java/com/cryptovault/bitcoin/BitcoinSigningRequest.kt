package com.cryptovault.bitcoin

data class BitcoinUtxo(
    val txid: String,
    val vout: Int,
    val amountSats: Long,
    val scriptPubKeyHex: String
)

data class BitcoinSigningRequest(
    val amountSats: Long,
    val feeRateSatPerByte: Long,
    val toAddress: String,
    val changeAddress: String?,
    val utxos: List<BitcoinUtxo>
) {
    init {
        require(amountSats > 0)
        require(feeRateSatPerByte in 1..1000)
        require(toAddress.isNotBlank())
        require(utxos.isNotEmpty())
    }
}
