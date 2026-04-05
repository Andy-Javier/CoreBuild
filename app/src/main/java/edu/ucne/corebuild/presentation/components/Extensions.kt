package edu.ucne.corebuild.presentation.components

fun Double.toPrice(): String =
    "$${String.format("%.2f", this)}"
