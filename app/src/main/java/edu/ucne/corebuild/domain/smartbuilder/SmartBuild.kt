package edu.ucne.corebuild.domain.smartbuilder

import edu.ucne.corebuild.domain.model.Component

data class SmartBuild(
    val anchorCpu: Component.CPU?,
    val anchorGpu: Component.GPU?,
    val suggested: List<Component>,
    val warnings: List<String>
)
