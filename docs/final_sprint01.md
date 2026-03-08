# Sprint 01 - Final Report

Version: `1.0.0`  
Equipo: Anouar El Kabiri, Eloi Mora Palomino

## 1. Sprint Goal

Completar el MVP mock de `BBTraveling` con:
- Base del proyecto Android (Kotlin + Compose)
- Navegacion funcional entre pantallas
- 7 pantallas mock solicitadas en Sprint 01
- Modelo de dominio y datos hardcoded consistentes
- Documentacion minima de entrega

---

## 2. Sprint Backlog

| ID | Tarea | Responsable | Estado |
|----|------|------------|--------|
| T1.1 | Crear nombre del producto | Eloi y Anouar | Completada |
| T1.2 | Crear logo | Eloi y Anouar | Completada |
| T1.3 | Configurar version minima Android | Anouar | Completada |
| T1.4 | Configurar Kotlin | Anouar | Completada |
| T1.5 | Crear proyecto inicial | Anouar | Completada |
| T2.1 | Crear repositorio GitHub | Anouar | Completada |
| T2.3 | Anadir LICENSE | Eloi | Completada |
| T2.4 | Crear CONTRIBUTING | Eloi y Anouar | Completada |
| T2.5 | Crear README | Eloi y Anouar | Completada |
| T2.6 | Crear estructura /docs | Eloi | Completada |
| T2.7 | Crear design.md | Eloi y Anouar | Completada |
| T2.10 | Crear Release v1.0.0 | Anouar | Completada |
| T3.1 | Crear pantallas mock | Anouar y Eloi | Completada |
| T3.2 | Implementar navegacion | Anouar | Completada |
| T3.3 | Crear diagrama modelo dominio | Eloi y Anouar | Completada |
| T3.4 | Crear clases dominio | Anouar y Eloi | Completada |
| T4.1 | Implementar Splash Screen | Anouar | Completada |
| T4.2 | Crear pantalla About | Eloi y Anouar | Completada |
| T4.3 | Crear pantalla Terms | Eloi | Completada |
| T4.4 | Crear pantalla Preferences | Anouar y Eloi | Completada |

Resumen funcional del backlog:
- Pantallas implementadas: `Splash`, `Home`, `Trip Detail + Itinerary`, `Gallery`, `Preferences`, `About`, `Terms`.
- Navegacion completa con `NavController` (root + bottom tabs).
- Datos mock consistentes en `MockData` (4 viajes, actividades y fotos).
- Preferencias mock con desplegable de idioma: `English`, `Espanol`, `Catalan`.

---

## 3. Definition of Done (DoD)

- [x] Modelo de dominio implementado en carpeta `domain`
- [x] Paleta de colores aplicada en `Theme`
- [x] README actualizado
- [x] Release publicada (`v1.0.0`)

Validacion tecnica:
- [x] `./gradlew :app:assembleDebug`
- [x] `./gradlew :app:testDebugUnitTest`

---

## 4. Riesgos identificados

- Falta de experiencia con Git  
  Mitigacion: flujo de commits y estructura de docs definida desde inicio.

- Problemas de configuracion Android  
  Mitigacion: ajustes gradle, limpieza de dependencias y compilacion continua.

- Problemas con navegacion UI  
  Mitigacion: separacion de root graph y main shell con rutas claras.

- Problemas con diagramas de dominio  
  Mitigacion: documentacion Mermaid mantenida en `docs/`.

---

## 5. Update post-release (`2026-03-08`)

- Se anade version visual del diagrama UML en `docs/diagrams/app-uml.jpg`.
