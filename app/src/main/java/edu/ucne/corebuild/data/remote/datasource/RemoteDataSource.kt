package edu.ucne.corebuild.data.remote.datasource

import edu.ucne.corebuild.data.remote.api.CoreBuildApi
import edu.ucne.corebuild.data.remote.dto.*
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: CoreBuildApi
) {
    suspend fun getCpus(): Result<List<CpuDto>> = runCatching { api.getCpus() }
    suspend fun getCpu(id: Int): Result<CpuDto> = runCatching { api.getCpu(id) }

    suspend fun getGpus(): Result<List<GpuDto>> = runCatching { api.getGpus() }
    suspend fun getGpu(id: Int): Result<GpuDto> = runCatching { api.getGpu(id) }

    suspend fun getMotherboards(): Result<List<MotherboardDto>> = runCatching { api.getMotherboards() }
    suspend fun getMotherboard(id: Int): Result<MotherboardDto> = runCatching { api.getMotherboard(id) }

    suspend fun getRams(): Result<List<RamDto>> = runCatching { api.getRams() }
    suspend fun getRam(id: Int): Result<RamDto> = runCatching { api.getRam(id) }

    suspend fun getPsus(): Result<List<PsuDto>> = runCatching { api.getPsus() }
    suspend fun getPsu(id: Int): Result<PsuDto> = runCatching { api.getPsu(id) }
}
