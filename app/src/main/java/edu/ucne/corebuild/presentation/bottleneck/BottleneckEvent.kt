package edu.ucne.corebuild.presentation.bottleneck

import edu.ucne.corebuild.domain.model.Component

sealed interface BottleneckEvent {
    data class SelectCpu(val cpu: Component.CPU?) : BottleneckEvent
    data class SelectGpu(val gpu: Component.GPU?) : BottleneckEvent
    data class SelectResolution(val resolution: String) : BottleneckEvent
}
