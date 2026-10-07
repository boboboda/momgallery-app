package com.buyoungsil.momgallery.ui.artist

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buyoungsil.momgallery.data.Artwork
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.appViewModel

// 작가님이 쓰신 글 그대로예요. 한 문단을 앞뒤로 나눠서 뒷부분을 큰 인용구로 보여줘요.
private const val P1 = "저는 65세 요양보호사 일을 하며 틈틈이 그림 그리는 걸 좋아하는 사람입니다."
private const val P2 =
    "어릴 때부터 그림을 좋아했지만 형편상 시기를 놓치고, 생활이 바쁘다 보니 못 그렸던 것을 이제라도 즐거운 마음으로 그려보려 합니다."
private const val P3 = "그림은 그냥 그리고 싶은 사물이든지 상상이든지 그림이 주는 신비로움이 있다고 생각합니다."
private const val QUOTE =
    "무엇이든 하얀 종이 위에 그려낼 수 있다는 것이 너무나 황홀하고 희열이 느껴지는, 그런 것이 저의 그림에 대한 생각입니다."

// 액자 속 흰 종이(매트) 색. 사이트와 같아요.
private val Mat = Color(0xFFF1F0EB)

@Composable
fun ArtistScreen(
    onOpenArtwork: (Int) -> Unit,
    onSeeWorks: () -> Unit,
    onLogin: () -> Unit,
) {
    val vm = appViewModel { ArtistViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()

    val body = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = FontFamily.Serif,
        lineHeight = 36.sp,
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("작가 소개", style = MaterialTheme.typography.displaySmall)
        Text(
            "그림으로 꿈을 그리는 요양보호사",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        // 대표 작품: 액자에 걸린 것처럼
        // 액자 크기는 그림이 오기 전에도 늘 같아요. 그래서 그림이 뜰 때 글이 밀리지 않아요.
        if (state.loading || state.featured != null) {
            val artwork = state.featured
            Spacer(Modifier.height(32.dp))
            Column(
                Modifier.clickable(enabled = artwork != null) { artwork?.let { onOpenArtwork(it.id) } },
            ) {
                FeaturedFrame(artwork = artwork, url = artwork?.let { vm.imageUrl(it.imageUrl) })
                // 제목 자리도 미리 잡아 둬요 (제목 한 줄 + "대표 작품" 한 줄)
                if (artwork != null) {
                    Text(
                        artwork.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    Text(
                        "대표 작품",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        " ",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .width(160.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Text(" ", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(44.dp))
        Text(P1, style = body)
        Spacer(Modifier.height(24.dp))
        Text(P2, style = body)
        Spacer(Modifier.height(24.dp))
        Text(P3, style = body)

        // 큰 인용구
        Spacer(Modifier.height(36.dp))
        Row(Modifier.height(IntrinsicSize.Min)) {
            Spacer(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
            Text(
                QUOTE,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    fontSize = 26.sp,
                    lineHeight = 42.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.padding(start = 18.dp),
            )
        }

        Spacer(Modifier.height(48.dp))
        BigButton("작품 보기", onSeeWorks, outlined = true)

        // 작가(엄마)만 쓰는 로그인. 로그인한 폰에서는 보이지 않아요.
        if (!isAdmin) {
            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    "작가 로그인",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}


// 항상 같은 크기(4:3)의 액자. 그림은 잘리지 않게 안쪽에 맞춰 넣고,
// 불러오는 동안에는 반짝이는 빈 자리(스켈레톤)를 보여줘요.
@Composable
private fun FeaturedFrame(artwork: Artwork?, url: String?) {
    var loaded by remember(url) { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmerAlpha",
    )

    Box(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(Mat)
            .padding(14.dp)
            .aspectRatio(4f / 3f),
        contentAlignment = Alignment.Center,
    ) {
        if (!loaded) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = alpha * 0.35f)),
            )
        }
        if (artwork != null && url != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                contentDescription = artwork.title,
                contentScale = ContentScale.Fit,
                onSuccess = { loaded = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
