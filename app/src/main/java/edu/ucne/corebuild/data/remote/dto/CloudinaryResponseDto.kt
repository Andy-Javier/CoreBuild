package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudinaryResponseDto(
    @Json(name = "secure_url")
    val secureUrl: String? = null,
    @Json(name = "public_id")
    val publicId: String? = null
)
