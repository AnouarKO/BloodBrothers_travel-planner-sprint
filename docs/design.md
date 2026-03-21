# Sprint 01 - Decisiones de diseno

## 1. Objetivo

Definir una base de app Travel Planner mock que sea:
- Clara en navegacion
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

Esta separacion permite evolucionar UI y datos sin acoplamiento fuerte.

---

## 3. Modelo de navegacion

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
- `Photo`: item de galeria

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
- Tarjetas y jerarquia clara
- Preferencias con idioma mock: `English`, `Espanol`, `Catalan`

---

## 7. Actualizacion Sprint 02 (Logic)

Arquitectura implementada para el segundo sprint:

- `UI -> ViewModel -> Repository -> DataSource`
- `FakeTripDataSource` como almacenamiento in-memory
- `TripRepository` + `TripRepositoryImpl` para CRUD de viajes y actividades
- `SharedPreferencesSettingsRepository` para persistir ajustes de usuario

Se anadieron validaciones funcionales para:

- campos obligatorios
- fechas de viaje (inicio < fin y futuras)
- fechas de actividad dentro del rango del viaje

Ademas:

- Se aplico soporte multiidioma real (`en`, `es`, `ca`) con recursos por locale
- Se añadieron logs de operaciones y errores de validacion para Logcat
- Se incorporaron pruebas unitarias de CRUD y validaciones base
