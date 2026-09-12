package com.cryptovault

data class TransactionPolicyV2(
    val requireBiometric: Boolean = true,
    val requireExplicitMainnetAcknowledgement: Boolean = true,
    val requireAddressReview: Boolean = true,
    val requireFeeReview: Boolean = true
) {
    fun validate(
        network: Network,
        biometricPassed: Boolean,
        mainnetAcknowledged: Boolean,
        addressReviewed: Boolean,
        feeReviewed: Boolean
    ) {
        if (requireBiometric) check(biometricPassed) { "Требуется биометрическая/PIN-аутентификация" }
        if (requireExplicitMainnetAcknowledgement) {
            MainnetGuard.requireExplicitAcknowledgement(network, mainnetAcknowledged)
        }
        if (requireAddressReview) check(addressReviewed) { "Адрес не подтверждён" }
        if (requireFeeReview) check(feeReviewed) { "Комиссия не подтверждена" }
    }
}
