package com.buyoungsil.momgallery.ui.admin

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.Inquiry
import com.buyoungsil.momgallery.data.Outcome
import com.buyoungsil.momgallery.data.inquiryKinds
import com.buyoungsil.momgallery.ui.common.CenterLoading
import com.buyoungsil.momgallery.ui.common.ErrorBox
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.errorOrNull
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InquiriesState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<Inquiry> = emptyList(),
)

class InquiriesViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(InquiriesState())
    val state: StateFlow<InquiriesState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            if (_state.value.items.isEmpty()) _state.update { it.copy(loading = true) }
            val r = repo.adminInquiries()
            _state.update {
                InquiriesState(loading = false, error = r.errorOrNull(), items = r.valueOrNull() ?: it.items)
            }
        }
    }

    // "확인했어요" 표시를 바꿔요 (화면은 먼저 바꾸고, 서버 저장이 실패하면 되돌려요)
    fun toggle(item: Inquiry) {
        val newValue = !item.handled
        setHandledLocally(item.id, newValue)
        viewModelScope.launch {
            if (repo.setHandled(item.id, newValue) is Outcome.Fail) {
                setHandledLocally(item.id, item.handled)
                _state.update { it.copy(error = "저장하지 못했어요. 인터넷을 확인해 주세요.") }
            }
        }
    }

    private fun setHandledLocally(id: Int, handled: Boolean) {
        _state.update { s ->
            s.copy(items = s.items.map { if (it.id == id) it.copy(handled = handled) else it })
        }
    }
}

@Composable
fun InquiriesScreen(onBack: () -> Unit) {
    val vm = appViewModel { InquiriesViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("받은 문의", onBack)

        when {
            state.loading && state.items.isEmpty() -> CenterLoading()
            state.error != null && state.items.isEmpty() -> ErrorBox(state.error!!, vm::load)
            state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("받은 문의가 아직 없어요.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    InquiryCard(
                        item = item,
                        onToggle = { vm.toggle(item) },
                        onContact = { openContact(context, item.contact) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InquiryCard(item: Inquiry, onToggle: () -> Unit, onContact: () -> Unit) {
    val kindLabel = inquiryKinds.firstOrNull { it.first == item.kind }?.second ?: "문의"
    val canContact = contactIntent(item.contact) != null

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.handled) 0.6f else 1f),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                (if (item.handled) "" else "새 문의 · ") + kindLabel + " · " + item.createdAt.take(10),
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.handled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(item.contact, style = MaterialTheme.typography.bodyLarge)
            if (item.artworkTitle.isNotBlank()) {
                Text("작품: ${item.artworkTitle}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(item.message, style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                if (canContact) {
                    OutlinedButton(
                        onClick = onContact,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                    ) {
                        Text(if (item.contact.contains("@")) "메일 쓰기" else "전화하기")
                    }
                }
                OutlinedButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                ) {
                    Text(if (item.handled) "다시 새 문의로" else "확인했어요")
                }
            }
        }
    }
}

private fun contactIntent(contact: String): Intent? {
    val digits = contact.filter { it.isDigit() }
    return when {
        contact.contains("@") -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.trim()}"))
        digits.length in 9..15 -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
        else -> null
    }
}

private fun openContact(context: Context, contact: String) {
    val intent = contactIntent(contact) ?: return
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // 전화나 메일 앱이 없는 폰이면 아무 일도 하지 않아요
    }
}
