package edu.ucne.corebuild.domain.model

data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val isLogged: Boolean = false
)
