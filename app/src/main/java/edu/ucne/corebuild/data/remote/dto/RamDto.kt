package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class RamDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val tipo: String,
    val capacidadTotal: String,
    val configuracion: String,
    val velocidad: String,
    val latencia: String,
    val voltaje: String,
    val perfil: String,
    val precioUsd: Double,
    val descripcion: String
)

fun RamDto.toDomain(): Component.RAM {
    return Component.RAM(
        id = id,
        name = nombre,
        brand = marca,
        type = tipo,
        capacity = capacidadTotal,
        speed = velocidad,
        latency = latencia,
        voltage = voltaje,
        price = precioUsd,
        description = descripcion
    )
}
