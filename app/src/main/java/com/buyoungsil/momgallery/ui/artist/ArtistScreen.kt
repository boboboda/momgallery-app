package com.buyoungsil.momgallery.ui.artist

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.buyoungsil.momgallery.ui.common.ArtworkImage
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
fun ArtistScreen(onOpenArtwork: (Int) -> Unit, onSeeWorks: () -> Unit) {
    val vm = appViewModel { ArtistViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

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
        state.featured?.let { artwork ->
            Spacer(Modifier.height(32.dp))
            Column(Modifier.clickable { onOpenArtwork(artwork.id) }) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp)
                        .background(Mat)
                        .padding(14.dp),
                ) {
                    ArtworkImage(
                        artwork,
                        vm.imageUrl(artwork.imageUrl),
                        Modifier.fillMaxWidth(),
                        elevated = false,
                    )
                }
                Text(
                    artwork.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "대표 작품",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        Spacer(Modifier.height(32.dp))
    }
}