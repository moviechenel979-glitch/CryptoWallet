package com.cryptovault

class RpcFailover(private val endpoints: List<String>) {
    init { require(endpoints.isNotEmpty()) }

    fun healthyEndpoints(): List<String> = endpoints.filter { it.startsWith("https://") }

    fun <T> execute(action: (String) -> T): T {
        var last: Throwable? = null
        for (endpoint in healthyEndpoints()) {
            try { return action(endpoint) } catch (t: Throwable) { last = t }
        }
        throw IllegalStateException("Все RPC endpoints недоступны", last)
    }
}
