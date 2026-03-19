package com.example.gestiondescommandes.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val state by vm.state.collectAsStateWithLifecycle()
    val urgentCount = state.orders.count { it.priority == Priority.URGENT }

    val tabs = listOf(
        Dest(
            route = "home",
            label = "Accueil",
            icon = { Icon(Icons.Filled.Home, contentDescription = null) }
        ),
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
                    Icon(Icons.Filled.Description, contentDescription = null)
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("/") ?: "home"
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
            startDestination = "home",
            modifier = Modifier
        ) {
            composable("home") {
                HomeScreenV2(
                    vm = vm,
                    padding = paddingValues,
                    onOpenAbout = { navController.navigate("about") },
                    onOpenManual = { navController.navigate("manual") }
                )
            }

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

            composable(
                route = "container/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { entry ->
                val index = entry.arguments?.getInt("index") ?: 0
                val container = state.currentPlan?.containers?.getOrNull(index)

                if (container != null) {
                    ContainerScreen(
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }
                else Text("Conteneur introuvable")
            }

            composable("plan") {
                PlanScreenV2(vm = vm, padding = paddingValues, navController = navController)
            }

            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable("manual") {
                ManualScreen(onBack = { navController.popBackStack() })
            }

            composable("deferred-orders") {
                DeferredOrdersScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

