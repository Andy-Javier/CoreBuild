package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class MotherboardDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val socket: String,
    val chipset: String,
    val formato: String,
    val compatibilidadCpu: String,
    val tipoRam: String,
    val velocidadRamMax: String,
    val slotsRam: String,
    val almacenamiento: String,
    val puertos: String,
    val conectividad: String?,
    val precioUsd: Double,
    val descripcion: String
)

fun MotherboardDto.toDomain(): Component.Motherboard {
    return Component.Motherboard(
        id = id,
        name = nombre,
        brand = marca,
        socket = socket,
        chipset = chipset,
        format = formato,
        ramType = tipoRam,
        maxRamCapacity = velocidadRamMax,
        price = precioUsd,
        description = descripcion
    )
}
