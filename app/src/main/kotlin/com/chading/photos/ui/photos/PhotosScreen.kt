package com.chading.photos.ui.photos

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chading.photos.data.MediaItem
import com.chading.photos.ui.PhotosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotosScreen(
    viewModel: PhotosViewModel,
    contentPadding: PaddingValues,
    onOpenItem: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var columns by rememberSaveable { mutableStateOf(3) }
    val animatedColumns by animateIntAsState(
        targetValue = columns,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "gridColumns",
    )
    val spacing by animateDpAsState(
        targetValue = when (columns) { 2 -> 4.dp; 3 -> 3.dp; 4 -> 2.dp; else -> 1.dp },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "gridSpacing",
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedPinchToZoom(
            onZoom = { zoom ->
                columns = (columns + if (zoom > 1.05f) -1 else if (zoom < 0.95f) 1 else 0)
                    .coerceIn(2, 5)
            },
        ),
        topBar = {
            LargeTopAppBar(
                title = { Text("Photos") },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.items.isEmpty() -> {
                    Text(
                        text = "No photos or videos yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    val grouped = remember(state.items) { groupByDay(state.items) }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(animatedColumns.coerceAtLeast(2)),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = contentPadding.calculateBottomPadding() + 16.dp,
                            start = 4.dp,
                            end = 4.dp,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        grouped.forEach { (header, items) ->
                            item(key = "hdr-$header", span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                )
                            }
                            items(items, key = { it.id }) { item ->
                                Thumbnail(item = item, onClick = { onOpenItem(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(item: MediaItem, onClick: () -> Unit) {
    val ctx = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color(0x99000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = formatDuration(item.durationMillis),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun Modifier.nestedPinchToZoom(onZoom: (Float) -> Unit): Modifier = this.pointerInput(Unit) {
    detectTransformGestures { _, _, zoom, _ -> if (zoom != 1f) onZoom(zoom) }
}

private fun groupByDay(items: List<MediaItem>): List<Pair<String, List<MediaItem>>> {
    val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val out = LinkedHashMap<String, MutableList<MediaItem>>()
    for (item in items) {
        val key = sdf.format(Date(item.dateTakenMillis))
        out.getOrPut(key) { mutableListOf() }.add(item)
    }
    return out.toList()
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return ""
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
