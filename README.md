# 🔧 CoreBuild Pro - PC Hardware Ecosystem

**CoreBuild** es una plataforma avanzada de Android diseñada para entusiastas del hardware, gamers y ensambladores profesionales. A diferencia de un e-commerce convencional, CoreBuild integra **motores de cálculo avanzados** para garantizar que cada componente sea técnica y económicamente viable dentro de un ecosistema de alto rendimiento.

<img width="250" height="2224" alt="Screenshot_20260415_203300_CoreBuild" src="https://github.com/user-attachments/assets/766ef817-d626-40e4-9169-7f6af7092e11" />


![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Clean-Architecture-blue?style=for-the-badge)
![Hilt](https://img.shields.io/badge/DI-Hilt-orange?style=for-the-badge)

---
## 👨🏻‍💻👨🏻‍💻 Participantes 
- **Andy Vladimir Javier Familia 2021-0767**
- **Anderson Núñez Consoró 2019-0030**

## 🚀 Funcionalidades Destacadas

### 👤 Experiencia del Usuario
- 🛒 **Gestión de Carrito Inteligente**: No es solo una lista de compras. Analiza en tiempo real la compatibilidad (Socket, TDP, tipo de RAM) y genera un **Build Score** dinámico basado en el balance de calidad/precio.
- 📉 **Calculadora de Cuello de Botella (Bottleneck)**: Algoritmo predictivo que identifica si el procesador o la gráfica limitan el rendimiento mutuo según la resolución seleccionada (1080p, 1440p, 4K).
  
<img width="250" height="2224" alt="Screenshot_20260415_203621_CoreBuild" src="https://github.com/user-attachments/assets/1e787397-9ae8-4199-9f81-9d28b8368608" />

- 🎮 **Simulador de FPS**: Predicción de rendimiento en cuadros por segundo para títulos AAA populares (Cyberpunk 2077, GTA V, Valorant) utilizando modelos de benchmarking sintético.

<img width="250" height="2224" alt="Screenshot_20260415_203506_CoreBuild" src="https://github.com/user-attachments/assets/5a80e409-a548-4864-9296-17997cb326f6" />

- 🧠 **Smart Builder**: Generador automático de presupuestos optimizados. El usuario elige un componente "ancla" (ej. una RTX 4070) y el sistema selecciona automáticamente el resto de piezas para maximizar el rendimiento.
- 🔍 **Comparador Pro**: Interfaz técnica para comparar especificaciones detalladas (Caché, VRAM, frecuencias, TDP) de dos componentes lado a lado con visualización de barras de potencia.
- 🎯 **Recomendador IA por Presupuesto**: Encuentra la mejor configuración posible ajustándose estrictamente a un límite de gasto definido por el usuario.

<img width="250" height="2680" alt="Screenshot_20260415_203603_CoreBuild" src="https://github.com/user-attachments/assets/9207788e-e344-447e-ae32-7d66bd2c46b8" />

### 🛡️ Módulo de Administración (Panel de Control)
- **Control Total (CRUD)**: Gestión completa de la base de datos de hardware (CPUs, GPUs, Placas Base, RAMs, PSUs).
- ☁️ **Cloudinary Image Integration**: Sistema de subida de imágenes a la nube directamente desde la galería del dispositivo, generando URLs seguras y optimizadas.
- 📜 **Auditoría de Logs**: Registro detallado de cada operación realizada por los administradores para garantizar la trazabilidad.
- 📦 **Gestión de Pedidos**: Visualización y seguimiento del estado de las órdenes generadas por los usuarios.

---

## 🏗️ Arquitectura del Software

La aplicación está construida bajo los principios de **Clean Architecture** y el patrón **MVI (Model-View-Intent)**, asegurando un flujo de datos unidireccional y una separación de responsabilidades impecable.

### Capas del Proyecto:
1.  **Data Layer**: 
    *   **Offline-First**: Utiliza **Room** como Fuente Única de Verdad (SSOT). Los datos se consultan localmente para garantizar velocidad y disponibilidad sin conexión.
    *   **Networking**: **Retrofit 2** con Moshi para el consumo de la API REST externa.
    *   **Sincronización Inteligente**: El `SyncManager` coordina la descarga de datos remotos mientras protege las ediciones manuales (como imágenes de Cloudinary) realizadas por el administrador.
2.  **Domain Layer**: 
    *   Modelos de dominio puros y fuertemente tipados mediante **Sealed Classes**.
    *   **Motores de Negocio**: `CompatibilityEngine`, `PerformanceCalculator` y `BuildScoreCalculator`.
3.  **Presentation Layer**: 
    *   UI 100% declarativa con **Jetpack Compose**.
    *   Navegación con **Type Safety** para evitar errores en tiempo de ejecución.
    *   Funciones de extensión personalizadas (ej: `.toPrice()`) para consistencia visual.

---

## 🖼️ Sistema de Imagen Híbrido e Inteligente

Uno de los puntos más innovadores es la lógica de visualización de hardware:
1.  **Prioridad Local (Admin)**: Si un administrador sube una foto personalizada, esta se preserva en la base de datos local y se respeta ante cualquier sincronización.
2.  **API Fallback**: Si no existe imagen local, se intenta cargar la URL proporcionada por el servidor remoto.
3.  **Automated Image Determiner**: Como última instancia, un motor de reglas interno analiza el nombre y la generación del producto (ej. "Ryzen 7000") y asigna automáticamente una imagen oficial del socket correspondiente.

---

## 🛠️ Stack Tecnológico

| Tecnología | Propósito |
| :--- | :--- |
| **Kotlin Coroutines / Flow** | Programación asíncrona y flujos de datos reactivos. |
| **Dagger Hilt** | Inyección de dependencias para desacoplamiento total. |
| **Room Database** | Persistencia local robusta con soporte para migraciones. |
| **Jetpack Compose** | Interfaz de usuario moderna con Material 3. |
| **Cloudinary SDK** | Almacenamiento, gestión y optimización de media en la nube. |
| **Coil** | Carga eficiente de imágenes con gestión de caché. |
| **Retrofit 2** | Cliente HTTP para comunicación con microservicios. |

---

## 📂 Estructura de Carpetas

```text
app/src/main/java/edu/ucne/corebuild/
├── data/
│   ├── local/           # Room: Entidades, DAOs y Database
│   ├── remote/          # Retrofit: Endpoints, DTOs y Cloudinary Config
│   ├── repository/      # Implementación de Repositorios (Lógica Local vs Remota)
│   └── sync/            # Motores de sincronización y SyncManager
├── domain/
│   ├── model/           # Modelos de dominio (Sealed classes para Componentes)
│   ├── use_case/        # Casos de uso atómicos
│   └── (engines)/       # Motores: Compatibility, Performance, SmartBuilder
├── presentation/        # UI: Pantallas, ViewModels y Eventos (MVI)
├── di/                  # Módulos de provisión de dependencias (Hilt)
└── util/                # Utilidades globales y funciones de extensión
```

---

## ⚙️ Configuración e Instalación

### Requisitos:
*   Android Studio Ladybug o superior.
*   JDK 17+.
*   Dispositivo o Emulador con Android 8.0 (API 26) o superior.

### Pasos:
1.  **Clonar**: `git clone https://github.com/tu-usuario/CoreBuild.git`
2.  **API Key**: Configura tus credenciales de Cloudinary en `data/remote/CloudinaryConfig.kt`.
3.  **Sincronizar**: Realiza un *Gradle Sync* para descargar todas las dependencias.
4.  **Ejecutar**: Instala la aplicación en tu dispositivo mediante la tarea `:app:installDebug`.

---
© 2026 CoreBuild Team - Soluciones de ingeniería para el PC Master Race.
