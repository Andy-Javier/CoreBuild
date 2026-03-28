package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class PsuDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val potenciaWatts: Int,
    val certificacion: String,
    val tipoModular: String,
    val eficiencia: String,
    val ventilador: String,
    val protecciones: String,
    val conectores: String,
    val precioUsd: Double,
    val descripcion: String
)

fun PsuDto.toDomain(): Component.PSU {
    return Component.PSU(
        id = id,
        name = nombre,
        brand = marca,
        wattage = potenciaWatts,
        certification = certificacion,
        modularity = tipoModular,
        fanSize = ventilador,
        protection = protecciones,
        price = precioUsd,
        description = descripcion
    )
}
