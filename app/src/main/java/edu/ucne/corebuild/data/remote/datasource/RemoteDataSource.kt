package edu.ucne.corebuild.data.remote.datasource

import edu.ucne.corebuild.data.remote.api.CoreBuildApi
import edu.ucne.corebuild.data.remote.dto.*
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val api: CoreBuildApi
) {
    suspend fun getCpus(): Result<List<CpuDto>> = runCatching { api.getCpus() }
    suspend fun getCpu(id: Int): Result<CpuDto> = runCatching { api.getCpu(id) }
    suspend fun createCpu(dto: CpuDto): Result<CpuDto> = api.createCpu(dto).toResult()
    suspend fun updateCpu(id: Int, dto: CpuDto): Result<CpuDto> = api.updateCpu(id, dto).toResult()
    suspend fun deleteCpu(id: Int): Result<Unit> = api.deleteCpu(id).toUnitResult()

    suspend fun getGpus(): Result<List<GpuDto>> = runCatching { api.getGpus() }
    suspend fun getGpu(id: Int): Result<GpuDto> = runCatching { api.getGpu(id) }
    suspend fun createGpu(dto: GpuDto): Result<GpuDto> = api.createGpu(dto).toResult()
    suspend fun updateGpu(id: Int, dto: GpuDto): Result<GpuDto> = api.updateGpu(id, dto).toResult()
    suspend fun deleteGpu(id: Int): Result<Unit> = api.deleteGpu(id).toUnitResult()

    suspend fun getMotherboards(): Result<List<MotherboardDto>> = runCatching { api.getMotherboards() }
    suspend fun getMotherboard(id: Int): Result<MotherboardDto> = runCatching { api.getMotherboard(id) }
    suspend fun createMotherboard(dto: MotherboardDto): Result<MotherboardDto> = api.createMotherboard(dto).toResult()
    suspend fun updateMotherboard(id: Int, dto: MotherboardDto): Result<MotherboardDto> = api.updateMotherboard(id, dto).toResult()
    suspend fun deleteMotherboard(id: Int): Result<Unit> = api.deleteMotherboard(id).toUnitResult()

    suspend fun getRams(): Result<List<RamDto>> = runCatching { api.getRams() }
    suspend fun getRam(id: Int): Result<RamDto> = runCatching { api.getRam(id) }
    suspend fun createRam(dto: RamDto): Result<RamDto> = api.createRam(dto).toResult()
    suspend fun updateRam(id: Int, dto: RamDto): Result<RamDto> = api.updateRam(id, dto).toResult()
    suspend fun deleteRam(id: Int): Result<Unit> = api.deleteRam(id).toUnitResult()

    suspend fun getPsus(): Result<List<PsuDto>> = runCatching { api.getPsus() }
    suspend fun getPsu(id: Int): Result<PsuDto> = runCatching { api.getPsu(id) }
    suspend fun createPsu(dto: PsuDto): Result<PsuDto> = api.createPsu(dto).toResult()
    suspend fun updatePsu(id: Int, dto: PsuDto): Result<PsuDto> = api.updatePsu(id, dto).toResult()
    suspend fun deletePsu(id: Int): Result<Unit> = api.deletePsu(id).toUnitResult()

    private fun <T> Response<T>.toResult(): Result<T> {
        return if (this.isSuccessful) {
            val body = this.body()
            if (body != null) Result.success(body)
            else Result.failure(Exception("Response body is null"))
        } else {
            Result.failure(Exception(this.errorBody()?.string() ?: "Unknown error"))
        }
    }

    private fun Response<Unit>.toUnitResult(): Result<Unit> {
        return if (this.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(this.errorBody()?.string() ?: "Unknown error"))
    }
}
