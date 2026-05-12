package com.example.didong

data class Admin(
    var uid: String = "",
    var email: String = "",
    var role: String = "admin",
    var createdAt: Long = System.currentTimeMillis()
) {
    // Constructor mặc định cho Firebase
    constructor() : this("", "", "admin", 0)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "role" to role,
            "createdAt" to createdAt
        )
    }
}
