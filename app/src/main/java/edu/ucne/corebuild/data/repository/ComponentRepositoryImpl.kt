package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.remote.dto.*
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val dao: ComponentDao,
    private val remoteDataSource: RemoteDataSource
) : ComponentRepository {

    override fun getComponents(): Flow<List<Component>> {
        return dao.getComponents()
            .onStart {
                withContext(Dispatchers.IO) {
                    if (dao.getCount() < 10) {
                        refreshAllCategories()
                    }
                }
            }
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getComponentById(id: Int): Flow<Component?> {
        return dao.getComponentById(id)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun refreshAllCategories() = supervisorScope {
        launch {
            try {
                remoteDataSource.getCpus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain()
                        val imageUrl = determineCpuImage(domain.name, domain.generation)
                        domain.copy(imageUrl = imageUrl).toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }

        launch {
            try {
                remoteDataSource.getGpus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 1000)
                        val imageUrl = determineGpuImage(domain.name)
                        domain.copy(imageUrl = imageUrl).toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }

        launch {
            try {
                remoteDataSource.getMotherboards().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 2000)
                        val imageUrl = determineMotherboardImage(domain.name)
                        domain.copy(imageUrl = imageUrl).toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }

        launch {
            try {
                remoteDataSource.getRams().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 3000)
                        val imageUrl = determineRamImage(domain.name)
                        domain.copy(imageUrl = imageUrl).toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }

        launch {
            try {
                remoteDataSource.getPsus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 4000)
                        val imageUrl = determinePsuImage(domain.name)
                        domain.copy(imageUrl = imageUrl).toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
    }

    private fun determineCpuImage(name: String, gen: String): String? {
        val search = "$name $gen".lowercase()
        val nameOnly = name.lowercase()

        val amd3000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_3000_l7gbgt.jpg"
        val amd5000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741841/Ryzen_5000_waljfw.jpg"
        val amd7000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741951/Ryzen_7000_flhzjd.jpg"
        val amd9000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_9000_pwufy0.jpg"

        val intelArrowLake = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774743251/imagen_2026-03-28_201410763_ex7ktj.png"
        val intel14gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746616/imagen_2026-03-28_211015385_lx6ngi.png"
        val intel13gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746606/imagen_2026-03-28_211005856_wlezyh.png"
        val intel12gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746551/imagen_2026-03-28_210908927_dep24d.png"
        val intel11gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746405/imagen_2026-03-28_210642931_csxqqh.png"
        val intel10gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774745942/imagen_2026-03-28_205900911_jizp1k.png"

        // 1. Clasificación Intel - Mantenemos search combinado para Intel
        if (search.contains("intel") || search.contains("core")) {
            return when {
                search.contains("ultra") || search.contains("245k") || search.contains("265k") || search.contains("285k") -> intelArrowLake
                search.contains("-14") || search.contains("14th gen") || search.contains("14a gen") -> intel14gen
                search.contains("-13") || search.contains("13th gen") || search.contains("13a gen") -> intel13gen
                search.contains("-12") || search.contains("12th gen") || search.contains("12a gen") -> intel12gen
                search.contains("-11") || search.contains("11th gen") || search.contains("11a gen") -> intel11gen
                search.contains("-10") || search.contains("10th gen") || search.contains("10a gen") -> intel10gen
                else -> null
            }
        }


        if (nameOnly.contains("ryzen")) {
            val modelNumber = Regex("""\d{4}""").find(nameOnly)?.value ?: ""
            val firstDigit = modelNumber.firstOrNull()?.toString() ?: ""
            
            return when (firstDigit) {
                "9" -> amd9000
                "7" -> amd7000
                "5" -> amd5000
                "3" -> amd3000
                "4" -> {
                    // Serie 4000: 4100 y 4500 son Zen 2 (amd3000)
                    if (modelNumber == "4100" || modelNumber == "4500") amd3000 else null
                }
                else -> null
            }
        }
        
        return null
    }

    private fun determineGpuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("5090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747099/imagen_2026-03-28_211818710_uexrhy.png"
            search.contains("5060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747013/imagen_2026-03-28_211525393_u32mud.png"
            search.contains("6800") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747210/imagen_2026-03-28_212009640_a62xz9.png"
            search.contains("6600") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747183/imagen_2026-03-28_211943116_lc7xvs.png"
            else -> null
        }
    }

    private fun determineMotherboardImage(name: String): String? {
        val search = name.lowercase()
        return when {
            // MSI B550 Tomahawk
            search.contains("b550") && search.contains("tomahawk") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789871/imagen_2026-03-29_091110311_unor0y.png"
            else -> null
        }
    }

    private fun determineRamImage(name: String): String? {
        val search = name.lowercase()
        return when {
            // Corsair Vengeance RGB Pro DDR4-3600 2x16GB
            search.contains("vengeance") && search.contains("rgb") &&
                    search.contains("3600") && search.contains("2x16") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789924/imagen_2026-03-29_091202718_yrikxo.png"
            else -> null
        }
    }

    private fun determinePsuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            // ASUS ROG Thor 850P
            search.contains("rog") && search.contains("thor") && search.contains("850") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789973/imagen_2026-03-29_091253254_ueu1cz.png"
            else -> null
        }
    }
}
