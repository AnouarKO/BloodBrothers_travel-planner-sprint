package com.example.bbtraveling.data

import com.example.bbtraveling.domain.Activity
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.Photo
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripStatus
import java.time.LocalDate
import java.time.LocalTime

object MockData {

    fun initialTrips(): List<Trip> {
        val baseDate = LocalDate.now().plusMonths(1)
        val romeStart = baseDate.plusDays(7)
        val barcelonaStart = baseDate.plusDays(28)
        val parisStart = baseDate.plusDays(46)
        val lisbonStart = baseDate.plusDays(72)

        return listOf(
            Trip(
                id = "t1",
                title = "Weekend in Rome",
                startDate = romeStart,
                endDate = romeStart.plusDays(2),
                description = "A compact cultural escape with landmarks, food and a sunset walk.",
                destination = "Rome, Italy",
                status = TripStatus.Upcoming,
                accommodation = "Trastevere Urban Suites",
                transport = "Direct flight from Barcelona",
                travelers = 2,
                budgetEur = 420.0,
                activities = listOf(
                    Activity(
                        id = "a1",
                        title = "Colosseum Entry",
                        description = "Historic Center guided visit",
                        date = romeStart,
                        time = LocalTime.of(9, 0),
                        category = ActivityCategory.Museum,
                        costEur = 42.0
                    ),
                    Activity(
                        id = "a2",
                        title = "Trastevere Lunch",
                        description = "Lunch near Via della Scala",
                        date = romeStart,
                        time = LocalTime.of(13, 30),
                        category = ActivityCategory.Restaurant,
                        costEur = 36.0
                    ),
                    Activity(
                        id = "a3",
                        title = "Evening Walk",
                        description = "Gelato and walk in Piazza Navona",
                        date = romeStart.plusDays(1),
                        time = LocalTime.of(19, 45),
                        category = ActivityCategory.Leisure,
                        costEur = 18.0
                    )
                ),
                photos = listOf(
                    Photo("p1", "Arrival day", "Fiumicino transfer", android.R.drawable.ic_menu_myplaces),
                    Photo("p2", "Forum route", "Ancient Rome", android.R.drawable.ic_menu_compass),
                    Photo("p3", "Dinner stop", "Trastevere", android.R.drawable.ic_menu_camera)
                )
            ),
            Trip(
                id = "t2",
                title = "Barcelona Escape",
                startDate = barcelonaStart,
                endDate = barcelonaStart.plusDays(4),
                description = "Beach, architecture and relaxed evenings by the sea.",
                destination = "Barcelona, Spain",
                status = TripStatus.Planning,
                accommodation = "Hotel Jazz Barcelona",
                transport = "High-speed train",
                travelers = 3,
                budgetEur = 780.0,
                activities = listOf(
                    Activity(
                        id = "a4",
                        title = "Sagrada Familia Visit",
                        description = "Main basilica guided pass",
                        date = barcelonaStart,
                        time = LocalTime.of(10, 0),
                        category = ActivityCategory.Museum,
                        costEur = 35.0
                    ),
                    Activity(
                        id = "a5",
                        title = "Beach and Snack",
                        description = "Relaxed afternoon in Barceloneta",
                        date = barcelonaStart.plusDays(1),
                        time = LocalTime.of(15, 0),
                        category = ActivityCategory.Leisure,
                        costEur = 22.0
                    ),
                    Activity(
                        id = "a6",
                        title = "Tapas Dinner",
                        description = "Evening in El Born",
                        date = barcelonaStart.plusDays(2),
                        time = LocalTime.of(21, 0),
                        category = ActivityCategory.Restaurant,
                        costEur = 48.0
                    ),
                    Activity(
                        id = "a7",
                        title = "Night Viewpoint",
                        description = "Bunkers del Carmel skyline",
                        date = barcelonaStart.plusDays(3),
                        time = LocalTime.of(23, 0),
                        category = ActivityCategory.Leisure,
                        costEur = 12.0
                    )
                ),
                photos = listOf(
                    Photo("p1", "Morning route", "Passeig de Gracia", android.R.drawable.ic_menu_mapmode),
                    Photo("p2", "Golden hour", "Barceloneta", android.R.drawable.ic_menu_gallery),
                    Photo("p3", "Gaudi details", "Sagrada Familia", android.R.drawable.ic_menu_camera)
                )
            ),
            Trip(
                id = "t3",
                title = "Paris Museum Day",
                startDate = parisStart,
                endDate = parisStart.plusDays(1),
                description = "A museum-focused city break with metro passes and a late dinner.",
                destination = "Paris, France",
                status = TripStatus.Upcoming,
                accommodation = "Hotel des Arts Montmartre",
                transport = "Morning flight and metro pass",
                travelers = 2,
                budgetEur = 350.0,
                activities = listOf(
                    Activity(
                        id = "a8",
                        title = "Louvre Ticket",
                        description = "Main hall and temporary exhibition",
                        date = parisStart,
                        time = LocalTime.of(11, 0),
                        category = ActivityCategory.Museum,
                        costEur = 31.0
                    ),
                    Activity(
                        id = "a9",
                        title = "Metro Day Pass",
                        description = "Unlimited transport pass",
                        date = parisStart,
                        time = LocalTime.of(14, 0),
                        category = ActivityCategory.Transport,
                        costEur = 11.0
                    ),
                    Activity(
                        id = "a10",
                        title = "Orsay Quick Visit",
                        description = "Focused visit to top collections",
                        date = parisStart,
                        time = LocalTime.of(17, 30),
                        category = ActivityCategory.Museum,
                        costEur = 18.0
                    ),
                    Activity(
                        id = "a11",
                        title = "Bistro Dinner",
                        description = "Late dinner in Saint-Germain",
                        date = parisStart,
                        time = LocalTime.of(20, 30),
                        category = ActivityCategory.Restaurant,
                        costEur = 54.0
                    )
                ),
                photos = listOf(
                    Photo("p1", "Museum pass", "Louvre", android.R.drawable.ic_menu_report_image),
                    Photo("p2", "Seine view", "Pont Neuf", android.R.drawable.ic_menu_slideshow),
                    Photo("p3", "Evening cafe", "Saint-Germain", android.R.drawable.ic_menu_gallery)
                )
            ),
            Trip(
                id = "t4",
                title = "Lisbon Food Notes",
                startDate = lisbonStart,
                endDate = lisbonStart.plusDays(3),
                description = "A mock foodie plan with tram rides, viewpoints and local pastries.",
                destination = "Lisbon, Portugal",
                status = TripStatus.Draft,
                accommodation = "LX Boutique Hotel",
                transport = "Budget airline and city tram",
                travelers = 4,
                budgetEur = 610.0,
                activities = listOf(
                    Activity(
                        id = "a12",
                        title = "Pastel de Nata Stop",
                        description = "Morning pastry route in Belem",
                        date = lisbonStart,
                        time = LocalTime.of(8, 30),
                        category = ActivityCategory.Restaurant,
                        costEur = 14.0
                    ),
                    Activity(
                        id = "a13",
                        title = "Tram 28 Route",
                        description = "Alfama and central tram line",
                        date = lisbonStart.plusDays(1),
                        time = LocalTime.of(12, 0),
                        category = ActivityCategory.Transport,
                        costEur = 9.0
                    ),
                    Activity(
                        id = "a14",
                        title = "Sunset Miradouro",
                        description = "Santa Luzia viewpoint",
                        date = lisbonStart.plusDays(1),
                        time = LocalTime.of(18, 0),
                        category = ActivityCategory.Leisure,
                        costEur = 0.0
                    ),
                    Activity(
                        id = "a15",
                        title = "Seafood Dinner",
                        description = "Dinner in Cais do Sodre",
                        date = lisbonStart.plusDays(2),
                        time = LocalTime.of(21, 15),
                        category = ActivityCategory.Restaurant,
                        costEur = 62.0
                    )
                ),
                photos = listOf(
                    Photo("p1", "Yellow tram", "Alfama", android.R.drawable.ic_menu_directions),
                    Photo("p2", "Belvedere", "Santa Luzia", android.R.drawable.ic_menu_compass),
                    Photo("p3", "Dinner table", "Ribeira", android.R.drawable.ic_menu_camera)
                )
            )
        )
    }

    fun allPhotos(trips: List<Trip>): List<Photo> = trips.flatMap { it.photos }
}
