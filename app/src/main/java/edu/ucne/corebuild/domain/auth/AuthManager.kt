package edu.ucne.corebuild.domain.auth

import edu.ucne.corebuild.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {

    private val adminEmails = setOf(
        BuildConfig.ADMIN_EMAIL_1,
        BuildConfig.ADMIN_EMAIL_2
    )
    private val adminPassword = BuildConfig.ADMIN_PASSWORD

    fun isAdmin(email: String): Boolean =
        email.trim().lowercase() in adminEmails

    fun validateAdminCredentials(
        email: String,
        password: String
    ): Boolean =
        isAdmin(email) && password == adminPassword
}
