---
name: spec
description: >-
  Utiliza este skill cuando el usuario te solicite crear un documento de especificación técnica para detallar cómo se debe implementar una nueva funcionalidad.
---

# Technical Specification (spec)

Este skill define cómo estructurar y crear documentos de especificación técnica para planificar la implementación de nuevas funcionalidades en el proyecto RutaExpress.

## Reglas Obligatorias para la Creación del Documento

Al crear un nuevo documento de especificación, debes seguir estrictamente las siguientes reglas:

1. **Ubicación:** El documento generado **DEBE** guardarse dentro del directorio `docs/spec/` en la raíz del proyecto.
2. **Nomenclatura Semántica:** El nombre del archivo debe seguir una convención semántica y clara, utilizando kebab-case (por ejemplo: `feature-login-spec.md`, `gestion-rutas-spec.md`).
3. **Reglas de Arquitectura:** El diseño de la implementación debe respetar los principios descritos en `app/Arquitectura.md`:
   - **Arquitectura Base:** MVVM (Model-View-ViewModel) con principios de Clean Architecture.
   - **UI (Jetpack Compose):** Diseño declarativo, elevación de estado (State Hoisting), sin lógica de negocio, y flujo unidireccional de datos (UDF).
   - **ViewModel:** Mantenimiento del estado mediante `StateFlow` o `LiveData`. Sin referencias al contexto de Android (`Context` o `Activity`).
   - **Capa de Datos:** Uso de repositorios (Single Source of Truth) para abstraer el origen de los datos (Firebase, local, API) y data classes para modelos.
   - **Estados de UI:** Uso de Sealed Classes para manejar el estado de la vista (ej. `Loading`, `Success`, `Error`).

## Plantilla Sugerida para la Especificación

El documento generado debe incluir (como mínimo) la siguiente estructura:

```markdown
# Especificación Técnica: [Nombre de la Funcionalidad]

## 1. Descripción General
Resumen de la funcionalidad a implementar y los requisitos técnicos.

## 2. Capa de Presentación (UI)
*   **Composables (Jetpack Compose):** Descripción de los componentes de UI a crear en la carpeta `ui/`.
*   **State Hoisting y UDF:** Definición de los eventos que la vista emitirá y los estados que observará.

## 3. Capa de Presentación (ViewModel)
*   **Gestión de Estado:** Definición de la `sealed class` que representará el estado de la interfaz (ej. `Loading`, `Success`, `Error`).
*   **Lógica de Interacción:** Funciones del ViewModel en la carpeta `viewmodel/` y su interacción con los repositorios.

## 4. Capa de Datos
*   **Modelos (Data Classes):** Entidades a crear en `data/model/`.
*   **Repositorios:** Métodos y contratos a definir en `data/repository/`, incluyendo la lógica de abstracción de Firebase u otras fuentes.

## 5. Navegación e Inyección (Opcional)
*   Cambios requeridos en `AppNavigation` (NavHost).
*   Dependencias a inyectar en el ViewModel.
```
