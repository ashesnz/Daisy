package com.daisydev.daisy.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.daisydev.daisy.ui.compose.blog.BlogScreen
import com.daisydev.daisy.ui.compose.blog.EntryBlogScreen
import com.daisydev.daisy.ui.compose.recognition.CamaraScreen
import com.daisydev.daisy.ui.compose.tracking.TrackingScreen
import com.daisydev.daisy.ui.compose.session.AccessScreen
import com.daisydev.daisy.ui.compose.session.LoginScreen
import com.daisydev.daisy.ui.compose.session.RegisterScreen
import com.daisydev.daisy.ui.compose.session.SessionScreen
import com.daisydev.daisy.ui.compose.symptoms.SymptomsScreen
import com.daisydev.daisy.ui.compose.symptoms.PlantScreen
import com.daisydev.daisy.ui.feature.blog.BlogSharedViewModel
import com.daisydev.daisy.ui.compose.tracking.PlantTracking


/**
 * Function that contains the navigation graph of the application.
 */
@Composable
fun NavGraph(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    val blogSharedViewModel: BlogSharedViewModel = hiltViewModel() // For blog section

    NavHost(navController, startDestination = NavRoute.Symptoms.path) {
        composable(NavRoute.Symptoms.path) {
            SymptomsScreen(navController = navController)
        }

        composable(NavRoute.Tracking.path) {
            TrackingScreen(navController = navController)
        }

        composable(NavRoute.Blog.path) {
            BlogScreen(
                navController = navController,
                sharedViewModel = blogSharedViewModel,
                snackbarHostState = snackbarHostState
            )
        }

        composable(NavRoute.EntryBlog.path) {
            EntryBlogScreen(
                navController = navController,
                viewModel = blogSharedViewModel,
                snackbarHostState = snackbarHostState
            )
        }

        composable(NavRoute.Session.path) {
            SessionScreen(navController = navController)
        }

        composable(NavRoute.Access.path) {
            AccessScreen(navController = navController)
        }

        composable(NavRoute.Login.path) {
            LoginScreen(navController = navController, snackbarHostState)
        }

        composable(NavRoute.Register.path) {
            RegisterScreen(navController = navController, snackbarHostState)
        }

        composable(NavRoute.Camera.path) {
            CamaraScreen(navController = navController, snackbarHostState)
        }

        composable(
            route = "plantInfo/{name}/{nameC}/{body}/{uses}/{encodedUrl}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("nameC") { type = NavType.StringType },
                navArgument("body") { type = NavType.StringType },
                navArgument("uses") { type = NavType.StringType },
                navArgument("encodedUrl") { type = NavType.StringType }
            )
        )
        {
            val data_name = it.arguments?.getString("name")
            val data_nameC = it.arguments?.getString("nameC")
            val data_body = it.arguments?.getString("body")
            val data_uses = it.arguments?.getString("uses")
            val data_url = it.arguments?.getString("encodedUrl")
            requireNotNull(data_name)
            requireNotNull(data_nameC)
            requireNotNull(data_body)
            requireNotNull(data_uses)
            requireNotNull(data_url)
            PlantScreen(
                navController = navController,
                name = data_name,
                scientificName = data_nameC,
                body = data_body,
                uses = data_uses,
                url = data_url
            )
        }
        composable(
            route = "plantCares/{name}/{nameC}/{body}/{care}/{encodedUrl}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("nameC") { type = NavType.StringType },
                navArgument("body") { type = NavType.StringType },
                navArgument("care") { type = NavType.StringType },
                navArgument("encodedUrl") { type = NavType.StringType }
            )
        ) { it ->
            val data_name = it.arguments?.getString("name")
            val data_nameC = it.arguments?.getString("nameC")
            val data_body = it.arguments?.getString("body")
            val data_care = it.arguments?.getString("care")?.split("\n")
            val data_url = it.arguments?.getString("encodedUrl")
            requireNotNull(data_name)
            requireNotNull(data_nameC)
            requireNotNull(data_body)
            requireNotNull(data_care)
            requireNotNull(data_url)
            PlantTracking(
                navController = navController,
                name = data_name,
                nameC = data_nameC,
                body = data_body,
                care = data_care,
                url = data_url
            )
        }


    }
}