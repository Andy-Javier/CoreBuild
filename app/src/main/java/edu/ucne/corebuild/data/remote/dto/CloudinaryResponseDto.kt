package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudinaryResponseDto(
    val secure_url: String? = null,
    val public_id: String? = null
)
