package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: Int?,
    val componentes: List<String>, // IDs o JSON de componentes
    val precioTotal: Double,
    val fecha: Long,
    val estado: String
)
