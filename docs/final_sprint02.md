# Sprint 02 - Final Report

Version: `2.0.1`  
Equipo: Anouar El Kabiri, Eloi Mora Palomino

## 1. Sprint Goal

Completar la capa funcional de `BBTraveling` con:
- Arquitectura `MVVM`
- CRUD inMemory de viajes
- CRUD inMemory de actividades del itinerario
- Validaciones de fechas, presupuesto y campos obligatorios
- Persistencia de ajustes de usuario con `SharedPreferences`
- Multi-language funcional (`en`, `es`, `ca`)
- Testing y documentacion minima de entrega

---

## 2. Sprint Backlog

| ID | Tarea | Responsable | Estado |
|----|------|------------|--------|
| T1.1 | Implementar CRUD de trips en memoria | Anouar | Completada |
| T1.2 | Implementar CRUD de activities en memoria | Anouar | Completada |
| T1.3 | Validar fechas de trips y activities | Anouar | Completada |
| T1.4 | Persistir user settings con SharedPreferences | Eloi | Completada |
| T1.5 | Implementar multi-language en, es, ca | Anouar | Completada |
| T2.1 | Conectar UI con ViewModel y Repository | Anouar y Eloi | Completada |
| T2.2 | Crear formularios de alta y edicion | Anouar y Eloi | Completada |
| T2.3 | Hacer que los cambios se reflejen dinamicamente | Anouar | Completada |
| T3.1 | Anadir logs para operaciones y errores | Eloi | Completada |
| T3.2 | Crear unit tests de CRUD | Anouar | Completada |
| T3.3 | Actualizar README y docs del sprint | Eloi | Completada |
| T3.4 | Preparar y entregar release `v2.0.1` | Anouar | Completada |

Resumen funcional del backlog:
- Arquitectura `UI -> ViewModel -> Repository -> DataSource` implementada.
- CRUD completo de viajes y actividades en memoria con actualizacion dinamica en pantalla.
- Validaciones en `UI`, `ViewModel` y `Repository` para evitar datos inconsistentes.
- Ajustes persistidos: `username`, `dateOfBirth`, `darkMode`, `languageTag`, `termsAccepted`.
- Flujo de `Terms & Conditions` en primer arranque.
- Presupuesto real por viaje y coste real por actividad, todo en euros.
- Selector de ciudad y pais para que los viajes creados manualmente sigan el mismo formato visual que los viajes mock.
- Categorias y plantillas predefinidas para agilizar la creacion de actividades.
- Recursos multi-language separados en `values`, `values-es` y `values-ca`.
- Evidencia del sprint publicada mediante enlace externo en `doc/evidence/v2.0.1/`.

---

## 3. Definition of Done (DoD)

- [x] CRUD de viajes y actividades funcionando en memoria
- [x] Validaciones implementadas en UI, ViewModel y Repository
- [x] Settings persistidos y cargados al reiniciar la app
- [x] Terms & Conditions apareciendo al iniciar por primera vez
- [x] Cambio de idioma funcional en tiempo de ejecucion
- [x] Recursos de idioma preparados en `values`, `values-es` y `values-ca`
- [x] Logs basicos de operaciones y errores
- [x] Evidencia del video disponible en `doc/evidence/v2.0.1/`
- [x] Release final preparada (`v2.0.1`)

Validacion tecnica:
- [x] `./gradlew.bat :app:assembleDebug --console=plain`
- [x] `./gradlew.bat :app:testDebugUnitTest --console=plain`

---

## 4. Riesgos identificados

- Problemas al mantener el estado entre pantallas  
  Mitigacion: uso de `StateFlow`, `ViewModel` y repositorio unico para centralizar cambios.

- Validaciones inconsistentes entre UI y Repository  
  Mitigacion: reglas centralizadas en `TravelValidator` y comprobacion defensiva en varias capas.

- Errores al persistir ajustes de usuario  
  Mitigacion: repositorio especifico de settings con `SharedPreferences` y restauracion al iniciar.

- Textos hardcoded que rompan el multi-language  
  Mitigacion: textos movidos a recursos `strings.xml` por idioma y cambio de locale en runtime.

- Falta de cobertura de tests en casos limite  
  Mitigacion: tests unitarios de dominio y repositorio para CRUD, rangos de fechas y calculo de gastos.

- Bloqueo circular de validacion  
  Ejemplo: para mover el viaje el sistema pedia cambiar antes las actividades, pero esas actividades seguian dependiendo del rango actual del viaje.

  Mitigacion: se añadio la opcion de mover tambien el itinerario junto con el viaje y validar el resultado final completo.

---

## 5. Mejoras futuras

- Mejorar mensajes de error con mas contexto para el usuario.
  Ejemplo: si una actividad queda fuera del rango del viaje, mostrar tambien las fechas exactas del viaje para corregirlo mas rapido.

- Ampliar el selector de paises y hacerlo mas comodo con busqueda o listas mas grandes.

- Incorporar persistencia mas avanzada.
