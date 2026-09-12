package com.cryptovault.dapp

object WalletRequestPolicy {
    private val methods = setOf(
        "eth_sendTransaction",
        "personal_sign",
        "eth_signTypedData_v4",
        "eth_chainId",
        "eth_accounts"
    )

    fun allowed(method: String): Boolean = method in methods

    fun requiresUserConfirmation(method: String): Boolean =
        method == "eth_sendTransaction" ||
        method == "personal_sign" ||
        method == "eth_signTypedData_v4"
}
