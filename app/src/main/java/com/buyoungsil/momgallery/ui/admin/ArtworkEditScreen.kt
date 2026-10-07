package com.buyoungsil.momgallery.ui.admin

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.buyoungsil.momgallery.data.ArtworkForm
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.Outcome
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.CenterLoading
import com.buyoungsil.momgallery.ui.common.ConfirmDialog
import com.buyoungsil.momgallery.ui.common.ErrorBox
import com.buyoungsil.momgallery.ui.common.MessageDialog
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.valueOrNull
import com.buyoungsil.momgallery.util.ImageCompress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class EditState(
    val loading: Boolean = false,
    val loadError: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val medium: String = "유화",
    val year: String = Calendar.getInstance().get(Calendar.YEAR).toString(),
    val size: String = "",
    val featured: Boolean = false,
    val categories: List<String> = emptyList(),
    val existingImageUrl: String? = null,
    val photoUri: Uri? = null,
    val photo: ByteArray? = null,
    val preparing: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val doneMessage: String? = null,
)

// id가 null이면 새 그림, 있으면 고치기
class ArtworkEditViewModel(
    private val repo: GalleryRepository,
    private val id: Int?,
) : ViewModel() {

    private val _state = MutableStateFlow(EditState(loading = id != null))
    val state: StateFlow<EditState> = _state.asStateFlow()

    init {
        load()
    }

    fun edit(change: (EditState) -> EditState) = _state.update(change)

    fun load() {
        viewModelScope.launch {
            val names = repo.categories().valueOrNull().orEmpty().map { it.name }

            if (id == null) {
                _state.update {
                    it.copy(
                        categories = names,
                        category = it.category.ifBlank { names.firstOrNull() ?: "풍경화" },
                    )
                }
                return@launch
            }

            _state.update { it.copy(loading = true, loadError = null) }
            when (val r = repo.artwork(id)) {
                is Outcome.Ok -> {
                    val a = r.value
                    _state.update {
                        it.copy(
                            loading = false,
                            categories = names,
                            title = a.title,
                            description = a.description,
                            category = a.category?.name.orEmpty(),
                            medium = a.medium,
                            year = a.year?.toString().orEmpty(),
                            size = a.size,
                            featured = a.featured,
                            existingImageUrl = repo.imageUrl(a.imageUrl),
                        )
                    }
                }
                is Outcome.Fail -> _state.update { it.copy(loading = false, loadError = r.message) }
            }
        }
    }

    // 고른 사진을 올리기 좋게 줄여요
    fun onPhoto(context: Context, uri: Uri) {
        _state.update { it.copy(preparing = true, error = null, photoUri = uri) }
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.Default) { ImageCompress.prepare(context, uri) }
            _state.update {
                if (bytes == null) {
                    it.copy(preparing = false, photoUri = null, photo = null, error = "이 사진은 읽지 못했어요. 다른 사진을 골라 주세요.")
                } else {
                    it.copy(preparing = false, photo = bytes)
                }
            }
        }
    }

    fun save() {
        val s = _state.value
        if (s.saving || s.preparing) return

        val problem = when {
            id == null && s.photo == null -> "사진을 먼저 골라 주세요."
            s.title.isBlank() -> "그림 이름을 적어 주세요."
            else -> null
        }
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val form = ArtworkForm(
                title = s.title.trim(),
                description = s.description.trim(),
                year = s.year.trim(),
                medium = s.medium.trim(),
                size = s.size.trim(),
                categoryName = s.category.trim(),
                featured = s.featured,
            )
            val r = if (id == null) repo.createArtwork(form, s.photo!!) else repo.updateArtwork(id, form, s.photo)
            _state.update {
                when (r) {
                    is Outcome.Ok -> it.copy(
                        saving = false,
                        doneMessage = if (id == null) "그림을 올렸어요!" else "저장했어요!",
                    )
                    is Outcome.Fail -> it.copy(saving = false, error = r.message)
                }
            }
        }
    }

    fun delete() {
        if (id == null) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val r = repo.deleteArtwork(id)
            _state.update {
                when (r) {
                    is Outcome.Ok -> it.copy(saving = false, doneMessage = "그림을 지웠어요.")
                    is Outcome.Fail -> it.copy(saving = false, error = r.message)
                }
            }
        }
    }
}

