package com.cryptovault

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class PreparedEvmTransfer(
    val network: Network, val from: String, val to: String, val amountWei: BigInteger,
    val nonce: Long, val gasPriceWei: BigInteger, val gasLimit: BigInteger,
    val estimatedFeeWei: BigInteger, val dataHex: String = "0x",
    val tokenContract: String? = null, val tokenAmount: BigInteger? = null,
    /** Actual ERC-20 recipient (for native transfers this is null; `to` is the recipient). */
    val recipient: String? = null
) {
    fun feeNative(): BigDecimal =
        BigDecimal(estimatedFeeWei).divide(BigDecimal.TEN.pow(18), 18, RoundingMode.DOWN)
}

class RealTransactionService(private val rpc: RpcClient = RpcClient()) {
    fun prepareNative(network: Network, from: String, to: String, amountWei: BigInteger): PreparedEvmTransfer {
        AddressValidation.requireEvm(to)
        require(amountWei > BigInteger.ZERO) { "Сумма должна быть больше нуля" }
        val nonce = rpc.nonce(network, from)
        val gasPrice = rpc.gasPrice(network)
        val gas = rpc.estimateGas(network, from, to, amountWei).coerceAtLeast(BigInteger.valueOf(21_000L))
        return PreparedEvmTransfer(network, from, to, amountWei, nonce, gasPrice, gas, gas * gasPrice)
    }

    fun prepareErc20(network: Network, from: String, tokenContract: String,
                     recipient: String, amountBaseUnits: BigInteger): PreparedEvmTransfer {
        AddressValidation.requireEvm(tokenContract)
        AddressValidation.requireEvm(recipient)
        require(amountBaseUnits > BigInteger.ZERO) { "Сумма должна быть больше нуля" }
        val data = Erc20Codec.transferCall(recipient, amountBaseUnits)
        val nonce = rpc.nonce(network, from)
        val gasPrice = rpc.gasPrice(network)
        val gas = rpc.estimateGas(network, from, tokenContract, BigInteger.ZERO, data)
        return PreparedEvmTransfer(
            network = network,
            from = from,
            to = tokenContract,               // contract address (tx.to)
            amountWei = BigInteger.ZERO,
            nonce = nonce,
            gasPriceWei = gasPrice,
            gasLimit = gas,
            estimatedFeeWei = gas * gasPrice,
            dataHex = data,
            tokenContract = tokenContract,
            tokenAmount = amountBaseUnits,
            recipient = recipient             // actual token receiver
        )
    }

    fun signAndBroadcast(prepared: PreparedEvmTransfer, mnemonic: String,
                         explicitUserConfirmation: Boolean): String {
        check(explicitUserConfirmation) { "Broadcast заблокирован без явного подтверждения." }
        val privateKey = WalletCoreService.ethereumPrivateKey(mnemonic)
        return try {
            val signed = if (prepared.tokenContract == null) {
                EthereumSigner.signNativeTransfer(privateKey, prepared.network.chainId,
                    prepared.gasPriceWei, prepared.gasLimit, prepared.to, prepared.amountWei, prepared.nonce)
            } else {
                val tokenRecipient = prepared.recipient
                    ?: error("ERC-20 recipient is missing in prepared transfer")
                EthereumSigner.signErc20Transfer(
                    privateKey,
                    prepared.network.chainId,
                    prepared.gasPriceWei,
                    prepared.gasLimit,
                    prepared.tokenContract,
                    tokenRecipient,
                    prepared.tokenAmount ?: error("Нет token amount"),
                    prepared.nonce
                )
            }
            rpc.sendRaw(prepared.network, "0x" + signed.joinToString("") { "%02x".format(it) })
        } finally {
            privateKey.fill(0)
        }
    }

    fun receipt(network: Network, txHash: String): TransactionReceipt? =
        rpc.transactionReceipt(network, txHash)
}
