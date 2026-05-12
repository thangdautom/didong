package com.example.didong

import com.google.firebase.firestore.Exclude

data class Product(
    @get:Exclude var id: String = "",
    var userId: String = "",
    var name: String = "",
    var type: String = "",
    var price: Double = 0.0
) {
    constructor() : this("", "", "", "", 0.0)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "type" to type,
            "price" to price
        )
    }
}
