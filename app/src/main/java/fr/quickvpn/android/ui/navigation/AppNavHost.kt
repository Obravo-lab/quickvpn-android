package fr.quickvpn.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.quickvpn.android.AppGraph
import fr.quickvpn.android.ui.screens.account.AccountScreen
import fr.quickvpn.android.ui.screens.auth.LoginScreen
import fr.quickvpn.android.ui.screens.auth.RegisterScreen
import fr.quickvpn.android.ui.screens.home.HomeVpnScreen
import fr.quickvpn.android.ui.screens.onboarding.OnboardingScreen
import fr.quickvpn.android.ui.screens.plans.PlansScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PLANS = "plans"
    const val ACCOUNT = "account"
}

@Composable
fun AppNavHost() {
    val navController: NavHostController = rememberNavController()
    val tokenStore = AppGraph.ApiClient.current.tokenStore
    val start = if (tokenStore.token != null) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onGoForgot = {}
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            HomeVpnScreen(
                onLoggedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onGoPlans = { navController.navigate(Routes.PLANS) },
                onGoAccount = { navController.navigate(Routes.ACCOUNT) }
            )
        }
        composable(Routes.PLANS) {
            PlansScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.ACCOUNT) { inclusive = true }
                    }
                },
                onDeleted = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.ACCOUNT) { inclusive = true }
                    }
                }
            )
        }
    }
}
