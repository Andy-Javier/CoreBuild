package edu.ucne.corebuild.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val action: String,
    val componentName: String,
    val componentType: String,
    val timestamp: Long
)
