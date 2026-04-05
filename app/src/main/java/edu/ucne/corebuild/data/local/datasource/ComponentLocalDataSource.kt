package edu.ucne.corebuild.data.local.datasource

import edu.ucne.corebuild.domain.model.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponentLocalDataSource @Inject constructor() {
    fun getAllComponents(): List<Component> {
        return listOf(
            Component.CPU(1, "Intel Core i3-10100F", "Procesador de entrada ideal para builds de bajo presupuesto.", 70.0, "Intel", "LGA1200", "10a Gen", 4, 8, "3.6 GHz", "4.3 GHz", "65W"),
            Component.CPU(2, "Intel Core i5-10400F", "CPU de gama media-baja. Ideal para builds gaming de presupuesto ajustado.", 90.0, "Intel", "LGA1200", "10a Gen", 6, 12, "2.9 GHz", "4.3 GHz", "65W"),
            Component.CPU(3, "Intel Core i7-10700K", "Procesador desbloqueado de alto rendimiento con 8 núcleos.", 180.0, "Intel", "LGA1200", "10a Gen", 8, 16, "3.8 GHz", "5.1 GHz", "125W"),
            Component.CPU(4, "Intel Core i9-10900K", "Flagship de 10a generación. Orientado a gaming extremo.", 220.0, "Intel", "LGA1200", "10a Gen", 10, 20, "3.7 GHz", "5.3 GHz", "125W"),
            Component.CPU(8, "Intel Core i5-12400F", "Arquitectura Alder Lake con soporte DDR4 y DDR5. Muy eficiente.", 150.0, "Intel", "LGA1700", "12a Gen", 6, 12, "2.5 GHz", "4.4 GHz", "65W"),
            Component.CPU(21, "Intel Core i9-14900K", "El CPU Intel de mayor rendimiento para LGA1700. 24 núcleos.", 580.0, "Intel", "LGA1700", "14a Gen", 24, 32, "3.2 GHz", "6.0 GHz", "125W"),
            Component.CPU(24, "Intel Core Ultra 9 285K", "Flagship Arrow Lake. 24 núcleos optimizados para eficiencia.", 620.0, "Intel", "LGA1851", "Core Ultra 200", 24, 24, "3.7 GHz", "5.7 GHz", "125W"),

            Component.CPU(25, "AMD Ryzen 5 3600", "Clásico de la plataforma AM4. Excelente para gaming y multitarea.", 90.0, "AMD", "AM4", "Zen 2", 6, 12, "3.6 GHz", "4.2 GHz", "65W"),
            Component.CPU(31, "AMD Ryzen 5 5600", "Uno de los CPUs gaming de mejor relación calidad-precio.", 120.0, "AMD", "AM4", "Zen 3", 6, 12, "3.5 GHz", "4.4 GHz", "65W"),
            Component.CPU(35, "AMD Ryzen 7 5800X3D", "El mejor CPU gaming de plataforma AM4 con 3D V-Cache.", 320.0, "AMD", "AM4", "Zen 3", 8, 16, "3.4 GHz", "4.5 GHz", "105W"),
            Component.CPU(42, "AMD Ryzen 7 7800X3D", "El CPU gaming más popular de AM5. Rendimiento excepcional.", 420.0, "AMD", "AM5", "Zen 4", 8, 16, "4.5 GHz", "5.0 GHz", "120W"),
            Component.CPU(47, "AMD Ryzen 7 9800X3D", "El rey indiscutible del gaming AM5 con arquitectura Zen 5.", 520.0, "AMD", "AM5", "Zen 5", 8, 16, "4.7 GHz", "5.2 GHz", "120W"),

            Component.GPU(49, "NVIDIA GeForce GTX 1660 Super", "Excelente para 1080p en calidad media-alta.", 180.0, "NVIDIA", "TU116", "6GB", "GDDR6", consumptionWatts = "125", recommendedPSU = "450W"),
            Component.GPU(51, "NVIDIA GeForce RTX 3060", "Gama media con generosa VRAM de 12GB. Ideal 1080p.", 290.0, "NVIDIA", "GA106", "12GB", "GDDR6", consumptionWatts = "170", recommendedPSU = "550W"),
            Component.GPU(57, "NVIDIA GeForce RTX 4070 Super", "Ada Lovelace con DLSS 3. Excelente para 1440p.", 620.0, "NVIDIA", "AD104", "12GB", "GDDR6X", consumptionWatts = "220", recommendedPSU = "700W"),
            Component.GPU(59, "NVIDIA GeForce RTX 4090", "La GPU de consumo más poderosa del mundo. 24GB VRAM.", 1800.0, "NVIDIA", "AD102", "24GB", "GDDR6X", consumptionWatts = "450", recommendedPSU = "850W"),
            Component.GPU(63, "NVIDIA GeForce RTX 5090", "Máximo dominio en 4K, 8K y AI. 32GB GDDR7.", 2200.0, "NVIDIA", "GB202", "32GB", "GDDR7", consumptionWatts = "575", recommendedPSU = "1000W"),

            Component.GPU(64, "AMD Radeon RX 580", "GPU clásica económica para presupuestos limitados.", 90.0, "AMD", "Polaris 20", "8GB", "GDDR5", consumptionWatts = "185", recommendedPSU = "500W"),
            Component.GPU(67, "AMD Radeon RX 6700 XT", "RDNA 2 de gama media-alta. Excelente para 1440p.", 330.0, "AMD", "Navi 22", "12GB", "GDDR6", consumptionWatts = "230", recommendedPSU = "650W"),
            Component.GPU(74, "AMD Radeon RX 7900 XTX", "La GPU más potente de Radeon. 24GB GDDR6.", 950.0, "AMD", "Navi 31", "24GB", "GDDR6", consumptionWatts = "355", recommendedPSU = "850W"),

            Component.Motherboard(77, "ASUS Prime H510M", "Placa económica LGA1200 para Intel 10a/11a Gen.", 80.0, "ASUS", "LGA1200", "H510", "Micro-ATX", "DDR4"),
            Component.Motherboard(81, "ASUS ROG Strix Z790-E", "Gama entusiasta con 18+1 fases de poder.", 420.0, "ASUS", "LGA1700", "Z790", "ATX", "DDR5"),
            Component.Motherboard(89, "Gigabyte B650 Gaming X AX", "Equilibrada para AM5 con WiFi 6E y soporte EXPO.", 220.0, "Gigabyte", "AM5", "B650", "ATX", "DDR5"),
            Component.Motherboard(91, "ASRock X870E Nova WiFi", "Última generación para Ryzen 9000 con WiFi 7.", 500.0, "ASRock", "AM5", "X870E", "ATX", "DDR5"),

            Component.RAM(92, "Corsair Vengeance LPX 16GB", "DDR4-3200 de perfil bajo y alta compatibilidad.", 70.0, "Corsair", "DDR4", "16GB (2x8GB)", "16GB (2x8GB)", "3200 MHz", "CL16"),
            Component.RAM(101, "G.Skill Trident Z5 RGB 32GB", "DDR5-6000 premium con latencia ultra baja CL30.", 180.0, "G.Skill", "DDR5", "32GB (2x16GB)", "32GB (2x16GB)", "6000 MHz", "CL30"),
            Component.RAM(105, "Corsair Dominator Platinum 128GB", "RAM DDR5 profesional para workstations extremas.", 750.0, "Corsair", "DDR5", "128GB (2x64GB)", "128GB (2x64GB)", "6200 MHz", "CL32"),

            Component.PSU(110, "Seasonic Focus GX-750", "Calidad excepcional con ventilador FDB silencioso.", 140.0, "Seasonic", 750, "80 Plus Gold", "Totalmente modular"),
            Component.PSU(114, "be quiet! Dark Power 13", "Silenciosa por excelencia con eficiencia Titanium.", 320.0, "be quiet!", 1000, "80 Plus Titanium", "Totalmente modular"),
            Component.PSU(115, "Corsair HX1200i", "1200W con monitoreo digital para sistemas extremos.", 350.0, "Corsair", 1200, "80 Plus Platinum", "Totalmente modular")
        )
    }
}
