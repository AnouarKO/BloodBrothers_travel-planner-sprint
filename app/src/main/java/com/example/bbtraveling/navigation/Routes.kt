package com.example.bbtraveling.navigation

object Routes {
    const val Splash = "splash"
    const val Main = "main"

    const val Home = "home"
    const val Trips = "trips"
    const val Gallery = "gallery"
    const val Settings = "settings"
    const val Preferences = "preferences"
    const val About = "about"
    const val Terms = "terms"
    const val TermsOnboarding = "termsOnboarding"

    const val ARG_TRIP_ID = "tripId"
    const val TripDetail = "tripDetail/{$ARG_TRIP_ID}"
    const val GalleryTrip = "gallery/{$ARG_TRIP_ID}"

    fun tripDetail(tripId: String) = "tripDetail/$tripId"
    fun galleryTrip(tripId: String) = "gallery/$tripId"
}
