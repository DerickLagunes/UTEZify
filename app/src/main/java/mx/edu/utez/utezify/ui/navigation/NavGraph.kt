package mx.edu.utez.utezify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utez.utezify.ui.screens.HomeScreen
import mx.edu.utez.utezify.ui.screens.LoginScreen
import mx.edu.utez.utezify.ui.screens.RegisterScreen
import mx.edu.utez.utezify.ui.viewmodels.AuthViewModel
import mx.edu.utez.utezify.ui.viewmodels.PlayerViewModel
import mx.edu.utez.utezify.ui.viewmodels.SongViewModel

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun UTEZifyNavGraph(
    authViewModel: AuthViewModel,
    songViewModel: SongViewModel,
    playerViewModel: PlayerViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startDestination = if (isLoggedIn) NavRoutes.HOME else NavRoutes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                authViewModel = authViewModel,
                songViewModel = songViewModel,
                playerViewModel = playerViewModel,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
