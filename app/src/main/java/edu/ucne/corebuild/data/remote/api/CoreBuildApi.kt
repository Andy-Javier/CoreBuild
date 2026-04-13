package edu.ucne.corebuild.data.remote.api

import edu.ucne.corebuild.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CoreBuildApi {
    @GET("Cpu")
    suspend fun getCpus(): List<CpuDto>

    @GET("Cpu/{id}")
    suspend fun getCpu(@Path("id") id: Int): CpuDto

    @POST("Cpu")
    suspend fun createCpu(@Body dto: CpuDto): Response<CpuDto>

    @PUT("Cpu/{id}")
    suspend fun updateCpu(@Path("id") id: Int, @Body dto: CpuDto): Response<Unit>

    @DELETE("Cpu/{id}")
    suspend fun deleteCpu(@Path("id") id: Int): Response<Unit>

    @GET("Gpu")
    suspend fun getGpus(): List<GpuDto>

    @GET("Gpu/{id}")
    suspend fun getGpu(@Path("id") id: Int): GpuDto

    @POST("Gpu")
    suspend fun createGpu(@Body dto: GpuDto): Response<GpuDto>

    @PUT("Gpu/{id}")
    suspend fun updateGpu(@Path("id") id: Int, @Body dto: GpuDto): Response<Unit>

    @DELETE("Gpu/{id}")
    suspend fun deleteGpu(@Path("id") id: Int): Response<Unit>

    @GET("Motherboard")
    suspend fun getMotherboards(): List<MotherboardDto>

    @GET("Motherboard/{id}")
    suspend fun getMotherboard(@Path("id") id: Int): MotherboardDto

    @POST("Motherboard")
    suspend fun createMotherboard(@Body dto: MotherboardDto): Response<MotherboardDto>

    @PUT("Motherboard/{id}")
    suspend fun updateMotherboard(@Path("id") id: Int, @Body dto: MotherboardDto): Response<Unit>

    @DELETE("Motherboard/{id}")
    suspend fun deleteMotherboard(@Path("id") id: Int): Response<Unit>

    @GET("Ram")
    suspend fun getRams(): List<RamDto>

    @GET("Ram/{id}")
    suspend fun getRam(@Path("id") id: Int): RamDto

    @POST("Ram")
    suspend fun createRam(@Body dto: RamDto): Response<RamDto>

    @PUT("Ram/{id}")
    suspend fun updateRam(@Path("id") id: Int, @Body dto: RamDto): Response<Unit>

    @DELETE("Ram/{id}")
    suspend fun deleteRam(@Path("id") id: Int): Response<Unit>

    @GET("Psu")
    suspend fun getPsus(): List<PsuDto>

    @GET("Psu/{id}")
    suspend fun getPsu(@Path("id") id: Int): PsuDto

    @POST("Psu")
    suspend fun createPsu(@Body dto: PsuDto): Response<PsuDto>

    @PUT("Psu/{id}")
    suspend fun updatePsu(@Path("id") id: Int, @Body dto: PsuDto): Response<Unit>

    @DELETE("Psu/{id}")
    suspend fun deletePsu(@Path("id") id: Int): Response<Unit>
}
