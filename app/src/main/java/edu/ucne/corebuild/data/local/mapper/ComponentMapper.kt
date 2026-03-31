package edu.ucne.corebuild.data.local.mapper

import edu.ucne.corebuild.data.local.entity.ComponentEntity
import edu.ucne.corebuild.domain.model.Component

fun ComponentEntity.toDomain(): Component {
    return when (componentType) {
        "CPU" -> Component.CPU(
            id = id,
            name = name,
            description = description,
            price = price,
            brand = brand ?: "",
            socket = socket ?: "",
            generation = generation ?: "",
            cores = cores ?: 0,
            threads = threads ?: 0,
            baseClock = baseClock ?: "",
            boostClock = boostClock ?: "",
            tdp = tdp ?: "",
            cache = cache,
            integratedGraphics = integratedGraphics,
            category = category,
            imageUrl = imageUrl
        )
        "GPU" -> Component.GPU(
            id = id,
            name = name,
            description = description,
            price = price,
            brand = brand ?: "",
            chipset = chipset ?: "",
            vram = vram ?: "",
            vramType = vramType ?: "",
            consumptionWatts = recommendedWattage ?: "",
            recommendedPSU = recommendedPSU,
            pcieInterface = pcieInterface,
            length = length,
            category = category,
            imageUrl = imageUrl
        )
        "Motherboard" -> Component.Motherboard(
            id = id,
            name = name,
            description = description,
            price = price,
            brand = brand ?: "",
            socket = socket ?: "",
            chipset = chipset ?: "",
            format = format ?: "",
            ramType = motherboardRamType ?: "",
            maxRamCapacity = maxRamCapacity,
            slotsM2 = slotsM2,
            category = category,
            imageUrl = imageUrl
        )
        "RAM" -> Component.RAM(
            id = id,
            name = name,
            description = description,
            price = price,
            brand = brand ?: "",
            type = ramType ?: "",
            capacity = ramCapacity ?: "",
            speed = ramSpeed ?: "",
            latency = ramLatency ?: "" ,
            voltage = voltage,
            hasRGB = hasRGB,
            category = category,
            imageUrl = imageUrl
        )
        "PSU" -> Component.PSU(
            id = id,
            name = name,
            description = description,
            price = price,
            brand = brand ?: "",
            wattage = psuWattage ?: 0,
            certification = psuCertification ?: "",
            modularity = psuModularity ?: "",
            fanSize = fanSize,
            protection = protection,
            category = category,
            imageUrl = imageUrl
        )
        else -> throw IllegalArgumentException("Unknown component type: $componentType")
    }
}

fun Component.toEntity(): ComponentEntity {
    return when (this) {
        is Component.CPU -> ComponentEntity(
            id = id, name = name, description = description, price = price, category = category,
            componentType = "CPU", brand = brand, socket = socket, generation = generation,
            cores = cores, threads = threads, baseClock = baseClock, boostClock = boostClock, 
            tdp = tdp, cache = cache, integratedGraphics = integratedGraphics, imageUrl = imageUrl
        )
        is Component.GPU -> ComponentEntity(
            id = id, name = name, description = description, price = price, category = category,
            componentType = "GPU", brand = brand, chipset = chipset, vram = vram, vramType = vramType,
            recommendedWattage = consumptionWatts, recommendedPSU = recommendedPSU, 
            pcieInterface = pcieInterface, length = length, imageUrl = imageUrl
        )
        is Component.Motherboard -> ComponentEntity(
            id = id, name = name, description = description, price = price, category = category,
            componentType = "Motherboard", brand = brand, socket = socket, chipset = chipset,
            format = format, motherboardRamType = ramType, maxRamCapacity = maxRamCapacity, 
            slotsM2 = slotsM2, imageUrl = imageUrl
        )
        is Component.RAM -> ComponentEntity(
            id = id, name = name, description = description, price = price, category = category,
            componentType = "RAM", brand = brand, ramType = type, ramCapacity = capacity,
            ramSpeed = speed, ramLatency = latency, voltage = voltage, hasRGB = hasRGB, imageUrl = imageUrl
        )
        is Component.PSU -> ComponentEntity(
            id = id, name = name, description = description, price = price, category = category,
            componentType = "PSU", brand = brand, psuWattage = wattage, psuCertification = certification,
            psuModularity = modularity, fanSize = fanSize, protection = protection, imageUrl = imageUrl
        )
    }
}
