package com.cryptovault

import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
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
        } finally {
            key.fill(0)
        }
    }

    fun sign(plan: BitcoinSendPlan, mnemonic: String): Signed {
        val key = WalletCoreService.bitcoinPrivateKey(mnemonic)
        return try {
            val input = buildInput(plan, key)
            val planned = AnySigner.plan(input.build(), CoinType.BITCOIN, Bitcoin.TransactionPlan.parser())
            input.plan = planned
            val output = AnySigner.sign(input.build(), CoinType.BITCOIN, Bitcoin.SigningOutput.parser())
            check(output.error == SigningError.OK) { "Bitcoin signing error: " + output.error }
            val rawBytes = output.encoded.toByteArray()
            val raw = rawBytes.joinToString("") { b -> "%02x".format(b) }
            // Prefer transactionId from output if available; otherwise derive from encoded
            val txId = try {
                if (output.transactionId.isNotBlank()) {
                    output.transactionId
                } else {
                    // Fallback: double-SHA256 of raw tx, reversed (Bitcoin txid)
                    val digest = java.security.MessageDigest.getInstance("SHA-256")
                    val h1 = digest.digest(rawBytes)
                    digest.digest(h1).reversedArray().joinToString("") { b -> "%02x".format(b) }
                }
            } catch (_: Throwable) {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val h1 = digest.digest(rawBytes)
                digest.digest(h1).reversedArray().joinToString("") { b -> "%02x".format(b) }
            }
            Signed(raw, txId)
        } finally {
            key.fill(0)
        }
    }

    private fun buildInput(plan: BitcoinSendPlan, key: ByteArray): Bitcoin.SigningInput.Builder {
        val input = Bitcoin.SigningInput.newBuilder()
            .setAmount(plan.amountSats)
            .setHashType(BitcoinScript.hashTypeForCoin(CoinType.BITCOIN))
            .setToAddress(plan.to)
            .setChangeAddress(plan.from)
            .setByteFee(plan.feeRateSatVb.coerceAtLeast(1))
            .setCoinType(CoinType.BITCOIN.value())
            .addPrivateKey(ByteString.copyFrom(key))

        val scriptBytes = BitcoinScript.lockScriptForAddress(plan.from, CoinType.BITCOIN).data()

        plan.inputs.forEach { u ->
            val outpoint = Bitcoin.OutPoint.newBuilder()
                .setHash(ByteString.copyFrom(hexToBytes(u.txid).reversedArray()))
                .setIndex(u.vout)
                .setSequence(0xffffffff.toInt())
                .build()
            input.addUtxo(
                Bitcoin.UnspentTransaction.newBuilder()
                    .setAmount(u.valueSats)
                    .setOutPoint(outpoint)
                    .setScript(ByteString.copyFrom(scriptBytes))
                    .build()
            )
        }
        return input
    }

    private fun hexToBytes(s: String): ByteArray {
        require(s.length == 64 && s.all { it in "0123456789abcdefABCDEF" }) { "Некорректный txid" }
        return ByteArray(32) { i -> s.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
