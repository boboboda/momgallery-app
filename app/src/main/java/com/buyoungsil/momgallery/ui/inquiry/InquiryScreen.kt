package com.buyoungsil.momgallery.ui.inquiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.InquiryRequest
import com.buyoungsil.momgallery.data.Outcome
import com.buyoungsil.momgallery.data.inquiryKinds
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InquiryState(
    val kind: String = "PURCHASE",
    val name: String = "",
    val contact: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

class InquiryViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(InquiryState())
    val state: StateFlow<InquiryState> = _state.asStateFlow()

    fun setKind(v: String) = _state.update { it.copy(kind = v) }
    fun setName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun setContact(v: String) = _state.update { it.copy(contact = v, error = null) }
    fun setMessage(v: String) = _state.update { it.copy(message = v, error = null) }

    fun send(artworkId: Int?) {
        val s = _state.value
        val problem = when {
            s.name.isBlank() -> "이름을 적어 주세요."
            s.contact.isBlank() -> "연락받을 전화번호나 이메일을 적어 주세요."
            s.message.trim().length < 5 -> "문의 내용을 5자 이상 적어 주세요."
            else -> null
        }
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val result = repo.sendInquiry(
                InquiryRequest(
                    kind = s.kind,
                    name = s.name.trim(),
                    contact = s.contact.trim(),
                    message = s.message.trim(),
                    artworkId = artworkId,
                ),
            )
            _state.update {
                when (result) {
                    is Outcome.Ok -> it.copy(sending = false, done = true)
                    is Outcome.Fail -> it.copy(sending = false, error = result.message)
                }
            }
        }
    }
}

@Composable
fun InquiryScreen(
    artworkId: Int?,
    artworkTitle: String,
    onDone: () -> Unit,
) {
    val vm = appViewModel { InquiryViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("문의")

        if (state.done) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("문의가 전달됐어요", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "남겨 주신 연락처로 곧 연락드릴게요.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
                )
                BigButton("확인", onDone)
            }
            return@Column
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (artworkId != null && artworkTitle.isNotBlank()) {
                Text(
                    "문의할 작품: $artworkTitle",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text("문의 종류", style = MaterialTheme.typography.titleMedium)
            inquiryKinds.forEach { (value, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = state.kind == value,
                            onClick = { vm.setKind(value) },
                            role = Role.RadioButton,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = state.kind == value, onClick = null)
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.contact,
                onValueChange = vm::setContact,
                label = { Text("전화번호 또는 이메일") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.message,
                onValueChange = vm::setMessage,
                label = { Text("문의 내용") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }

            BigButton(
                text = if (state.sending) "보내는 중..." else "문의 보내기",
                onClick = { vm.send(artworkId) },
                enabled = !state.sending,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}