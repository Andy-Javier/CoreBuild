package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class PsuDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val potenciaWatts: Int? = null,
    val certificacion: String? = null,
    val tipoModular: String? = null,
    val ventilador: String? = null,
    val protecciones: String? = null,
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun PsuDto.toDomain(idOverride: Int? = null): Component.PSU {
    return Component.PSU(
        id = idOverride ?: id,
        name = nombre ?: "Fuente de Poder Desconocida",
        brand = marca ?: "",
        wattage = potenciaWatts ?: 0,
        certification = certificacion ?: "",
        modularity = tipoModular ?: "",
        fanSize = ventilador,
        protection = protecciones,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
