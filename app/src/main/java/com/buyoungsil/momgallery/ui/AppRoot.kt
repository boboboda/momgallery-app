package com.buyoungsil.momgallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buyoungsil.momgallery.GalleryApp
import com.buyoungsil.momgallery.ui.admin.AdminArtworksScreen
import com.buyoungsil.momgallery.ui.admin.AdminHomeScreen
import com.buyoungsil.momgallery.ui.admin.ArtworkEditScreen
import com.buyoungsil.momgallery.ui.admin.InquiriesScreen
import com.buyoungsil.momgallery.ui.artist.ArtistScreen
import com.buyoungsil.momgallery.ui.detail.DetailScreen
import com.buyoungsil.momgallery.ui.home.HomeScreen
import com.buyoungsil.momgallery.ui.inquiry.InquiryScreen
import com.buyoungsil.momgallery.ui.login.LoginScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector) {
    // "inquiry?artworkId=..." 처럼 뒤에 값이 붙은 주소도 같은 탭으로 봐요
    fun matches(current: String) = current == route || current.startsWith("$route?")
}

private val baseTabs = listOf(
    Tab("home", "작품", Icons.Outlined.Palette),
    Tab("artist", "작가 소개", Icons.Outlined.Person),
    Tab("inquiry", "문의", Icons.Outlined.Email),
)
private val adminTab = Tab("admin", "관리", Icons.Outlined.Edit)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()

    val repo = (LocalContext.current.applicationContext as GalleryApp).repository
    val isAdmin by repo.isAdmin.collectAsStateWithLifecycle()

    val tabs = if (isAdmin) baseTabs + adminTab else baseTabs
    val showBar = tabs.any { it.matches(route) }

    // 로그아웃했거나 로그인이 풀리면 관리 화면에서 처음 화면으로 돌려보내요
    LaunchedEffect(isAdmin, route) {
        if (!isAdmin && route.startsWith("admin")) {
            nav.navigate("home") { popUpTo("home") }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tab.matches(route),
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

            // 방문자 화면
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
                    onLogin = { nav.navigate("login") },
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

            // 엄마(작가) 화면
            composable("login") {
                LoginScreen(
                    onBack = { nav.popBackStack() },
                    onSuccess = { nav.navigate("admin") { popUpTo("home") } },
                )
            }

            composable("admin") {
                AdminHomeScreen(
                    onNewArtwork = { nav.navigate("admin/artwork/0") },
                    onArtworks = { nav.navigate("admin/artworks") },
                    onInquiries = { nav.navigate("admin/inquiries") },
                )
            }

            composable("admin/artworks") {
                AdminArtworksScreen(
                    onBack = { nav.popBackStack() },
                    onNew = { nav.navigate("admin/artwork/0") },
                    onEdit = { nav.navigate("admin/artwork/$it") },
                )
            }

            composable(
                "admin/artwork/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                ArtworkEditScreen(
                    id = id.takeIf { it > 0 }, // 0이면 새 그림
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() },
                )
            }

            composable("admin/inquiries") {
                InquiriesScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
