package com.chading.photos.ui.nav

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chading.photos.ui.PhotosViewModel
import com.chading.photos.ui.albums.AlbumDetailScreen
import com.chading.photos.ui.albums.AlbumsScreen
import com.chading.photos.ui.photos.PhotosScreen
import com.chading.photos.ui.viewer.ViewerScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private sealed class Tab(val route: String, val label: String) {
    data object Photos : Tab("photos", "Photos")
    data object Albums : Tab("albums", "Albums")
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChadingNav() {
    val vm: PhotosViewModel = viewModel(factory = PhotosViewModel.Factory)

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val permState = rememberMultiplePermissionsState(permissions)
    val granted = permState.permissions.any { it.status.isGranted }

    if (!granted) {
        PermissionGate(
            onGrant = { permState.launchMultiplePermissionRequest() },
        )
        return
    }

    // Trigger initial load once permission is available.
    androidx.compose.runtime.LaunchedEffect(granted) { if (granted) vm.onPermissionGranted() }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute == Tab.Photos.route || currentRoute == Tab.Albums.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val tabs = listOf(Tab.Photos, Tab.Albums)
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Tab.Photos.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        Tab.Photos -> Icons.Filled.Photo
                                        Tab.Albums -> Icons.Filled.PhotoAlbum
                                    },
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Photos.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(160)) },
        ) {
            composable(Tab.Photos.route) {
                PhotosScreen(
                    viewModel = vm,
                    contentPadding = innerPadding,
                    onOpenItem = { id -> navController.navigate("viewer/all/$id") },
                )
            }
            composable(Tab.Albums.route) {
                AlbumsScreen(
                    viewModel = vm,
                    contentPadding = innerPadding,
                    onOpenAlbum = { bucketId -> navController.navigate("album/$bucketId") },
                )
            }
            composable(
                route = "album/{bucketId}",
                arguments = listOf(navArgument("bucketId") { type = NavType.LongType }),
                enterTransition = {
                    slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 12 } +
                        fadeIn(tween(260))
                },
                exitTransition = { fadeOut(tween(160)) },
                popExitTransition = {
                    slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 12 } +
                        fadeOut(tween(220))
                },
            ) { entry ->
                val bucketId = entry.arguments?.getLong("bucketId") ?: 0L
                AlbumDetailScreen(
                    viewModel = vm,
                    bucketId = bucketId,
                    onBack = { navController.popBackStack() },
                    onOpenItem = { id -> navController.navigate("viewer/album-$bucketId/$id") },
                )
            }
            composable(
                route = "viewer/{source}/{id}",
                arguments = listOf(
                    navArgument("source") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType },
                ),
                enterTransition = {
                    fadeIn(tween(240)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing))
                },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(240)) },
                popExitTransition = {
                    fadeOut(tween(220)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                },
            ) { entry ->
                val source = entry.arguments?.getString("source") ?: "all"
                val id = entry.arguments?.getLong("id") ?: 0L
                ViewerScreen(
                    viewModel = vm,
                    source = source,
                    initialId = id,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun PermissionGate(onGrant: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Chading",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "A local-only gallery. Grant access to your photos and videos to get started — nothing leaves your device.",
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onGrant) { Text("Allow access") }
        }
    }
}
