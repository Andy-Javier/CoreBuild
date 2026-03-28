package edu.ucne.corebuild.data.remote.api

import edu.ucne.corebuild.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path

interface CoreBuildApi {
    @GET("Cpu")
    suspend fun getCpus(): List<CpuDto>

    @GET("Cpu/{id}")
    suspend fun getCpu(@Path("id") id: Int): CpuDto

    @GET("Gpu")
    suspend fun getGpus(): List<GpuDto>

    @GET("Gpu/{id}")
    suspend fun getGpu(@Path("id") id: Int): GpuDto

    @GET("Motherboard")
    suspend fun getMotherboards(): List<MotherboardDto>

    @GET("Motherboard/{id}")
    suspend fun getMotherboard(@Path("id") id: Int): MotherboardDto

    @GET("Ram")
    suspend fun getRams(): List<RamDto>

    @GET("Ram/{id}")
    suspend fun getRam(@Path("id") id: Int): RamDto

    @GET("Psu")
    suspend fun getPsus(): List<PsuDto>

    @GET("Psu/{id}")
    suspend fun getPsu(@Path("id") id: Int): PsuDto
}
