package com.cryptovault

/** Deterministic UTXO selection and review model. Wallet Core calculates the final serialized fee. */
data class BitcoinUtxo(
    val txid: String,
    val vout: Int,
    val valueSats: Long,
    val scriptPubKeyHex: String
)

data class BitcoinSendPlan(
    val network: BitcoinNetwork,
    val from: String,
    val to: String,
    val amountSats: Long,
    val feeSats: Long,
    val feeRateSatVb: Long,
    val changeSats: Long,
    val inputs: List<BitcoinUtxo>
)

class BitcoinTransactionService {
    fun prepare(
        network: BitcoinNetwork,
        from: String,
        to: String,
        amountSats: Long,
        feeSats: Long,
        feeRateSatVb: Long = 1,
        utxos: List<BitcoinUtxo>
    ): BitcoinSendPlan {
        require(amountSats > 0) { "Сумма BTC должна быть больше 0" }
        require(feeSats > 0) { "Комиссия должна быть больше 0" }
        require(feeRateSatVb > 0) { "Fee rate должен быть больше 0" }
        require(to.isNotBlank()) { "Не указан получатель" }
        var total = 0L
        val selected = mutableListOf<BitcoinUtxo>()
        for (u in utxos.sortedBy { it.valueSats }) {
            require(u.valueSats > 0) { "Некорректный UTXO" }
            selected += u
            total = Math.addExact(total, u.valueSats)
            if (total >= amountSats + feeSats) break
        }
        require(total >= amountSats + feeSats) { "Недостаточно BTC с учётом комиссии" }
        return BitcoinSendPlan(network, from, to, amountSats, feeSats, feeRateSatVb,
            total - amountSats - feeSats, selected)
    }
}
