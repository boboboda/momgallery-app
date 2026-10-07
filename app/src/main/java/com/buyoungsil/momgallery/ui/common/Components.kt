package com.buyoungsil.momgallery.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.ui.theme.Dimens

// "유화, 2020" 같은 한 줄 설명
fun Artwork.subtitle(): String =
    listOfNotNull(medium.takeIf { it.isNotBlank() }, year?.toString()).joinToString(", ")

@Composable
fun CenterLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 5.dp)
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        BigButton("다시 불러오기", onRetry)
    }
}

// 크고 누르기 쉬운 버튼
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
) {
    val shape = RoundedCornerShape(32.dp)
    val size = modifier
        .fillMaxWidth()
        .heightIn(min = Dimens.MinTouch)
    if (outlined) {
        OutlinedButton(onClick = onClick, modifier = size, enabled = enabled, shape = shape) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Button(onClick = onClick, modifier = size, enabled = enabled, shape = shape) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ScreenTopBar(title: String, onBack: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = Dimens.MinTouch)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("뒤로", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

// 그림 비율 그대로 보여줘요 (잘리지 않게)
@Composable
fun ArtworkImage(
    artwork: Artwork,
    url: String,
    modifier: Modifier = Modifier,
    elevated: Boolean = true,
) {
    val w = artwork.imageWidth
    val h = artwork.imageHeight
    val ratio = if (w != null && h != null && w > 0 && h > 0) w.toFloat() / h else 4f / 3f

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = artwork.title,
        contentScale = ContentScale.Crop, // 비율이 같아서 실제로는 잘리지 않아요
        modifier = modifier
            .then(if (elevated) Modifier.shadow(6.dp) else Modifier)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .aspectRatio(ratio),
    )
}

// 그림을 크게 보기: 두 손가락으로 확대, 아래 "닫기" 버튼
@Composable
fun ZoomDialog(url: String, title: String, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            AsyncImage(
                model = url,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
            )
            Button(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .heightIn(min = Dimens.MinTouch),
            ) {
                Text("닫기", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}