package edu.ucne.corebuild.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.remote.dto.*
import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val dao: ComponentDao,
    @ApplicationContext private val context: Context
) {
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let {
            cm.getNetworkCapabilities(it)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }

    suspend fun syncAll() {
        if (!isOnline()) return
        syncCpus()
        syncGpus()
        syncMotherboards()
        syncRams()
        syncPsus()
    }

    private suspend fun syncCpus() {
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

    private suspend fun syncGpus() {
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

    private suspend fun syncMotherboards() {
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

    private suspend fun syncRams() {
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

    private suspend fun syncPsus() {
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

    private fun determineCpuImage(name: String, gen: String): String? {
        val search = "$name $gen".lowercase()
        return when {
            search.contains("ultra") || search.contains("285k") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774743251/imagen_2026-03-28_201410763_ex7ktj.png"
            search.contains("14900") || search.contains("14700") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746616/imagen_2026-03-28_211015385_lx6ngi.png"
            else -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741841/Ryzen_5000_waljfw.jpg"
        }
    }

    private fun determineGpuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("5090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747099/imagen_2026-03-28_211818710_uexrhy.png"
            else -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747013/imagen_2026-03-28_211525393_u32mud.png"
        }
    }

    private fun determineMotherboardImage(name: String): String? {
        val search = name.lowercase()
        
        // 1. PRIORIDAD ABSOLUTA A TUS 3 MODELOS
        if (search.contains("tomahawk") && search.contains("b550")) {
            return "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775335932/imagen_2026-04-04_165211818_aniu8s.png"
        }
        if (search.contains("steel legend") && search.contains("x570")) {
            return "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775335986/imagen_2026-04-04_165305611_pxlc39.png"
        }
        if (search.contains("tuf") && (search.contains("a620") || search.contains("620"))) {
            return "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775336077/imagen_2026-04-04_165437283_qwb80j.png"
        }
        
        // 2. Otras placas genéricas
        return when {
            search.contains("z790") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323869/imagen_2026-04-04_133100984_qcpeie.png"
            search.contains("b650") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324123/imagen_2026-04-04_133514794_xbnz0f.png"
            else -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323850/imagen_2026-04-04_133041883_kderap.png"
        }
    }

    private fun determineRamImage(name: String): String? {
        return "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789924/imagen_2026-03-29_091202718_yrikxo.png"
    }

    private fun determinePsuImage(name: String): String? {
        return "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789973/imagen_2026-03-29_091253254_ueu1cz.png"
    }
}
