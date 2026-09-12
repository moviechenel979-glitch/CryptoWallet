package com.cryptovault

object AddressValidation {
    private val evm = Regex("^0x[0-9a-fA-F]{40}$")
    fun requireEvm(address: String) {
        require(evm.matches(address.trim())) { "Неверный EVM-адрес" }
    }
}
