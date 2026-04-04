package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildScoreDto(
    val score: Int,
    val label: String,
    val recommendations: List<String>
)
