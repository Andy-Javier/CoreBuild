package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int?,
    val nombre: String,
    val correo: String,
    val token: String? = null
)
