package com.example.gestiondescommandes.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.R
import com.example.gestiondescommandes.data.Priority

private data class Dest(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun AppNav(vm: MainViewModel) {

    val navController = rememberNavController()
    val state by vm.state.collectAsState()

    // ✅ Déclarer urgentCount AVANT de l'utiliser
    val urgentCount = state.orders.count { it.priority == Priority.URGENT }

    val tabs = listOf(
        Dest(
            route = "orders",
            label = "Commandes",
            icon = {
                BadgedBox(
                    badge = {
                        if (urgentCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) { Text(urgentCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                }
            }
        ),
        Dest(
            route = "config",
            label = "Conteneurs",
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) }
        ),
        Dest(
            route = "plan",
            label = "Plan",
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_truck),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary // ✅ couleur via thème
                )
            }
        )
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "orders"
    val isTabRoute = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTabRoute) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    launchSingleTop = true
                                    popUpTo("orders") { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = tab.icon,
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "orders",
            modifier = Modifier
        ) {

            composable("orders") {
                OrdersListScreenV2(
                    vm = vm,
                    padding = paddingValues,
                    onOpenDetail = { id -> navController.navigate("detail/$id") }
                )
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                OrderDetailScreenV2(
                    vm = vm,
                    orderId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("config") {
                ConfigScreenV2(vm = vm, padding = paddingValues)
            }

            composable("plan") {
                PlanScreenV2(vm = vm, padding = paddingValues)
            }
        }
    }
}