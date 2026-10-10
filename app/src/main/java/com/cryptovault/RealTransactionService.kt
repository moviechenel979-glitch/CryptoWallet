package com.cryptovault

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class PreparedEvmTransfer(
    val network: Network,
    val from: String,
    val to: String,
    val amountWei: BigInteger,
    val nonce: Long,
    val gasPriceWei: BigInteger,
    val gasLimit: BigInteger,
    val estimatedFeeWei: BigInteger,
    val dataHex: String = "0x",
    val tokenContract: String? = null,
    val tokenAmount: BigInteger? = null,
    /** Actual ERC-20 recipient (for native transfers this is null; `to` is the recipient). */
    val recipient: String? = null
) {
    fun feeNative(): BigDecimal =
        BigDecimal(estimatedFeeWei).divide(BigDecimal.TEN.pow(18), 18, RoundingMode.DOWN)
}

class RealTransactionService(private val rpc: RpcClient = RpcClient()) {
    fun prepareNative(network: Network, from: String, to: String, amountWei: BigInteger): PreparedEvmTransfer {
        AddressValidation.requireEvm(from)
        AddressValidation.requireEvm(to)
        require(amountWei > BigInteger.ZERO) { "Сумма должна быть больше нуля" }
        WalletSecurityPolicy.requireHttps(network.rpcUrl)
        val nonce = rpc.nonce(network, from)
        val gasPrice = rpc.gasPrice(network)
        val gas = rpc.estimateGas(network, from, to, amountWei).coerceAtLeast(BigInteger.valueOf(21_000L))
        require(gas <= BigInteger.valueOf(1_000_000L)) { "Gas limit слишком высокий" }
        return PreparedEvmTransfer(network, from, to, amountWei, nonce, gasPrice, gas, gas * gasPrice)
    }

    fun prepareErc20(
        network: Network,
        from: String,
        tokenContract: String,
        recipient: String,
        amountBaseUnits: BigInteger
    ): PreparedEvmTransfer {
        AddressValidation.requireEvm(from)
        AddressValidation.requireEvm(tokenContract)
        AddressValidation.requireEvm(recipient)
        require(amountBaseUnits > BigInteger.ZERO) { "Сумма должна быть больше нуля" }
        require(tokenContract.lowercase() != recipient.lowercase()) {
            "Адрес контракта токена не может совпадать с получателем"
        }
        WalletSecurityPolicy.requireHttps(network.rpcUrl)
        val data = Erc20Codec.transferCall(recipient, amountBaseUnits)
        val nonce = rpc.nonce(network, from)
        val gasPrice = rpc.gasPrice(network)
        val gas = rpc.estimateGas(network, from, tokenContract, BigInteger.ZERO, data)
        require(gas <= BigInteger.valueOf(1_000_000L)) { "Gas limit слишком высокий" }
        return PreparedEvmTransfer(
            network = network,
            from = from,
            to = tokenContract,
            amountWei = BigInteger.ZERO,
            nonce = nonce,
            gasPriceWei = gasPrice,
            gasLimit = gas,
            estimatedFeeWei = gas * gasPrice,
            dataHex = data,
            tokenContract = tokenContract,
            tokenAmount = amountBaseUnits,
            recipient = recipient
        )
    }

    fun signAndBroadcast(
        prepared: PreparedEvmTransfer,
        mnemonic: String,
        explicitUserConfirmation: Boolean
    ): String {
        check(explicitUserConfirmation) { "Broadcast заблокирован без явного подтверждения." }
        TransactionPolicyV3.requireEvm(prepared)
        WalletSecurityPolicy.requireHttps(prepared.network.rpcUrl)
        val words = mnemonic.trim().split(Regex("\s+"))
        require(words.size in 12..24) { "Некорректная seed-фраза" }

        val privateKey = WalletCoreService.ethereumPrivateKey(mnemonic)
        return try {
            val signed = if (prepared.tokenContract == null) {
                EthereumSigner.signNativeTransfer(
                    privateKey,
                    prepared.network.chainId,
                    prepared.gasPriceWei,
                    prepared.gasLimit,
                    prepared.to,
                    prepared.amountWei,
                    prepared.nonce
                )
            } else {
                val tokenRecipient = prepared.recipient
                    ?: error("ERC-20 recipient is missing in prepared transfer")
                require(tokenRecipient.lowercase() != prepared.tokenContract.lowercase()) {
                    "ERC-20: получатель не может быть адресом контракта"
                }
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
            val hex = "0x" + signed.joinToString("") { "%02x".format(it) }
            rpc.sendRaw(prepared.network, hex)
        } finally {
            privateKey.fill(0)
        }
    }

    fun receipt(network: Network, txHash: String): TransactionReceipt? =
        rpc.transactionReceipt(network, txHash)
}
