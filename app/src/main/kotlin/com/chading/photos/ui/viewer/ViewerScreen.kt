package com.chading.photos.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewerScreen(
    viewModel: PhotosViewModel,
    source: String,
    initialId: Long,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val items: List<MediaItem> = remember(state.items, source) {
        when {
            source.startsWith("album-") -> {
                val bucketId = source.removePrefix("album-").toLongOrNull() ?: 0L
                state.items.filter { it.bucketId == bucketId }
            }
            else -> state.items
        }
    }
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }
    val startIndex = remember(items, initialId) {
        items.indexOfFirst { it.id == initialId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = startIndex) { items.size }

    var chromeVisible by remember { mutableStateOf(true) }
    var showInfo by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    // Reset vertical dismiss offset when the user flips pages.
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) { dragOffset = 0f }

    val bgAlpha by animateFloatAsState(
        targetValue = (1f - (abs(dragOffset) / 1200f)).coerceIn(0.3f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "viewerBg",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { idx -> items[idx].id },
            pageSpacing = 16.dp,
        ) { page ->
            ZoomableImage(
                item = items[page],
                onToggleChrome = { chromeVisible = !chromeVisible },
                onVerticalDrag = { dy -> dragOffset = dy },
                onDragEnd = {
                    if (abs(dragOffset) > 220f) onBack() else dragOffset = 0f
                },
                verticalOffset = dragOffset,
            )
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = slideInVertically(tween(220, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { -it } + fadeOut(tween(180)),
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    val ctx = LocalContext.current
                    IconButton(onClick = {
                        val current = items[pagerState.currentPage]
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = current.mimeType
                            putExtra(android.content.Intent.EXTRA_STREAM, current.uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        ctx.startActivity(android.content.Intent.createChooser(send, "Share"))
                    }) { Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White) }
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().padding(WindowInsets.systemBars.asPaddingValues()),
            )
        }
    }

    if (showInfo) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showInfo = false }, sheetState = sheetState) {
            InfoSheet(items[pagerState.currentPage])
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoomableImage(
    item: MediaItem,
    onToggleChrome: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    verticalOffset: Float,
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "zoom",
    )

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(item.id) {
                detectTapGestures(
                    onTap = { onToggleChrome() },
                    onDoubleTap = {
                        scale = if (scale > 1.5f) 1f else 2.5f
                        if (scale == 1f) { offsetX = 0f; offsetY = 0f }
                    },
                )
            }
            .pointerInput(item.id) {
                awaitEachGesture {
                    val first = awaitFirstDown(requireUnconsumed = false)
                    var accumY = 0f
                    var accumX = 0f
                    var dismissMode = false
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        if (zoomChange != 1f || scale > 1.05f) {
                            // Pinch / already-zoomed pan.
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            offsetX += panChange.x
                            offsetY += panChange.y
                            dismissMode = false
                        } else {
                            accumX += panChange.x
                            accumY += panChange.y
                            if (!dismissMode && abs(accumY) > abs(accumX) && abs(accumY) > 12f) {
                                dismissMode = true
                            }
                            if (dismissMode) {
                                onVerticalDrag(accumY)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    // Gesture ended
                    if (dismissMode) onDragEnd()
                    // Silence "unused" for first.
                    val _unused = first
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val ctx = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(item.uri).crossfade(true).build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = offsetX
                    translationY = offsetY + verticalOffset
                },
        )
    }
}

@Composable
private fun InfoSheet(item: MediaItem) {
    val dateFmt = remember { SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(item.displayName, style = MaterialTheme.typography.titleLarge)
        InfoRow("Captured", dateFmt.format(Date(item.dateTakenMillis)))
        InfoRow("Type", item.mimeType)
        if (item.width > 0 && item.height > 0) {
            InfoRow("Resolution", "${item.width} x ${item.height}")
        }
        InfoRow("Size", humanSize(item.sizeBytes))
        if (item.isVideo && item.durationMillis > 0) {
            InfoRow("Duration", humanDuration(item.durationMillis))
        }
        if (item.relativePath.isNotBlank()) {
            InfoRow("Path", item.relativePath + item.displayName)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun humanSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var i = 0
    while (value >= 1024 && i < units.size - 1) { value /= 1024; i++ }
    return "%.1f %s".format(value, units[i])
}

private fun humanDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
