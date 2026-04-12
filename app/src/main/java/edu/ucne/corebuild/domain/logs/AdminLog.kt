package edu.ucne.corebuild.domain.logs

data class AdminLog(
    val id: Int = 0,
    val userEmail: String,
    val action: String,
    val componentName: String,
    val componentType: String,
    val timestamp: Long = System.currentTimeMillis()
)
