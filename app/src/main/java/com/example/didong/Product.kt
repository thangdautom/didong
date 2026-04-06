package com.example.didong

data class Product(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val price: Double = 0.0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "type" to type,
            "price" to price
        )
    }
}
