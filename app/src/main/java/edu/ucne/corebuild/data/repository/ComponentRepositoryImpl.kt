package edu.ucne.corebuild.data.repository

import android.util.Log
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.remote.dto.*
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val dao: ComponentDao,
    private val remoteDataSource: RemoteDataSource
) : ComponentRepository {

    override fun getComponents(): Flow<Resource<List<Component>>> = flow {
        emit(Resource.Loading())

        syncComponents(
            fetcher = { remoteDataSource.getCpus() },
            mapper = { it.toDomain() },
            imageDeterminer = { domain ->
                if (domain is Component.CPU) determineCpuImage(domain.name, domain.generation) else null
            },
            errorLabel = "CPUs"
        )

        syncComponents(
            fetcher = { remoteDataSource.getGpus() },
            mapper = { it.toDomain(it.id + 1000) },
            imageDeterminer = { determineGpuImage(it.name) },
            errorLabel = "GPUs"
        )

        syncComponents(
            fetcher = { remoteDataSource.getMotherboards() },
            mapper = { it.toDomain(it.id + 2000) },
            imageDeterminer = { determineMotherboardImage(it.name) },
            errorLabel = "Motherboards"
        )

        syncComponents(
            fetcher = { remoteDataSource.getRams() },
            mapper = { it.toDomain(it.id + 3000) },
            imageDeterminer = { determineRamImage(it.name) },
            errorLabel = "RAMs"
        )

        syncComponents(
            fetcher = { remoteDataSource.getPsus() },
            mapper = { it.toDomain(it.id + 4000) },
            imageDeterminer = { determinePsuImage(it.name) },
            errorLabel = "PSUs"
        )

        emitAll(
            dao.getComponents()
                .map { entities -> Resource.Success(entities.map { it.toDomain() }) }
        )
    }.flowOn(Dispatchers.IO)

    private suspend fun <T> FlowCollector<Resource<List<Component>>>.syncComponents(
        fetcher: suspend () -> Result<List<T>>,
        mapper: (T) -> Component,
        imageDeterminer: (Component) -> String?,
        errorLabel: String
    ) {
        fetcher()
            .onSuccess { list ->
                val entities = list.map { dto ->
                    val domain = mapper(dto)
                    val imageUrl = imageDeterminer(domain)
                    domain.withImageUrl(imageUrl).toEntity()
                }
                dao.insertAll(entities)
            }
            .onFailure { error ->
                Log.e("ComponentRepository", "Error al obtener $errorLabel", error)
                emit(Resource.Error("No se pudo sincronizar $errorLabel: ${error.localizedMessage}"))
            }
    }

    override fun getComponentById(id: Int): Flow<Component?> {
        return dao.getComponentById(id)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun addComponent(component: Component): Result<Unit> = runCatching {
        val result = when (component) {
            is Component.CPU -> remoteDataSource.createCpu(component.toDto())
            is Component.GPU -> remoteDataSource.createGpu(component.toDto())
            is Component.Motherboard -> remoteDataSource.createMotherboard(component.toDto())
            is Component.RAM -> remoteDataSource.createRam(component.toDto())
            is Component.PSU -> remoteDataSource.createPsu(component.toDto())
        }
        handleComponentResponse(result)
    }

    override suspend fun updateComponent(component: Component): Result<Unit> = runCatching {
        val result = when (component) {
            is Component.CPU -> remoteDataSource.updateCpu(component.id, component.toDto())
            is Component.GPU -> remoteDataSource.updateGpu(component.id - 1000, component.toDto())
            is Component.Motherboard -> remoteDataSource.updateMotherboard(component.id - 2000, component.toDto())
            is Component.RAM -> remoteDataSource.updateRam(component.id - 3000, component.toDto())
            is Component.PSU -> remoteDataSource.updatePsu(component.id - 4000, component.toDto())
        }
        handleComponentResponse(result)
    }

    private suspend fun handleComponentResponse(response: Result<Any>) {
        response.onSuccess { dto ->
            val domain = when (dto) {
                is CpuDto -> dto.toDomain()
                is GpuDto -> dto.toDomain(dto.id + 1000)
                is MotherboardDto -> dto.toDomain(dto.id + 2000)
                is RamDto -> dto.toDomain(dto.id + 3000)
                is PsuDto -> dto.toDomain(dto.id + 4000)
                else -> throw IllegalStateException("Tipo de DTO desconocido")
            }
            dao.insertAll(listOf(domain.toEntity()))
        }.onFailure { throw it }
    }

    override suspend fun deleteComponent(id: Int, type: String): Result<Unit> = runCatching {
        val response = when (type.lowercase()) {
            "procesador", "cpu" -> remoteDataSource.deleteCpu(id)
            "tarjeta gráfica", "gpu" -> remoteDataSource.deleteGpu(id - 1000)
            "placa base", "motherboard" -> remoteDataSource.deleteMotherboard(id - 2000)
            "memoria ram", "ram" -> remoteDataSource.deleteRam(id - 3000)
            "fuente de poder", "psu" -> remoteDataSource.deletePsu(id - 4000)
            else -> throw IllegalArgumentException("Tipo no soportado: $type")
        }

        response.onSuccess {
            dao.deleteById(id)
        }.onFailure { throw it }
    }

    private fun String.cleanInt() = this.filter { it.isDigit() }.toIntOrNull()

    private fun Component.CPU.toDto() = CpuDto(
        id = this.id,
        nombre = this.name,
        marca = this.brand,
        socket = this.socket,
        generacion = this.generation,
        nucleos = this.cores,
        hilos = this.threads,
        frecuenciaBase = this.baseClock,
        frecuenciaTurbo = this.boostClock,
        tdpWatts = this.tdp.cleanInt(),
        precioUsd = this.price,
        descripcion = this.description,
        imageUrl = this.imageUrl
    )

    private fun Component.GPU.toDto() = GpuDto(
        id = if (this.id > 1000) this.id - 1000 else this.id,
        nombre = this.name,
        marca = this.brand,
        chipset = this.chipset,
        vram = this.vram,
        tipoVram = this.vramType,
        frecuenciaBase = this.baseClock,
        frecuenciaBoost = this.boostClock,
        consumoWatts = this.consumptionWatts.cleanInt(),
        precioUsd = this.price,
        descripcion = this.description,
        imageUrl = this.imageUrl
    )

    private fun Component.Motherboard.toDto() = MotherboardDto(
        id = if (this.id > 2000) this.id - 2000 else this.id,
        nombre = this.name,
        marca = this.brand,
        socket = this.socket,
        chipset = this.chipset,
        formato = this.format,
        tipoRam = this.ramType,
        velocidadRamMax = this.maxRamCapacity,
        precioUsd = this.price,
        descripcion = this.description,
        imageUrl = this.imageUrl,
        slotsM2 = this.slotsM2
    )

    private fun Component.RAM.toDto() = RamDto(
        id = if (this.id > 3000) this.id - 3000 else this.id,
        nombre = this.name,
        marca = this.brand,
        tipo = this.type,
        capacidadTotal = this.capacity,
        configuracion = this.configuration,
        velocidad = this.speed,
        latencia = this.latency,
        voltaje = this.voltage,
        precioUsd = this.price,
        descripcion = this.description,
        imageUrl = this.imageUrl
    )

    private fun Component.PSU.toDto() = PsuDto(
        id = if (this.id > 4000) this.id - 4000 else this.id,
        nombre = this.name,
        marca = this.brand,
        potenciaWatts = this.wattage,
        certificacion = this.certification,
        tipoModular = this.modularity,
        ventilador = this.fanSize,
        protecciones = this.protection,
        precioUsd = this.price,
        descripcion = this.description,
        imageUrl = this.imageUrl
    )

    private fun determineCpuImage(name: String, generation: String): String? {
        val search = "$name $generation".lowercase()
        val nameOnly = name.lowercase()

        val images = mapOf(
            "ryzen_3000" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_3000_l7gbgt.jpg",
            "ryzen_4000" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741851/Ryzen_4000_isyqkf.png",
            "ryzen_5000" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741841/Ryzen_5000_waljfw.jpg",
            "ryzen_7000" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741951/Ryzen_7000_flhzjd.jpg",
            "ryzen_9000" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774741950/Ryzen_9000_pwufy0.jpg",
            "intel_arrow" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774743251/imagen_2026-03-28_201410763_ex7ktj.png",
            "intel_14" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746616/imagen_2026-03-28_211015385_lx6ngi.png",
            "intel_13" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746606/imagen_2026-03-28_211005856_wlezyh.png",
            "intel_12" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746551/imagen_2026-03-28_210908927_dep24d.png",
            "intel_11" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774746405/imagen_2026-03-28_210642931_csxqqh.png",
            "intel_10" to "https://res.cloudinary.com/dsnaidobx/image/upload/v1774745942/imagen_2026-03-28_205900911_jizp1k.png"
        )

        if (search.contains("intel") || search.contains("core")) {
            return when {
                search.contains("ultra") || search.contains("245k") || search.contains("265k") || search.contains("285k") -> images["intel_arrow"]
                search.contains("-14") || search.contains("14th gen") -> images["intel_14"]
                search.contains("-13") || search.contains("13th gen") -> images["intel_13"]
                search.contains("-12") || search.contains("12th gen") -> images["intel_12"]
                search.contains("-11") || search.contains("11th gen") -> images["intel_11"]
                search.contains("-10") || search.contains("10th gen") -> images["intel_10"]
                else -> null
            }
        }

        if (nameOnly.contains("ryzen")) {
            val modelNumber = Regex("""\d{4}""").find(nameOnly)?.value ?: ""
            return when (modelNumber.firstOrNull()) {
                '9' -> images["ryzen_9000"]
                '7' -> images["ryzen_7000"]
                '5' -> images["ryzen_5000"]
                '3' -> images["ryzen_3000"]
                '4' -> if (modelNumber == "4100" || modelNumber == "4500") images["ryzen_3000"] else images["ryzen_4000"]
                else -> null
            }
        }
        return null
    }

    private fun determineGpuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("5090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747099/imagen_2026-03-28_211818710_uexrhy.png"
            search.contains("5070") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319604/imagen_2026-04-04_122000988_zsyi4c.png"
            search.contains("5070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319562/imagen_2026-04-04_121918968_ribthz.png"
            search.contains("5060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1774747013/imagen_2026-03-28_211525393_u32mud.png"
            search.contains("4090") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319628/imagen_2026-04-04_121453805_khsde8.png"
            search.contains("4080") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319057/imagen_2026-04-04_121054768_eczizd.png"
            search.contains("4070") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319000/imagen_2026-04-04_120957236_g7suia.png"
            search.contains("4060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318925/imagen_2026-04-04_120841799_ldb57h.png"
            search.contains("4060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319927/imagen_2026-04-04_120757367_alvgog.png"
            search.contains("3080") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775319882/imagen_2026-04-04_122438639_bqblak.png"
            search.contains("3070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318695/imagen_2026-04-04_120452312_r7q4vc.png"
            search.contains("3060") && search.contains("ti") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318591/imagen_2026-04-04_120308449_wu6k4o.png"
            search.contains("3060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318574/imagen_2026-04-04_120251372_b9hkw8.png"
            search.contains("2060") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318509/imagen_2026-04-04_120146950_ic9o9u.png"
            search.contains("1660") && search.contains("super") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775318458/imagen_2026-04-04_120055557_s9unfh.png"
            search.contains("9070") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321290/imagen_2026-04-04_124802651_iptoe8.png"
            search.contains("9070") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321204/imagen_2026-04-04_124637444_qtjd5c.png"
            search.contains("9060") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321149/imagen_2026-04-04_124446296_f4kyeb.png"
            search.contains("7900") && search.contains("xtx") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321006/imagen_2026-04-04_124319703_fhclm2.png"
            search.contains("7900") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320886/imagen_2026-04-04_123949291_gnoewl.png"
            search.contains("7800") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320744/imagen_2026-04-04_123859526_lnl0b8.png"
            search.contains("7700") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320486/imagen_2026-04-04_123442364_gsmuyk.png"
            search.contains("7600") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320410/imagen_2026-04-04_123325994_diz6ap.png"
            search.contains("6800") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747210/imagen_2026-03-28_212009640_a62xz9.png"
            search.contains("6800") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320347/imagen_2026-04-04_123223006_uysagz.png"
            search.contains("6700") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320104/imagen_2026-04-04_122820556_t7x518.png"
            search.contains("6650") && search.contains("xt") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320085/imagen_2026-04-04_122801885_n5oyrw.png"
            search.contains("6600") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774747183/imagen_2026-03-28_211943116_lc7xvs.png"
            search.contains("580") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775320054/imagen_2026-04-04_122730864_hc9r4l.png"
            else -> null
        }
    }

    private fun determineMotherboardImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("tomahawk") && search.contains("b550") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775335932/imagen_2026-04-04_165211818_aniu8s.png"
            search.contains("steel legend") && search.contains("x570") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775335986/imagen_2026-04-04_165305611_pxlc39.png"
            search.contains("tuf") && (search.contains("a620") || search.contains("620")) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775336077/imagen_2026-04-04_165437283_qwb80j.png"
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
            search.contains("vengeance") && search.contains("rgb") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789924/imagen_2026-03-29_091202718_yrikxo.png"
            search.contains("vengeance") && search.contains("lpx") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321782/imagen_2026-04-04_125614615_yyrryx.png"
            search.contains("ripjaws") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321881/imagen_2026-04-04_125754161_q5zqd0.png"
            search.contains("fury") && search.contains("beast") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321930/imagen_2026-04-04_125842604_acjqpw.png"
            search.contains("trident") && search.contains("z5") && search.contains("rgb") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775321976/imagen_2026-04-04_125928533_pqfyev.png"
            search.contains("dominator") && search.contains("platinum") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322029/imagen_2026-04-04_130015179_mcgkxq.png"
            search.contains("trident") && (search.contains("royal") || search.contains("neo")) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322093/imagen_2026-04-04_130126082_vylrzh.png"
            else -> null
        }
    }

    private fun determinePsuImage(name: String): String? {
        val search = name.lowercase()
        return when {
            search.contains("evga") && search.contains("500") && search.contains("w3") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322689/imagen_2026-04-04_131121363_h728wq.png"
            search.contains("cooler") && search.contains("master") && search.contains("mwe") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322742/imagen_2026-04-04_131213943_jzy0pz.png"
            search.contains("corsair") && (search.contains("cx650") || (search.contains("cx") && search.contains("650"))) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775322764/imagen_2026-04-04_131237025_bccaco.png"
            search.contains("msi") && search.contains("mpg") && search.contains("650") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323138/imagen_2026-04-04_131849921_hvjzsa.png"
            search.contains("seasonic") && search.contains("focus") && search.contains("750") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323165/imagen_2026-04-04_131917289_em55jw.png"
            search.contains("corsair") && (search.contains("rm750") || (search.contains("rm") && search.contains("750"))) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323190/imagen_2026-04-04_131942824_qvigfi.png"
            search.contains("evga") && search.contains("supernova") && search.contains("850") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323213/imagen_2026-04-04_132005083_f9gjjs.png"
            search.contains("rog") && search.contains("thor") && search.contains("850") -> "https://res.cloudinary.com/dsnaidobx/image/upload/v1774789973/imagen_2026-03-29_091253254_ueu1cz.png"
            search.contains("quiet") && search.contains("dark") && search.contains("power") -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323310/imagen_2026-04-04_132142547_dz8gen.png"
            search.contains("corsair") && (search.contains("hx1200") || (search.contains("hx") && search.contains("1200"))) -> "https://res.cloudinary.com/dsnaidobx/image/upload/q_auto/f_auto/v1775323536/imagen_2026-04-04_132528173_mnme9z.png"
            else -> null
        }
    }
}
