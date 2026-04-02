package edu.ucne.corebuild.domain.smartbuilder

import edu.ucne.corebuild.domain.model.Component

data class SmartBuild(
    val anchorCpu: Component?,
    val anchorGpu: Component?,
    val suggested: List<Component>,
    val warnings: List<String>
)
