# Sprint 01 - Decisiones de diseño

## 1. Objetivo

Definir una base de app Travel Planner mock que sea:
- Clara en navegación
- Coherente visualmente
- Modular por capas
- Escalable para Sprint 02

---

## 2. Arquitectura

Estructura usada en el proyecto:

- `ui/`: pantallas y componentes Compose
- `navigation/`: rutas y grafos
- `data/`: datos mock hardcoded
- `domain/`: entidades y funciones de negocio

Esta separación permite evolucionar UI y datos sin acoplamiento fuerte.

---

## 3. Modelo de navegación

### Root graph

- `Splash`
- `Main`
- `TripDetail/{tripId}`
- `Gallery/{tripId}`
- `Terms`

### Bottom navigation (`MainShell`)

- `Home`
- `Trips`
- `Gallery`
- `Settings`

### Flujo de pantallas (Mermaid)

```mermaid
flowchart TD
    A[Splash] --> B[Main Shell]

    subgraph MainTabs [Bottom Navigation]
        B --> H[Home]
        B --> TR[Trips]
        B --> G[Gallery]
        B --> S[Settings]
    end

    H --> TD[Trip Detail]
    H --> TR
    TR --> TD
    TD --> TG[Trip Gallery]
    S --> P[Preferences]
    S --> AB[About]
    S --> T[Terms & Conditions]
    AB --> T
```

---

## 4. Diagrama UML de app (pantallas + acciones)

Este diagrama extiende el flujo anterior con atributos y funciones principales.

```mermaid
classDiagram
direction LR

class SplashScreen {
  +onFinished()
}

class MainShell {
  +navigate(route:String)
}

class HomeScreen {
  +trips: List~Trip~
  +onTripClick(tripId:String)
  +onOpenTrips()
}

class TripsScreen {
  +trips: List~Trip~
  +selectedFilter: Int
  +filterBy(status:String)
  +onTripClick(tripId:String)
}

class TripDetailScreen {
  +tripId: String
  +selectedTab: Int
  +onOpenGallery()
  +onBack()
}

class GalleryScreen {
  +tripId: String
  +photos: List~Photo~
  +onAddPhotoClick()
  +onDeletePhotoClick(photoId:String)
  +onBack()
}

class SettingsScreen {
  +onOpenPreferences()
  +onOpenAbout()
  +onOpenTerms()
}

class PreferencesScreen {
  +selectedLanguage: String
  +darkTheme: Boolean
  +notifications: Boolean
  +setLanguage(language:String)
  +toggleTheme()
  +toggleNotifications()
}

class AboutScreen {
  +onOpenTerms()
  +onBack()
}

class TermsScreen {
  +onAccept()
  +onReject()
}

class MockData {
  +trips: List~Trip~
  +tripById(id:String): Trip?
  +allPhotos(): List~Photo~
}

class Trip {
  +id:String
  +title:String
  +destination:String
  +status:String
  +startDate:String
  +endDate:String
  +summary:String
  +accommodation:String
  +transport:String
  +travelers:Int
  +budgetEur:Double
  +activities:List~Activity~
  +photos:List~Photo~
  +spentEur:Double
  +remainingEur:Double
  +isOverBudget():Boolean
  +averageActivityCost():Double
  +projectedDailyBudget(totalDays:Int):Double
}

class Activity {
  +time:String
  +title:String
  +location:String
  +costEur:Double
}

class Photo {
  +id:String
  +title:String
  +spot:String
  +resId:Int
}

MainShell --> HomeScreen
MainShell --> TripsScreen
MainShell --> GalleryScreen
MainShell --> SettingsScreen
HomeScreen --> TripDetailScreen
TripsScreen --> TripDetailScreen
TripDetailScreen --> GalleryScreen
SettingsScreen --> PreferencesScreen
SettingsScreen --> AboutScreen
SettingsScreen --> TermsScreen
AboutScreen --> TermsScreen

HomeScreen --> MockData
TripsScreen --> MockData
TripDetailScreen --> MockData
GalleryScreen --> MockData

MockData --> Trip
Trip "1" *-- "0..*" Activity
Trip "1" *-- "0..*" Photo
```

---

## 5. Modelo de dominio

Entidades principales:

- `Trip`: agregado principal del viaje
- `Activity`: item de itinerario con coste
- `Photo`: item de galería

Funciones de `Trip` en Sprint 01:

- `spentEur`
- `remainingEur`
- `isOverBudget()`
- `averageActivityCost()`
- `projectedDailyBudget(totalDays)`

El diagrama de dominio detallado se mantiene en `docs/domain-model.mmd`.

---

## 6. UI y tema

- Base visual Material 3
- Identidad morado/amarillo
- Tarjetas y jerarquía clara
- Preferencias con idioma mock: `English`, `Español`, `Català`

---

## 7. Actualización Sprint 02 (Logic)

Arquitectura implementada para el segundo sprint:

- `UI -> ViewModel -> Repository -> DataSource`
- DataSource in-memory como almacenamiento temporal de Sprint 02
- `TripRepository` con implementación in-memory para CRUD de viajes y actividades
- `SharedPreferencesSettingsRepository` para persistir ajustes de usuario

Se añadieron validaciones funcionales para:

- campos obligatorios
- fechas de viaje (inicio < fin y futuras)
- fechas de actividad dentro del rango del viaje

Además:

- Se aplicó soporte multiidioma real (`en`, `es`, `ca`) con recursos por locale
- Se añadieron logs de operaciones y errores de validación para Logcat
- Se incorporaron pruebas unitarias de CRUD y validaciones base

