package edu.ucne.corebuild.domain.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {

    private val adminEmails = setOf(
        "andernunez307@gmail.com",
        "andyjavierrd@gmail.com"
    )
    private val adminPassword = "123456"

    fun isAdmin(email: String): Boolean =
        email.trim().lowercase() in adminEmails

    fun validateAdminCredentials(
        email: String,
        password: String
    ): Boolean =
        isAdmin(email) && password == adminPassword
}
