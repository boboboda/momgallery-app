package com.buyoungsil.momgallery.ui.home

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.ui.common.ArtworkImage
import com.buyoungsil.momgallery.ui.common.CenterLoading
import com.buyoungsil.momgallery.ui.common.ErrorBox
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.subtitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlin.math.abs

private const val QUOTE =
    "무엇이든 하얀 종이 위에 그려낼 수 있다는 것이 너무나 황홀하고 희열이 느껴집니다."

private val HeroDark = Color(0xFF101412)
private const val HERO_MILLIS = 6000

@Composable
fun HomeScreen(onOpenArtwork: (Int) -> Unit) {
    val vm = appViewModel { HomeViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    when {
        state.loading -> CenterLoading()
        state.error != null && state.artworks.isEmpty() -> ErrorBox(state.error!!, vm::load)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                Text(
                    "엄마의 그림 갤러리",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }

            if (state.selectedCategory == null && state.featured.isNotEmpty()) {
                item { Hero(state.featured, vm::imageUrl, onOpenArtwork) }
                item {
                    Text(
                        QUOTE,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 36.dp),
                    )
                }
            }

            item {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CategoryChip("전체", state.selectedCategory == null) { vm.select(null) }
                    state.categories.forEach { c ->
                        CategoryChip("${c.name} ${c.artworkCount}", state.selectedCategory == c.id) {
                            vm.select(c.id)
                        }
                    }
                }
            }

            items(state.artworks, key = { it.id }) { artwork ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenArtwork(artwork.id) }
                        .padding(horizontal = 20.dp)
                        .padding(top = 32.dp),
                ) {
                    ArtworkImage(artwork, vm.imageUrl(artwork.imageUrl), Modifier.fillMaxWidth())
                    Text(
                        artwork.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    Text(
                        artwork.subtitle(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.artworks.isEmpty()) {
                item {
                    Text(
                        "아직 올라온 그림이 없어요.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.heightIn(min = 48.dp),
    )
}

// 큰 그림 슬라이더
// - 그림 한 점이 화면 너비를 꽉 채워요 (잘리지 않아요)
// - 끝없이 이어져서, 마지막 다음에 첫 그림이 같은 방향으로 나와요
// - 아래 막대가 6초 동안 차오르면 다음 그림으로 넘어가요
@Composable
private fun Hero(items: List<Artwork>, imageUrl: (String) -> String, onOpen: (Int) -> Unit) {
    val n = items.size
    val loop = n > 1

    // 끝없이 이어지는 것처럼 보이게, 그림 수의 2000배만큼 페이지를 만들고 가운데서 시작해요.
    // (페이지 수를 너무 크게 잡으면 위치 계산이 어긋나서 그림이 두 장 걸쳐 멈춰요)
    val total = if (loop) n * 2000 else 1
    val start = if (loop) total / 2 else 0
    val pager = rememberPagerState(initialPage = start) { total }
    val progress = remember { Animatable(0f) }

    if (loop) {
        LaunchedEffect(pager.settledPage) {
            while (true) {
                progress.snapTo(0f)
                progress.animateTo(1f, tween(HERO_MILLIS, easing = LinearEasing))
                // 손가락으로 넘기는 중이면 끝날 때까지 기다려요
                snapshotFlow { pager.isScrollInProgress }.first { !it }
                try {
                    pager.animateScrollToPage(
                        pager.currentPage + 1,
                        animationSpec = tween(900, easing = FastOutSlowInEasing),
                    )
                } catch (e: CancellationException) {
                    ensureActive() // 손가락에 끊긴 경우만 계속 진행
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .background(HeroDark),
    ) {
        HorizontalPager(
            state = pager,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val artwork = items[page % n]
            val offset = (pager.currentPage - page) + pager.currentPageOffsetFraction
            HeroPage(artwork, imageUrl(artwork.imageUrl), offset) { onOpen(artwork.id) }
        }

        if (loop) {
            HeroBars(count = n, active = pager.currentPage % n) { progress.value }
        }
    }
}

@Composable
private fun HeroPage(artwork: Artwork, url: String, offset: Float, onOpen: () -> Unit) {
    val context = LocalContext.current
    val request = remember(url) { ImageRequest.Builder(context).data(url).crossfade(true).build() }

    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        // 뒤에 같은 그림을 흐리게 깔아서 분위기를 이어줘요 (안드로이드 12 이상에서 흐려져요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(28.dp)
                    .alpha(0.5f),
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0x33101412), Color(0xCC101412))),
                ),
        )

        Column {
            // 그림: 원본 비율 그대로 (넘길 때 옆으로 깔끔하게 밀려요)
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 제목: 그림이 자리 잡으면 서서히 나타나요
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .graphicsLayer {
                        alpha = (1f - abs(offset) * 1.6f).coerceIn(0f, 1f)
                        translationY = abs(offset) * 40f
                    }
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
            ) {
                Text(
                    artwork.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                    color = Color.White,
                )
                Text(
                    artwork.subtitle(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.HeroBars(count: Int, active: Int, progress: () -> Float) {
    Row(
        Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(count) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.35f)),
            ) {
                val fraction = when {
                    i < active -> 1f
                    i == active -> progress()
                    else -> 0f
                }
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .background(Color.White),
                )
            }
        }
    }
}