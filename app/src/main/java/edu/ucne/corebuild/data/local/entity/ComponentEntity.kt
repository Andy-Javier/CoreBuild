package edu.ucne.corebuild.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val componentType: String, // CPU, GPU, Motherboard, RAM, PSU
    
    val brand: String? = null,
    
    // CPU fields
    val socket: String? = null,
    val generation: String? = null,
    val cores: Int? = null,
    val threads: Int? = null,
    val baseClock: String? = null,
    val boostClock: String? = null,
    val tdp: String? = null,
    val cache: String? = null,
    val integratedGraphics: String? = null,
    
    // GPU fields
    val chipset: String? = null,
    val vram: String? = null,
    val vramType: String? = null,
    val recommendedWattage: String? = null,
    val pcieInterface: String? = null,
    val length: String? = null,
    
    // Motherboard fields
    val format: String? = null,
    val motherboardRamType: String? = null,
    val maxRamCapacity: String? = null,
    val slotsM2: Int? = null,
    
    // RAM fields
    val ramCapacity: String? = null,
    val ramSpeed: String? = null,
    val ramLatency: String? = null,
    val ramType: String? = null,
    val voltage: String? = null,
    val hasRGB: Boolean? = null,
    
    // PSU fields
    val psuWattage: Int? = null,
    val psuCertification: String? = null,
    val psuModularity: String? = null,
    val fanSize: String? = null,
    val protection: String? = null
)
