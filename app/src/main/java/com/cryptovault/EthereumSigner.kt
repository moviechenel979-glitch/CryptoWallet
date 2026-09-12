package com.cryptovault

import com.google.protobuf.ByteString
import java.math.BigInteger
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Ethereum

object EthereumSigner {
    private fun bytes(value: BigInteger): ByteArray {
        if (value == BigInteger.ZERO) return byteArrayOf(0)
        val raw = value.toByteArray()
        return if (raw.size > 1 && raw[0] == 0.toByte()) raw.copyOfRange(1, raw.size) else raw
    }

    fun signNativeTransfer(privateKey: ByteArray, chainId: Long, gasPriceWei: BigInteger,
                           gasLimit: BigInteger, to: String, amountWei: BigInteger, nonce: Long): ByteArray {
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(bytes(BigInteger.valueOf(chainId)))
            this.nonce = ByteString.copyFrom(bytes(BigInteger.valueOf(nonce)))
            this.gasPrice = ByteString.copyFrom(bytes(gasPriceWei))
            this.gasLimit = ByteString.copyFrom(bytes(gasLimit))
            this.toAddress = to
            this.privateKey = ByteString.copyFrom(privateKey)
            transaction = Ethereum.Transaction.newBuilder().apply {
                transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                    amount = ByteString.copyFrom(bytes(amountWei))
                }.build()
            }.build()
        }.build()
        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        check(output.error == wallet.core.jni.proto.Common.SigningError.OK) {
            "Wallet Core signing error: ${output.error}"
        }
        return output.encoded.toByteArray()
    }

    fun signEip1559NativeTransfer(privateKey: ByteArray, chainId: Long, maxPriorityFeeWei: BigInteger,
                                  maxFeeWei: BigInteger, gasLimit: BigInteger, to: String,
                                  amountWei: BigInteger, nonce: Long): ByteArray {
        Eip1559Policy.validate(BigInteger.ZERO, maxPriorityFeeWei, maxFeeWei)
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(bytes(BigInteger.valueOf(chainId)))
            this.nonce = ByteString.copyFrom(bytes(BigInteger.valueOf(nonce)))
            this.txMode = Ethereum.TransactionMode.Enveloped
            this.maxInclusionFeePerGas = ByteString.copyFrom(bytes(maxPriorityFeeWei))
            this.maxFeePerGas = ByteString.copyFrom(bytes(maxFeeWei))
            this.gasLimit = ByteString.copyFrom(bytes(gasLimit))
            this.toAddress = to
            this.privateKey = ByteString.copyFrom(privateKey)
            transaction = Ethereum.Transaction.newBuilder().apply {
                transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                    amount = ByteString.copyFrom(bytes(amountWei))
                }.build()
            }.build()
        }.build()
        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        check(output.error == wallet.core.jni.proto.Common.SigningError.OK) { "Wallet Core signing error: ${output.error}" }
        return output.encoded.toByteArray()
    }

    fun signEip1559Erc20Transfer(privateKey: ByteArray, chainId: Long, maxPriorityFeeWei: BigInteger,
                                 maxFeeWei: BigInteger, gasLimit: BigInteger, tokenContract: String,
                                 recipient: String, amountBaseUnits: BigInteger, nonce: Long): ByteArray {
        Eip1559Policy.validate(BigInteger.ZERO, maxPriorityFeeWei, maxFeeWei)
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(bytes(BigInteger.valueOf(chainId)))
            this.nonce = ByteString.copyFrom(bytes(BigInteger.valueOf(nonce)))
            this.txMode = Ethereum.TransactionMode.Enveloped
            this.maxInclusionFeePerGas = ByteString.copyFrom(bytes(maxPriorityFeeWei))
            this.maxFeePerGas = ByteString.copyFrom(bytes(maxFeeWei))
            this.gasLimit = ByteString.copyFrom(bytes(gasLimit))
            this.toAddress = tokenContract
            this.privateKey = ByteString.copyFrom(privateKey)
            transaction = Ethereum.Transaction.newBuilder().apply {
                erc20Transfer = Ethereum.Transaction.ERC20Transfer.newBuilder().apply {
                    to = recipient
                    amount = ByteString.copyFrom(bytes(amountBaseUnits))
                }.build()
            }.build()
        }.build()
        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        check(output.error == wallet.core.jni.proto.Common.SigningError.OK) { "Wallet Core signing error: ${output.error}" }
        return output.encoded.toByteArray()
    }

    fun signErc20Transfer(privateKey: ByteArray, chainId: Long, gasPriceWei: BigInteger,
                          gasLimit: BigInteger, tokenContract: String, recipient: String,
                          amountBaseUnits: BigInteger, nonce: Long): ByteArray {
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.chainId = ByteString.copyFrom(bytes(BigInteger.valueOf(chainId)))
            this.nonce = ByteString.copyFrom(bytes(BigInteger.valueOf(nonce)))
            this.gasPrice = ByteString.copyFrom(bytes(gasPriceWei))
            this.gasLimit = ByteString.copyFrom(bytes(gasLimit))
            this.toAddress = tokenContract
            this.privateKey = ByteString.copyFrom(privateKey)
            transaction = Ethereum.Transaction.newBuilder().apply {
                erc20Transfer = Ethereum.Transaction.ERC20Transfer.newBuilder().apply {
                    to = recipient
                    amount = ByteString.copyFrom(bytes(amountBaseUnits))
                }.build()
            }.build()
        }.build()
        val output = AnySigner.sign(input, CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        check(output.error == wallet.core.jni.proto.Common.SigningError.OK) {
            "Wallet Core signing error: ${output.error}"
        }
        return output.encoded.toByteArray()
    }
}
