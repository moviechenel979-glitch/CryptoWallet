package com.cryptovault

object AddressValidation {
    private val evmRegex = Regex("^0x[0-9a-fA-F]{40}$")

    fun evm(address: String): Boolean = evmRegex.matches(address.trim())

    fun requireEvm(address: String) {
        require(evm(address)) { "Неверный EVM-адрес" }
    }
}
