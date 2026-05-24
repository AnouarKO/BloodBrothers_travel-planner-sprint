# Sprint 04 - Final Report

Versión: `4.0.0`

Equipo: Anouar El Kabiri, Eloi Mora Palomino

## 1. Sprint Goal

Implementar la persistencia remota de reservas hoteleras de `BBTraveling` con Retrofit, Room, Hilt y galería local por viaje:
- Búsqueda de hoteles en London, Paris o Barcelona usando fechas.
- Reserva de habitaciones y persistencia local asociada a viajes.
- Listado y cancelación de reservas.
- Imágenes de hoteles, habitaciones y galería específica por viaje.
- Tests y documentación final del sprint.

---

## 2. Trabajo técnico extra

Antes de iniciar la implementación funcional de Sprint 04 se ha corregido el punto de mejora técnico detectado en Sprint 03:
- Eliminado el código in-memory heredado que ya no se usaba en producción.
- Sustituido el soporte de previews por un repositorio privado de preview.
- Migrada la cobertura relevante al repositorio real con Room.
- Verificado que no quedan referencias a la implementación in-memory en `app/src/main` ni en `app/src/test`.

Validación inicial:
- [x] `./gradlew.bat :app:assembleDebug --console=plain`
- [x] `./gradlew.bat :app:testDebugUnitTest --console=plain` (`41 tests`, `0 failures`, `0 errors`)
- [x] `./gradlew.bat :app:lintDebug --console=plain`

---

## 3. Organización del trabajo

Para Sprint 04 se ha organizado el trabajo por bloques funcionales para dejar claro qué parte ha asumido cada miembro:

- Anouar: limpieza técnica, persistencia Room de reservas e imágenes, repositorio de reservas, flujo de reserva/cancelación, ViewModel, tests de Repository/ViewModel y documentación final.
- Eloi: dependencias de red, módulo Retrofit/Hilt, contrato `HotelApiService`, DTOs remotos y configuración base de la API de hoteles.
- Tareas compartidas: integración de pantalla de hoteles, galería por viaje, revisión de strings, validación funcional en emulador y revisión del informe.

La validación final se recoge en la sección de tests ejecutados.

---

## 4. Sprint Backlog

Estado actual de implementación:

- [x] Dependencias Retrofit, Gson converter, OkHttp logging interceptor y Coil.
- [x] `NetworkModule` con `OkHttpClient`, `Retrofit` y `HotelApiService`.
- [x] DTOs de disponibilidad, hotel, habitación, reserva y petición de reserva.
- [x] `HotelBookingRepository` + `HotelBookingRepositoryImpl` integrando API remota y Room.
- [x] `hotel_reservations` y `trip_images` añadidas al esquema Room.
- [x] DAOs de reservas e imágenes.
- [x] Búsqueda de hoteles por ciudad y rango de fechas con date pickers.
- [x] Visualización de hoteles, habitaciones, precios e imágenes.
- [x] Reserva de habitación y creación automática de viaje local asociado.
- [x] Listado de reservas locales.
- [x] Cancelación de reserva vía API y eliminación local.
- [x] Galería específica por viaje con imágenes guardadas en Room.
- [x] Strings nuevos localizados en inglés, castellano y catalán.
- [x] Tests de DAO, Repository y ViewModel para Sprint04.

---

## 5. Arquitectura aplicada

La arquitectura se mantiene alineada con los sprints anteriores:

```text
Compose UI
  -> ViewModel
  -> Repository
  -> Retrofit API / Room
```

Piezas añadidas:

- `HotelBookingScreen`: pantalla de búsqueda, reserva y listado de reservas.
- `HotelBookingViewModel`: estado de búsqueda, reservas, mensajes y acciones de galería.
- `HotelBookingRepositoryImpl`: coordina Retrofit, Room y usuario autenticado.
- `HotelApiService`: contrato Retrofit para disponibilidad, reserva y cancelación.
- `HotelReservationEntity`: reserva local asociada a usuario y viaje.
- `TripImageEntity`: imágenes persistidas por viaje.

---

## 6. Persistencia y flujo funcional

Al reservar una habitación:

1. La app llama a la API remota con `groupId = G05`.
2. La reserva devuelta se guarda localmente.
3. Se crea un viaje local asociado al usuario autenticado.
4. Se guardan las imágenes del hotel y la habitación dentro de la galería del viaje.

Al cancelar una reserva:

1. La app llama al endpoint de cancelación.
2. Se borra la reserva local.
3. Se elimina el viaje generado y las imágenes asociadas por relación Room.

---

## 7. Tests ejecutados

Validación actual:

- [x] `./gradlew.bat :app:assembleDebug --console=plain`
- [x] `./gradlew.bat :app:testDebugUnitTest --console=plain` (`54 tests`, `0 failures`, `0 errors`)
- [x] `./gradlew.bat :app:lintDebug --console=plain`

Tests relevantes de Sprint04:

- `TravelDatabaseTest`: reservas e imágenes en Room.
- `HotelBookingRepositoryImplTest`: mapeo Retrofit, reserva, cancelación y galería.
- `HotelBookingViewModelTest`: validación de fechas, búsqueda, reserva y cancelación.

Validación manual de API:

- [x] Consulta real de disponibilidad `GET /hotels/G05/availability` con Barcelona.
- [x] Reserva temporal real `POST /hotels/G05/reserve`.
- [x] Cancelación inmediata de la reserva temporal con `DELETE /reservations/{reservationId}`.

---

## 8. Requisitos críticos del enunciado

- [x] Hilt se mantiene como librería de inyección de dependencias.
- [x] Room se mantiene como persistencia local para viajes, itinerario, reservas, imágenes, perfiles y logs.
- [x] La arquitectura sigue el flujo `View -> ViewModel -> Repository -> DB/API`.
- [x] La estructura del proyecto incluye `ui/screens`, `ui/viewmodel`, `data/repository`, `di` y `data`.
- [x] Las fechas de búsqueda/reserva se seleccionan con date pickers.
- [x] El nombre del proyecto Android se mantiene como `BBTraveling`.

---

## 9. Evidencia de video

La evidencia del Sprint 04 queda documentada en `doc/evidence/v4.0.0/README.md`.

Video:
- https://youtube.com/shorts/R1BUbUY_SOU

Nota: YouTube convierte automaticamente en Short cualquier video vertical que dure menos de 3 minutos.
