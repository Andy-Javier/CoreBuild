package edu.ucne.corebuild.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CartItemWithComponent(
    @Embedded val cart: CartEntity,
    @Relation(
        parentColumn = "componentId",
        entityColumn = "id"
    )
    val component: ComponentEntity
)
