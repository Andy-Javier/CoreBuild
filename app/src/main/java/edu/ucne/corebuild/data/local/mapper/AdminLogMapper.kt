package edu.ucne.corebuild.data.local.mapper

import edu.ucne.corebuild.data.local.entity.AdminLogEntity
import edu.ucne.corebuild.domain.logs.AdminLog

fun AdminLogEntity.toDomain(): AdminLog {
    return AdminLog(
        id = id,
        userEmail = userEmail,
        action = action,
        componentName = componentName,
        componentType = componentType,
        timestamp = timestamp
    )
}

fun AdminLog.toEntity(): AdminLogEntity {
    return AdminLogEntity(
        id = id,
        userEmail = userEmail,
        action = action,
        componentName = componentName,
        componentType = componentType,
        timestamp = timestamp
    )
}
