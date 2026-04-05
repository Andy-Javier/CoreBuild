package edu.ucne.corebuild.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val componentsJson: String,
    val totalPrice: Double,
    val date: Long,
    val status: String
)
