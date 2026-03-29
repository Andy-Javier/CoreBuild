package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class MotherboardDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val socket: String? = null,
    val chipset: String? = null,
    val formato: String? = null,
    val tipoRam: String? = null,
    val velocidadRamMax: String? = null,
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun MotherboardDto.toDomain(idOverride: Int? = null): Component.Motherboard {
    return Component.Motherboard(
        id = idOverride ?: id,
        name = nombre ?: "Placa Base Desconocida",
        brand = marca ?: "",
        socket = socket ?: "",
        chipset = chipset ?: "",
        format = formato ?: "",
        ramType = tipoRam ?: "",
        maxRamCapacity = velocidadRamMax,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
