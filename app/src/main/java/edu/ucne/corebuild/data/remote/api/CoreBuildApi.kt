package edu.ucne.corebuild.data.remote.api

import edu.ucne.corebuild.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path

interface CoreBuildApi {
    @GET("api/Cpu")
    suspend fun getCpus(): List<CpuDto>

    @GET("api/Cpu/{id}")
    suspend fun getCpu(@Path("id") id: Int): CpuDto

    @GET("api/Gpu")
    suspend fun getGpus(): List<GpuDto>

    @GET("api/Gpu/{id}")
    suspend fun getGpu(@Path("id") id: Int): GpuDto

    @GET("api/Motherboard")
    suspend fun getMotherboards(): List<MotherboardDto>

    @GET("api/Motherboard/{id}")
    suspend fun getMotherboard(@Path("id") id: Int): MotherboardDto

    @GET("api/Ram")
    suspend fun getRams(): List<RamDto>

    @GET("api/Ram/{id}")
    suspend fun getRam(@Path("id") id: Int): RamDto

    @GET("api/Psu")
    suspend fun getPsus(): List<PsuDto>

    @GET("api/Psu/{id}")
    suspend fun getPsu(@Path("id") id: Int): PsuDto
}
