package com.cryptovault

import com.google.protobuf.ByteString
import wallet.core.jni.AnySigner
import wallet.core.jni.BitcoinScript
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common.SigningError

object BitcoinSigner {
    data class Signed(val rawHex: String, val txId: String)
    data class ExactPlan(val feeSats: Long, val changeSats: Long)

    fun exactPlan(plan: BitcoinSendPlan, mnemonic: String): ExactPlan {
        val key = WalletCoreService.bitcoinPrivateKey(mnemonic)
        return try {
            val input = buildInput(plan, key)
            val p = AnySigner.plan(input.build(), CoinType.BITCOIN, Bitcoin.TransactionPlan.parser())
            ExactPlan(p.fee, p.change)
        } finally { key.fill(0) }
    }

    fun sign(plan: BitcoinSendPlan, mnemonic: String): Signed {
        val key = WalletCoreService.bitcoinPrivateKey(mnemonic)
        return try {
            val input = buildInput(plan, key)
            val planned = AnySigner.plan(input.build(), CoinType.BITCOIN, Bitcoin.TransactionPlan.parser())
            input.plan = planned
            val output = AnySigner.sign(input.build(), CoinType.BITCOIN, Bitcoin.SigningOutput.parser())
            check(output.error == SigningError.OK) { "Bitcoin signing error: ${output.error}" }
            val raw = output.encoded.toByteArray().joinToString("") { "%02x".format(it) }
            val txId = output.transaction.hash.toByteArray().reversedArray().joinToString("") { "%02x".format(it) }
            Signed(raw, txId)
        } finally { key.fill(0) }
    }

    private fun buildInput(plan: BitcoinSendPlan, key: ByteArray): Bitcoin.SigningInput.Builder {
        val input = Bitcoin.SigningInput.newBuilder()
            .setAmount(plan.amountSats)
            .setHashType(BitcoinScript.hashTypeForCoin(CoinType.BITCOIN))
            .setToAddress(plan.to)
            .setChangeAddress(plan.from)
            .setByteFee(plan.feeRateSatVb.coerceAtLeast(1).toInt())
            .setCoinType(CoinType.BITCOIN.value())
            .addPrivateKey(ByteString.copyFrom(key))
        val script = BitcoinScript.buildForAddress(plan.from, CoinType.BITCOIN).data()
        plan.inputs.forEach { u ->
            val outpoint = Bitcoin.OutPoint.newBuilder()
                .setHash(ByteString.copyFrom(hexToBytes(u.txid).reversedArray()))
                .setIndex(u.vout).setSequence(-1).build()
            input.addUtxo(Bitcoin.UnspentTransaction.newBuilder()
                .setAmount(u.valueSats).setOutPoint(outpoint)
                .setScript(ByteString.copyFrom(script)).build())
        }
        return input
    }

    private fun hexToBytes(s: String): ByteArray {
        require(s.length == 64 && s.all { it in "0123456789abcdefABCDEF" }) { "Некорректный txid" }
        return ByteArray(32) { i -> s.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
