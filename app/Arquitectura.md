# Arquitectura del Proyecto RutaExpress

Este proyecto está diseñado siguiendo las directrices de la arquitectura recomendada por Google para aplicaciones Android modernas, la cual está basada en **MVVM (Model-View-ViewModel)** junto con principios de **Clean Architecture**, y utilizando **Jetpack Compose** para la interfaz de usuario.

## 🏗️ Estructura de Capas (MVVM)

La aplicación está dividida principalmente en las siguientes capas, lo que permite separar las preocupaciones (Separation of Concerns), facilitar el mantenimiento y mejorar la escalabilidad y el testing del proyecto:

### 1. Capa de Presentación (UI + ViewModel)
- **UI (Jetpack Compose):** Se encuentra en la carpeta `ui/`. Aquí residen todos los componentes visuales (Screens y Composables) construidos de forma declarativa. Solo se encargan de observar el estado que provee el ViewModel y reaccionar a la interacción del usuario.
- **ViewModel:** Se encuentra en la carpeta `viewmodel/`. Actúa como intermediario entre la Vista (UI) y la capa de Datos. Mantiene el estado de la UI (`StateFlow` o `LiveData`) y maneja la lógica de presentación. No debe tener referencias a componentes de Android (como `Context` o `Activity`), lo cual facilita las pruebas unitarias.

### 2. Capa de Datos (Model/Data)
- **Data:** Se ubica en la carpeta `data/`. Es responsable de la lógica de negocio y de gestionar las fuentes de datos, ya sea de forma local (Room Database, DataStore) o remota (Firebase, APIs REST con Retrofit). 
- Suele contener:
  - **Modelos de datos:** Clases de datos (`data class`) que representan entidades (por ejemplo, `User`, `Viaje`, `Ruta`).
  - **Repositorios:** Clases que deciden si obtener datos de la red o de la caché local, proporcionando una única fuente de verdad (Single Source of Truth) para la aplicación.

## 📂 Estructura de Carpetas Sugerida

Actualmente, el proyecto base en `src/main/java/com/example/rutaexpress/` tiene la siguiente estructura que se debe seguir expandiendo:

```text
com.example.rutaexpress
│
├── MainActivity.kt        # Punto de entrada de la aplicación y configuración de Navigation (NavHost).
│
├── ui/                    # Capa de presentación - UI (Jetpack Compose)
│   ├── theme/             # Tipografía, colores y formas (Theme global).
│   ├── login/             # Pantalla de Login y sus componentes.
│   ├── signup/            # Pantalla de Registro y sus componentes.
│   └── common/            # Componentes reutilizables (Botones custom, TopBars, etc).
│
├── viewmodel/             # Capa de presentación - Lógica y Estado
│   ├── AuthViewModel.kt   # Manejo de lógica de autenticación.
│   └── ...
│
├── data/                  # Capa de datos y lógica de negocio
│   ├── model/             # Data classes (Entidades).
│   ├── repository/        # Repositorios (Lógica de acceso a datos).
│   └── remote/            # Servicios de Firebase / APIs.
│
└── di/                    # (Opcional a futuro) Inyección de Dependencias (Hilt/Dagger).
```

## 🛠️ Buenas Prácticas y Patrones Implementados

1. **Jetpack Compose para UI:**
   - Evitar lógicas complejas dentro de las funciones `@Composable`.
   - Utilizar "State Hoisting" (elevación de estado): pasar eventos como funciones lambda (ej. `onLoginSuccess`) y recibir estado como parámetros inmutables.
   - Navegación manejada a nivel superior (`AppNavigation` en `MainActivity`) utilizando Jetpack Navigation Compose.

2. **Unidirectional Data Flow (UDF):**
   - El estado fluye hacia abajo (del ViewModel a la Vista).
   - Los eventos fluyen hacia arriba (de la Vista al ViewModel).

3. **Integración con Firebase:**
   - La inicialización y los llamados a servicios en la nube (como Firebase Auth o Firestore) deben estar abstraídos en repositorios dentro de la capa `data/` y no mezclados con la UI.

4. **Inyección de Dependencias (Futuro):**
   - Se recomienda el uso de **Hilt** para proveer instancias de Repositorios a los ViewModels, reduciendo el acoplamiento y facilitando los test.

5. **Manejo de Errores y Cargas:**
   - Representar estados de la UI mediante "Sealed Classes" o Clases de Estado de UI (ej. `Loading`, `Success`, `Error`) para reflejar los diferentes momentos de un llamado a red.
