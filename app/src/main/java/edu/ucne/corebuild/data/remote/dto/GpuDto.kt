package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class GpuDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val chipset: String,
    val vram: String,
    val tipoVram: String,
    val busMemoria: String,
    val frecuenciaBase: String,
    val frecuenciaBoost: String,
    val consumoWatts: Int,
    val fuenteRecomendada: String,
    val conectoresEnergia: String,
    val versionPcie: String,
    val precioUsd: Double,
    val descripcion: String
)

fun GpuDto.toDomain(): Component.GPU {
    return Component.GPU(
        id = id,
        name = nombre,
        brand = marca,
        chipset = chipset,
        vram = vram,
        vramType = tipoVram,
        recommendedWattage = consumoWatts.toString(),
        pcieInterface = versionPcie,
        price = precioUsd,
        description = descripcion
    )
}