@Composable
fun ArtworkEditScreen(id: Int?, onBack: () -> Unit, onDone: () -> Unit) {
    val vm = appViewModel(key = "artwork-edit-${id ?: 0}") { ArtworkEditViewModel(it, id) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var moreOpen by rememberSaveable { mutableStateOf(id != null) }
    var askDelete by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onPhoto(context.applicationContext, uri)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(if (id == null) "그림 올리기" else "그림 고치기", onBack)

        when {
            state.loading -> CenterLoading()
            state.loadError != null -> ErrorBox(state.loadError!!, vm::load)
            else -> Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. 사진
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    val model: Any? = state.photoUri ?: state.existingImageUrl
                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = "고른 그림",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text("아직 사진이 없어요", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                BigButton(
                    text = when {
                        state.preparing -> "사진 줄이는 중..."
                        state.photoUri != null || state.existingImageUrl != null -> "사진 바꾸기"
                        else -> "사진 고르기"
                    },
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !state.preparing && !state.saving,
                    outlined = true,
                )

                // 2. 그림 이름
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { v -> vm.edit { it.copy(title = v, error = null) } },
                    label = { Text("그림 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 첫 화면에 걸기
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable { vm.edit { it.copy(featured = !it.featured) } },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "첫 화면에 크게 걸기",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = state.featured, onCheckedChange = { v -> vm.edit { it.copy(featured = v) } })
                }

                // 3. 더 적기 (처음에는 접혀 있어요)
                TextButton(
                    onClick = { moreOpen = !moreOpen },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                ) {
                    Text(
                        if (moreOpen) "자세히 적기 접기" else "자세히 적기 (선택)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                if (moreOpen) {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { v -> vm.edit { it.copy(description = v) } },
                        label = { Text("그림 설명") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text("분류", style = MaterialTheme.typography.titleMedium)
                    if (state.categories.isNotEmpty()) {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            state.categories.forEach { name ->
                                FilterChip(
                                    selected = state.category == name,
                                    onClick = { vm.edit { it.copy(category = name) } },
                                    label = { Text(name, style = MaterialTheme.typography.bodyLarge) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.category,
                        onValueChange = { v -> vm.edit { it.copy(category = v) } },
                        label = { Text("분류 (새로 만들려면 직접 적어요)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.medium,
                        onValueChange = { v -> vm.edit { it.copy(medium = v) } },
                        label = { Text("재료 (예: 유화)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.year,
                        onValueChange = { v -> vm.edit { it.copy(year = v.filter(Char::isDigit).take(4)) } },
                        label = { Text("그린 해") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.size,
                        onValueChange = { v -> vm.edit { it.copy(size = v) } },
                        label = { Text("크기 (예: 53x45cm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                state.error?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }

                BigButton(
                    text = when {
                        state.saving -> "잠시만요..."
                        id == null -> "올리기"
                        else -> "저장하기"
                    },
                    onClick = vm::save,
                    enabled = !state.saving && !state.preparing,
                )

                if (id != null) {
                    OutlinedButton(
                        onClick = { askDelete = true },
                        enabled = !state.saving,
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                    ) {
                        Text(
                            "이 그림 지우기",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (askDelete) {
        ConfirmDialog(
            title = "정말 지울까요?",
            message = "지우면 되돌릴 수 없어요.",
            confirmText = "지우기",
            danger = true,
            onConfirm = {
                askDelete = false
                vm.delete()
            },
            onDismiss = { askDelete = false },
        )
    }

    state.doneMessage?.let { MessageDialog("완료", it, onClose = onDone) }
}
