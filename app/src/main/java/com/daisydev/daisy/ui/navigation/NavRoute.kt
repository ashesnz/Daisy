package com.daisydev.daisy.ui.navigation

import com.daisydev.daisy.R

/**
 * Class that represents a navigation route
 * @param title Title of the route
 * @param path Path to the route
 * @param icon Icon of the route (optional)
 */
sealed class NavRoute(val title: String, val path: String, val icon: Int = -1) {

    // Navigation routes for the BottomNavigation
    object Symptoms:
        NavRoute(title = "Symptoms", path = "symptoms", icon = R.drawable.ic_symptoms)
    object Tracking:
        NavRoute(title = "Tracking", path = "tracking", icon = R.drawable.ic_tracking)
    object Blog:
        NavRoute(title = "Blog", path = "blog", icon = R.drawable.ic_blog)
    object Session:
        NavRoute(title = "Session", path = "session", icon = R.drawable.ic_session)

    // Other navigation routes
    object Access:
        NavRoute(title = "Access", path = "access")
    object Login:
        NavRoute(title = "Login", path = "login")
    object Register:
        NavRoute(title = "Register", path = "register")

    object Camera:
        NavRoute(title = "Camera", path = "camera")

    object PlantInfo:
        NavRoute(title = "PlantInfo", path = "plantInfo")

    object EntryBlog:
        NavRoute(title = "EntryBlog", path = "entryBlog")

    object PlantCareTracking:
        NavRoute(title = "PlantCareTracking", path = "plantCareTracking")


    companion object {
        // Function that returns a list of NavRoute for the BottomNavigation
        fun getBottomNavRoutes(): List<NavRoute> {
            return listOf(
                Symptoms,
                Tracking,
                Blog,
                Session
            )
        }

        // Function that returns a list of NavRoute that need full screen
        fun getFullScreenPaths(): List<String> {
            return listOf(
                Camera.path,
                EntryBlog.path
            )
        }
    }
}