package edu.ucne.corebuild.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val email: String,
    val password: String, // En una app real, esto debe estar hasheado
    val isLogged: Boolean = false
)
