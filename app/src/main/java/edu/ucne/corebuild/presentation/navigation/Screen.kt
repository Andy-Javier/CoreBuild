package edu.ucne.corebuild.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class Detail(val id: Int) : Screen()

    @Serializable
    data object Cart : Screen()

    @Serializable
    data object Comparator : Screen()

    @Serializable
    data object Bottleneck : Screen()

    @Serializable
    data object Favorites : Screen()

    @Serializable
    data object Orders : Screen()

    @Serializable
    data class OrderDetail(val orderId: Int) : Screen()

    @Serializable
    data object Recommendation : Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object Thanks : Screen()
}
