package edu.ucne.corebuild.data.local.mapper

import edu.ucne.corebuild.data.local.entity.OrderEntity
import edu.ucne.corebuild.data.remote.dto.OrderDto
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.model.Order
import edu.ucne.corebuild.domain.model.OrderStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

private val json = Json { 
    ignoreUnknownKeys = true 
    classDiscriminator = "component_class" 
}

fun OrderEntity.toDomain(): Order {
    return Order(
        id = id ?: 0,
        components = try {
            json.decodeFromString<List<Component>>(componentsJson)
        } catch (e: Exception) {
            emptyList()
        },
        totalPrice = totalPrice,
        date = Date(date),
        status = try { OrderStatus.valueOf(status) } catch (e: Exception) { OrderStatus.CREATED }
    )
}

fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        id = if (id == 0) null else id,
        componentsJson = json.encodeToString(components),
        totalPrice = totalPrice,
        date = date.time,
        status = status.name
    )
}

fun OrderDto.toDomain(): Order {
    return Order(
        id = id ?: 0,
        components = emptyList(),
        totalPrice = precioTotal,
        date = Date(fecha),
        status = try { OrderStatus.valueOf(estado) } catch (e: Exception) { OrderStatus.CREATED }
    )
}

fun Order.toDto(): OrderDto {
    return OrderDto(
        id = if (id == 0) null else id,
        componentes = components.map { it.id.toString() },
        precioTotal = totalPrice,
        fecha = date.time,
        estado = status.name
    )
}
