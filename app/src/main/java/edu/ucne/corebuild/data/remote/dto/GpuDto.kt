package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class GpuDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val chipset: String? = null,
    val vram: String? = null,
    val tipoVram: String? = null,
    val consumoWatts: Int? = null,
    val fuenteRecomendada: String? = null,
    val versionPcie: String? = null,
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun GpuDto.toDomain(idOverride: Int? = null): Component.GPU {
    return Component.GPU(
        id = idOverride ?: id,
        name = nombre ?: "GPU Desconocida",
        brand = marca ?: "",
        chipset = chipset ?: "",
        vram = vram ?: "",
        vramType = tipoVram ?: "",
        consumptionWatts = (consumoWatts ?: 0).toString(),
        recommendedPSU = fuenteRecomendada,
        pcieInterface = versionPcie,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
