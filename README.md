# BBTraveling (BloodBrothersTravelling)

Proyecto Android de planificacion de viajes para la asignatura **Applications for Mobile Devices**.

`BBTraveling` se desarrollo en dos sprints:
- `Sprint 01`: estructura visual, navegacion y pantallas base.
- `Sprint 02`: logica funcional, validaciones, persistencia de ajustes, testing y documentacion.

## Estado del proyecto

La version actual implementa los requisitos funcionales del `LAB_SPRINT02`:
- arquitectura `UI -> ViewModel -> Repository -> DataSource`
- CRUD inMemory de viajes
- CRUD inMemory de actividades del itinerario
- validaciones en UI, ViewModel y Repository
- ajustes persistidos con `SharedPreferences`
- multi-language funcional (`en`, `es`, `ca`)
- Terms & Conditions en primer arranque
- presupuesto de viaje y costes de actividades en euros
- logs visibles en Logcat
- tests unitarios de dominio y repositorio

## Arquitectura

La app sigue una estructura MVVM sencilla:

```text
UI (Screens)
  -> ViewModel
  -> Repository
  -> DataSource
```

Piezas principales:
- `TripsViewModel`: coordina CRUD, validaciones previas y logs de viajes/itinerario.
- `SettingsViewModel`: gestiona preferencias persistidas y logs de ajustes.
- `TripRepositoryImpl`: concentra la logica de negocio de viajes y actividades.
- `FakeTripDataSource`: mantiene el estado en memoria.
- `SharedPreferencesSettingsRepository`: persiste idioma, tema, usuario y terminos aceptados.

## Funcionalidad implementada

### Viajes
- crear, editar y eliminar viajes
- titulo, descripcion, ciudad, pais, fechas, estado y presupuesto
- estado sugerido automaticamente segun fechas
- reprogramacion del viaje con opcion de mover tambien el itinerario

### Itinerario
- crear, editar y eliminar actividades
- fecha y hora mediante pickers
- categorias y plantillas predefinidas
- coste por actividad en euros

### Validaciones
- campos obligatorios
- fechas futuras
- fecha de inicio anterior a fecha final
- actividades dentro del rango del viaje
- presupuesto y coste no negativos
- mensajes de error claros en pantalla

### Ajustes
- `username`
- `dateOfBirth`
- `darkMode`
- `languageTag`
- `termsAccepted`

## Logs

Los logs solicitados por el sprint se pueden ver en Logcat con estos tags:
- `TripsViewModel`
- `SettingsViewModel`

Criterio aplicado:
- `Log.i` para operaciones correctas
- `Log.w` para validaciones rechazadas o reglas de negocio esperadas
- `Log.e` solo para fallos sin detalle o situaciones realmente anormales

## Idiomas

Los textos se gestionan con recursos Android separados:
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`
- `app/src/main/res/values-ca/strings.xml`

El cambio de idioma se aplica en tiempo de ejecucion.

## Verificacion local

Comandos usados para verificar la entrega:

```powershell
./gradlew.bat :app:assembleDebug --console=plain
./gradlew.bat :app:testDebugUnitTest --console=plain
```

## Evidencia y entrega

Ruta prevista para el video del sprint:

```text
doc/evidence/v2.0.2/
```

Version de entrega final:
- `v2.0.2`

## Estructura del repositorio

```text
BBTraveling/
|- app/
|  \- src/main/
|     |- java/com/example/bbtraveling/
|     |  |- data/
|     |  |- domain/
|     |  |- navigation/
|     |  \- ui/
|     \- res/
|- doc/
|- docs/
|- README.md
|- CONTRIBUTING.md
\- LICENSE
```

## Equipo

- Anouar El Kabiri
- Eloi Mora Palomino
