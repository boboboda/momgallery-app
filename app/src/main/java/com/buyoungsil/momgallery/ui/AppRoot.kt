package com.buyoungsil.momgallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buyoungsil.momgallery.ui.artist.ArtistScreen
import com.buyoungsil.momgallery.ui.detail.DetailScreen
import com.buyoungsil.momgallery.ui.home.HomeScreen
import com.buyoungsil.momgallery.ui.inquiry.InquiryScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "작품", Icons.Outlined.Palette),
    Tab("artist", "작가 소개", Icons.Outlined.Person),
    Tab("inquiry", "문의", Icons.Outlined.Email),
)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()
    val showBar = tabs.any { route.startsWith(it.route) }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route.startsWith(tab.route),
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(30.dp)) },
                            label = { Text(tab.label, fontSize = 16.sp) },
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(inner)) {

            composable("home") {
                HomeScreen(onOpenArtwork = { nav.navigate("artwork/$it") })
            }

            composable(
                "artwork/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                DetailScreen(
                    id = id,
                    onBack = { nav.popBackStack() },
                    onInquiry = { artworkId, title ->
                        nav.navigate("inquiry?artworkId=$artworkId&title=${Uri.encode(title)}")
                    },
                )
            }

            composable("artist") {
                ArtistScreen(
                    onOpenArtwork = { nav.navigate("artwork/$it") },
                    onSeeWorks = { nav.navigate("home") { popUpTo("home") } },
                )
            }

            composable(
                "inquiry?artworkId={artworkId}&title={title}",
                arguments = listOf(
                    navArgument("artworkId") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { backStack ->
                val artworkId = backStack.arguments?.getInt("artworkId") ?: 0
                val title = backStack.arguments?.getString("title").orEmpty()
                InquiryScreen(
                    artworkId = artworkId.takeIf { it > 0 },
                    artworkTitle = title,
                    onDone = { nav.navigate("home") { popUpTo("home") } },
                )
            }
        }
    }
}