---

## 8. Actualización Sprint 03 (Room, Hilt y Firebase)

La arquitectura de producción queda como:

```text
Compose UI
  -> ViewModel
  -> Repository
  -> Room / Firebase / SharedPreferences
```

### Inyección de dependencias

Se usa Hilt como librería DI:

- `BBTravelingApplication` con `@HiltAndroidApp`.
- `AppModule` provee `TravelDatabase`, DAOs, `FirebaseAuth` y `Clock`.
- `RepositoryModule` enlaza interfaces de dominio con implementaciones Room/Firebase.

### Esquema Room

Base de datos: `bbtraveling.db`

Tablas:

- `users`
  - `login` como primary key.
  - `username` único.
  - `birthdate`, `address`, `country`, `phone`, `acceptsReceiveEmails`.
- `trips`
  - `id` como primary key.
  - `ownerLogin` referencia a `users.login`.
  - campos de viaje: título, descripción, ciudad, país, fechas, estado, presupuesto, etc.
- `itinerary_items`
  - `id` como primary key.
  - `tripId` referencia a `trips.id`.
  - campos de actividad: título, descripción, fecha, hora, categoría y coste.
- `access_logs`
  - `id` autogenerado.
  - `userId`, `eventType`, `occurredAt`.

Relaciones:

```text
users.login 1 --- N trips.ownerLogin
trips.id    1 --- N itinerary_items.tripId
```

### Estrategia de uso

- Al registrar un usuario, Firebase crea la cuenta y Room guarda el perfil local.
- El registro valida todos los campos obligatorios, confirmación de contraseña y edad mínima de 18 años.
- Al hacer login, solo se acepta el usuario si el email está verificado y existe perfil local.
- Los viajes nuevos guardan `ownerLogin` con el email del usuario autenticado.
- La pantalla de viajes observa solo `trips.ownerLogin = currentUser.login`.
- Los eventos login/logout se insertan en `access_logs`.

### Migración

Durante Sprint 03 se ha usado `fallbackToDestructiveMigration(false)` porque es un prototipo académico y no se requiere conservar bases antiguas de sprints previos. Para una entrega real, el siguiente paso sería declarar migraciones Room versionadas y exportar schema.

### Tests

La cobertura del Sprint 03 incluye unit tests JVM con Robolectric para:

- `ExampleUnitTest`: entorno básico.
- `TripTest`: cálculos de dominio.
- `TravelDatabaseTest`: Room Database, DAOs y relaciones.
- `RoomTripRepositoryTest`: CRUD persistente, filtrado por usuario autenticado, edición, borrado y validaciones de trips e itinerario.
- `RoomUserProfileRepositoryTest`: mapeo y observación de perfil local.
- `AuthViewModelTest`: login, registro, recover password, confirmación de password y edad mínima.
- `SettingsViewModelTest`: perfil autenticado, idioma, tema y preferencias sin cuenta.
- `TripsViewModelTest`: carga, creación y validaciones desde ViewModel.
- `MainDispatcherRule`: soporte para corrutinas en tests.

---

## 9. Actualización Sprint 04 (Retrofit, reservas e imágenes)

La arquitectura se amplía manteniendo el mismo flujo por capas:

```text
Compose UI
  -> ViewModel
  -> Repository
  -> Retrofit API / Room
```

### Red y DI

- `NetworkModule` provee `OkHttpClient`, `Retrofit` y `HotelApiService`.
- `RepositoryModule` enlaza `HotelBookingRepository` con `HotelBookingRepositoryImpl` usando `@Binds`.
- La API remota se consume con Retrofit, Gson y llamadas suspendidas.
- Se habilita cleartext traffic porque la API académica del Sprint04 usa HTTP.

### Persistencia Room

Se añaden dos tablas:

- `hotel_reservations`
  - Guarda la reserva remota, hotel, habitación, fechas, huésped y relación con `tripId`.
  - Se asocia al usuario autenticado mediante `ownerLogin`.
- `trip_images`
  - Guarda URIs/referencias de imágenes para cada viaje.
  - Incluye imágenes devueltas por la API y las añadidas desde la galería.

Relaciones nuevas:

```text
users.login 1 --- N hotel_reservations.ownerLogin
trips.id    1 --- N hotel_reservations.tripId
users.login 1 --- N trip_images.ownerLogin
trips.id    1 --- N trip_images.tripId
```

### Pantallas

- `HotelBookingScreen`: búsqueda de hoteles por ciudad y fechas con date pickers.
- `HotelBookingScreen`: listado de reservas locales y cancelación de reservas.
- `GalleryScreen`: galería específica por viaje con imágenes persistidas en Room.

### Estrategia de reserva

Al reservar una habitación:

1. Retrofit crea la reserva remota.
2. Room crea un viaje local con destino, fechas, hotel, habitación y presupuesto calculado por noches.
3. Room guarda la reserva en `hotel_reservations`.
4. Room guarda imágenes del hotel/habitación en `trip_images`.

Al cancelar:

1. Se llama al endpoint de cancelación.
2. Se elimina la reserva local.
3. Se elimina el viaje generado y sus imágenes asociadas por cascada.

### Tests

La cobertura del Sprint04 añade:

- `HotelBookingRepositoryImplTest`: mapeo DTO-domain, reserva, cancelación y galería.
- `HotelBookingViewModelTest`: validación de fechas, búsqueda, reserva y cancelación.
- `TravelDatabaseTest`: DAOs de reservas e imágenes.
