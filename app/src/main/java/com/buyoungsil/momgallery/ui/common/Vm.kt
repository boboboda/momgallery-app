package com.buyoungsil.momgallery.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.buyoungsil.momgallery.GalleryApp
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.Outcome

// 화면에서 ViewModel을 만들 때, 앱 전체가 같이 쓰는 저장소를 넘겨 줘요.
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (GalleryRepository) -> VM,
): VM {
    val app = LocalContext.current.applicationContext as GalleryApp
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(app.repository) } },
    )
}

fun <T> Outcome<T>.valueOrNull(): T? = (this as? Outcome.Ok)?.value

fun Outcome<*>.errorOrNull(): String? = (this as? Outcome.Fail)?.message