package com.example.util

import java.security.MessageDigest

object PasswordHasher {
    fun hash(password: String, emailSalt: String): String {
        val normalized = "${emailSalt.trim().lowercase()}:$password"
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, emailSalt: String, storedHash: String): Boolean {
        return hash(password, emailSalt) == storedHash
    }
}
