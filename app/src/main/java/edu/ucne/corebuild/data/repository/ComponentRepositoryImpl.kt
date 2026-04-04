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

        // URLs Cloudinary - AMD
        val amd3000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_3000_l7gbgt.jpg"
        val amd4000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741851/Ryzen_4000_isyqkf.png"
        val amd5000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741841/Ryzen_5000_waljfw.jpg"
        val amd7000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741951/Ryzen_7000_flhzjd.jpg"
        val amd9000 = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_9000_pwufy0.jpg"

        // URLs Cloudinary - Intel
        val intelArrowLake = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774743251/imagen_2026-03-28_201410763_ex7ktj.png"
        val intel14gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746616/imagen_2026-03-28_211015385_lx6ngi.png"
        val intel13gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746606/imagen_2026-03-28_211005856_wlezyh.png"
        val intel12gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746551/imagen_2026-03-28_210908927_dep24d.png"
        val intel11gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746405/imagen_2026-03-28_210642931_csxqqh.png"
        val intel10gen = "https://res.cloudinary.com/dsnaidobx/image/upload/v1774745942/imagen_2026-03-28_205900911_jizp1k.png"

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
            // NVIDIA 50 Series
            search.contains("5090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747099/imagen_2026-03-28_211818710_uexrhy.png"
            search.contains("5070") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319604/imagen_2026-04-04_122000988_zsyi4c.png"
            search.contains("5070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319562/imagen_2026-04-04_121918968_ribthz.png"
            search.contains("5060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747013/imagen_2026-03-28_211525393_u32mud.png"
            
            // NVIDIA 40 Series
            search.contains("4090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319628/imagen_2026-04-04_121453805_khsde8.png"
            search.contains("4080") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319057/imagen_2026-04-04_121054768_eczizd.png"
            search.contains("4070") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319000/imagen_2026-04-04_120957236_g7suia.png"
            search.contains("4060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318925/imagen_2026-04-04_120841799_ldb57h.png"
            search.contains("4060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319927/imagen_2026-04-04_120757367_alvgog.png"
            
            // NVIDIA 30 Series
            search.contains("3080") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319882/imagen_2026-04-04_122438639_bqblak.png"
            search.contains("3070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318695/imagen_2026-04-04_120452312_r7q4vc.png"
            search.contains("3060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318591/imagen_2026-04-04_120308449_wu6k4o.png"
            search.contains("3060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318574/imagen_2026-04-04_120251372_b9hkw8.png"
            
            // NVIDIA Legacy
            search.contains("2060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318509/imagen_2026-04-04_120146950_ic9o9u.png"
            search.contains("1660") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318458/imagen_2026-04-04_120055557_s9unfh.png"
            
            // AMD Radeon 9000 Series
            search.contains("9070") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321290/imagen_2026-04-04_124802651_iptoe8.png"
            search.contains("9070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321204/imagen_2026-04-04_124637444_qtjd5c.png"
            search.contains("9060") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321149/imagen_2026-04-04_124446296_f4kyeb.png"
            
            // AMD Radeon 7000 Series
            search.contains("7900") && search.contains("xtx") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321006/imagen_2026-04-04_124319703_fhclm2.png"
            search.contains("7900") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320886/imagen_2026-04-04_123949291_gnoewl.png"
            search.contains("7800") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320744/imagen_2026-04-04_123859526_lnl0b8.png"
            search.contains("7700") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320486/imagen_2026-04-04_123442364_gsmuyk.png"
            search.contains("7600") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320410/imagen_2026-04-04_123325994_diz6ap.png"
            
            // AMD Radeon 6000 Series
            search.contains("6800") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747210/imagen_2026-03-28_212009640_a62xz9.png"
            search.contains("6800") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320347/imagen_2026-04-04_123223006_uysagz.png"
            search.contains("6700") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320104/imagen_2026-04-04_122820556_t7x518.png"
            search.contains("6650") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320085/imagen_2026-04-04_122801885_n5oyrw.png"
            search.contains("6600") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747183/imagen_2026-03-28_211943116_lc7xvs.png"
            
            // AMD Radeon Legacy
            search.contains("580") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320054/imagen_2026-04-04_122730864_hc9r4l.png"
            
            else -> null
        }
    }

    private fun determineMotherboardImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("h510m") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323732/imagen_2026-04-04_132844289_flu508.png"
            search.contains("b560") && search.contains("tomahawk") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323802/imagen_2026-04-04_132954356_t4bhkn.png"
            search.contains("h610m") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323826/imagen_2026-04-04_133017815_ewgfgi.png"
            search.contains("b760") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323850/imagen_2026-04-04_133041883_kderap.png"
            search.contains("z790") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323869/imagen_2026-04-04_133100984_qcpeie.png"
            search.contains("z890") && search.contains("taichi") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324019/imagen_2026-04-04_133331320_jlk0rx.png"
            search.contains("z890") && (search.contains("aorus") || search.contains("elite")) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324063/imagen_2026-04-04_133414414_fxv3uq.png"
            search.contains("a320m") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324084/imagen_2026-04-04_133435749_txwbsy.png"
            search.contains("b450m") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324104/imagen_2026-04-04_133455443_ntubhs.png"
            search.contains("b650") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324123/imagen_2026-04-04_133514794_xbnz0f.png"
            search.contains("x670e") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324163/imagen_2026-04-04_133554465_a9lqtu.png"
            search.contains("x870e") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775324290/imagen_2026-04-04_133801691_ft6l2x.png"
            else -> null
        }
    }

    private fun determineRamImage(name: String): String? {
        val search = name.lowercase()
        return when {
            // Corsair Vengeance RGB Pro
            search.contains("vengeance") && search.contains("rgb") && (search.contains("3600") || search.contains("pro")) ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789924/imagen_2026-03-29_091202718_yrikxo.png"
            
            // Corsair Vengeance LPX
            search.contains("vengeance") && search.contains("lpx") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321782/imagen_2026-04-04_125614615_yyrryx.png"
            
            // G.Skill Ripjaws V
            search.contains("ripjaws") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321881/imagen_2026-04-04_125754161_q5zqd0.png"
            
            // Kingston Fury Beast
            search.contains("fury") && search.contains("beast") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321930/imagen_2026-04-04_125842604_acjqpw.png"
            
            // G.Skill Trident Z5 RGB
            search.contains("trident") && search.contains("z5") && search.contains("rgb") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321976/imagen_2026-04-04_125928533_pqfyev.png"
            
            // Corsair Dominator Platinum RGB
            search.contains("dominator") && search.contains("platinum") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322029/imagen_2026-04-04_130015179_mcgkxq.png"
            
            // G.SKILL Trident Z5 Royal Neo
            search.contains("trident") && (search.contains("royal") || search.contains("neo")) ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322093/imagen_2026-04-04_130126082_vylrzh.png"
                
            else -> null
        }
    }

    private fun determinePsuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            // EVGA 500 W3
            search.contains("evga") && search.contains("500") && search.contains("w3") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322689/imagen_2026-04-04_131121363_h728wq.png"
            
            // Cooler Master MWE Gold 550
            search.contains("cooler") && search.contains("master") && search.contains("mwe") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322742/imagen_2026-04-04_131213943_jzy0pz.png"
            
            // Corsair CX650M
            search.contains("corsair") && search.contains("cx") && search.contains("650") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322764/imagen_2026-04-04_131237025_bccaco.png"
            
            // MSI MPG A650GF
            search.contains("msi") && search.contains("mpg") && search.contains("650") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323138/imagen_2026-04-04_131849921_hvjzsa.png"
            
            // Seasonic Focus GX-750
            search.contains("seasonic") && search.contains("focus") && search.contains("750") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323165/imagen_2026-04-04_131917289_em55jw.png"
            
            // Corsair RM750e
            search.contains("corsair") && search.contains("rm") && search.contains("750") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323190/imagen_2026-04-04_131942824_qvigfi.png"
            
            // EVGA SuperNova 850 G6
            search.contains("evga") && search.contains("supernova") && search.contains("850") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323213/imagen_2026-04-04_132005083_f9gjjs.png"
            
            // Redragon GC-PS007-1 850W RGB
            search.contains("redragon") && search.contains("850") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322960/imagen_2026-04-04_131411212_lalmd4.png"
            
            // be quiet! Dark Power 13
            search.contains("quiet") && search.contains("dark") && search.contains("power") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323310/imagen_2026-04-04_132142547_dz8gen.png"
            
            // Corsair HX1200i
            search.contains("corsair") && search.contains("hx") && search.contains("1200") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323536/imagen_2026-04-04_132528173_mnme9z.png"
            
            // ASUS ROG Thor 850P
            search.contains("rog") && search.contains("thor") && search.contains("850") ->
                "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789973/imagen_2026-03-29_091253254_ueu1cz.png"
                
            else -> null
        }
    }
}
