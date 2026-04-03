package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.entity.ComponentEntity
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.remote.dto.CpuDto
import edu.ucne.corebuild.data.remote.dto.GpuDto
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ComponentRepositoryImplTest {

    private lateinit var dao: ComponentDao
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: ComponentRepositoryImpl

    private val cpuEntity = ComponentEntity(
        id = 1,
        name = "Intel Core i3-10100F",
        description = "Desc",
        price = 70.0,
        category = "Procesador",
        componentType = "CPU",
        brand = "Intel"
    )

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        remoteDataSource = mockk(relaxed = true)

        coEvery { remoteDataSource.getCpus() } returns Result.success(emptyList())
        coEvery { remoteDataSource.getGpus() } returns Result.success(emptyList())
        coEvery { remoteDataSource.getMotherboards() } returns Result.success(emptyList())
        coEvery { remoteDataSource.getRams() } returns Result.success(emptyList())
        coEvery { remoteDataSource.getPsus() } returns Result.success(emptyList())

        every { dao.getComponents() } returns flowOf(emptyList())
        coEvery { dao.getCount() } returns 100

        // El constructor correcto tiene 2 parámetros
        repository = ComponentRepositoryImpl(dao, remoteDataSource)
    }

    @Test
    fun getComponents_emiteLista_desdeRoom() = runTest {
        every { dao.getComponents() } returns flowOf(listOf(cpuEntity))

        val result = repository.getComponents().first()

        assertEquals(1, result.size)
        assertEquals("Intel Core i3-10100F", result[0].name)
    }

    @Test
    fun getComponents_emiteVacio_cuandoRoomVacio() = runTest {
        every { dao.getComponents() } returns flowOf(emptyList())

        val result = repository.getComponents().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getComponents_llamaTodosLosEndpoints_alIniciar() = runTest {
        coEvery { dao.getCount() } returns 0
        every { dao.getComponents() } returns flowOf(emptyList())

        // Activamos el onStart del Flow
        repository.getComponents().first()

        coVerify {
            remoteDataSource.getCpus()
            remoteDataSource.getGpus()
            remoteDataSource.getMotherboards()
            remoteDataSource.getRams()
            remoteDataSource.getPsus()
        }
    }

    @Test
    fun getComponents_noFalla_cuandoAPIretornaFailure() = runTest {
        coEvery { dao.getCount() } returns 0
        coEvery { remoteDataSource.getCpus() } returns Result.failure(Exception("Sin red"))

        val result = repository.getComponents().first()

        assertNotNull(result)
    }

    @Test
    fun getComponentById_retornaComponente_cuandoExiste() = runTest {
        every { dao.getComponentById(25) } returns flowOf(cpuEntity.copy(id = 25))

        val result = repository.getComponentById(25).first()

        assertEquals(25, result?.id)
    }

    @Test
    fun getComponentById_retornaNull_cuandoNoExiste() = runTest {
        every { dao.getComponentById(999) } returns flowOf(null)

        val result = repository.getComponentById(999).first()

        assertNull(result)
    }

    @Test
    fun refreshAllCategories_guardaGPU_conIdDesplazadoEn1000() = runTest {
        coEvery { dao.getCount() } returns 0
        val gpuDto = GpuDto(id = 63, nombre = "NVIDIA RTX 5090", precioUsd = 2200.0)
        coEvery { remoteDataSource.getGpus() } returns Result.success(listOf(gpuDto))

        val slot = slot<List<ComponentEntity>>()
        coEvery { dao.insertAll(capture(slot)) } returns Unit

        repository.getComponents().first()

        val insertedGpu = slot.captured.find { it.componentType == "GPU" }
        assertEquals(1063, insertedGpu?.id)
    }

    @Test
    fun getComponents_emiteDatosDeRoom_aunqueFalleAPI() = runTest {
        coEvery { dao.getCount() } returns 0
        every { dao.getComponents() } returns flowOf(listOf(cpuEntity))
        coEvery { remoteDataSource.getCpus() } returns Result.failure(Exception("Error"))

        val result = repository.getComponents().first()

        assertEquals(1, result.size)
    }

    @Test
    fun determineCpuImage_asignaRyzen3000_paraRyzen5_3600() = runTest {
        coEvery { dao.getCount() } returns 0
        val cpuDto = CpuDto(id = 25, nombre = "AMD Ryzen 5 3600", generacion = "Zen 2", precioUsd = 100.0)
        coEvery { remoteDataSource.getCpus() } returns Result.success(listOf(cpuDto))

        val slot = slot<List<ComponentEntity>>()
        coEvery { dao.insertAll(capture(slot)) } returns Unit

        repository.getComponents().first()

        val insertedCpu = slot.captured.find { it.name == "AMD Ryzen 5 3600" }
        assertTrue(insertedCpu?.imageUrl?.contains("Ryzen_3000") == true)
    }

    @Test
    fun determineCpuImage_asignaRyzen3000_paraRyzen5_4500() = runTest {
        coEvery { dao.getCount() } returns 0
        val cpuDto = CpuDto(id = 29, nombre = "AMD Ryzen 5 4500", generacion = "Zen 2", precioUsd = 100.0)
        coEvery { remoteDataSource.getCpus() } returns Result.success(listOf(cpuDto))

        val slot = slot<List<ComponentEntity>>()
        coEvery { dao.insertAll(capture(slot)) } returns Unit

        repository.getComponents().first()

        val insertedCpu = slot.captured.find { it.name == "AMD Ryzen 5 4500" }
        assertTrue(insertedCpu?.imageUrl?.contains("Ryzen_3000") == true)
    }

    @Test
    fun determineCpuImage_asignaRyzen7000_paraRyzen5_7600() = runTest {
        coEvery { dao.getCount() } returns 0
        val cpuDto = CpuDto(id = 39, nombre = "AMD Ryzen 5 7600", generacion = "Zen 4", precioUsd = 200.0)
        coEvery { remoteDataSource.getCpus() } returns Result.success(listOf(cpuDto))

        val slot = slot<List<ComponentEntity>>()
        coEvery { dao.insertAll(capture(slot)) } returns Unit

        repository.getComponents().first()

        val insertedCpu = slot.captured.find { it.name == "AMD Ryzen 5 7600" }
        assertTrue(insertedCpu?.imageUrl?.contains("Ryzen_7000") == true)
    }

    @Test
    fun determineGpuImage_retornaNull_paraGPUSinImagen() = runTest {
        coEvery { dao.getCount() } returns 0
        val gpuDto = GpuDto(id = 50, nombre = "NVIDIA GeForce RTX 2060", precioUsd = 300.0)
        coEvery { remoteDataSource.getGpus() } returns Result.success(listOf(gpuDto))

        val slot = slot<List<ComponentEntity>>()
        coEvery { dao.insertAll(capture(slot)) } returns Unit

        repository.getComponents().first()

        val insertedGpu = slot.captured.find { it.name == "NVIDIA GeForce RTX 2060" }
        assertNull(insertedGpu?.imageUrl)
    }
}
