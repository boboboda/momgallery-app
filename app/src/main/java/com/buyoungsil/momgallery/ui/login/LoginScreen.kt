package com.buyoungsil.momgallery.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.Outcome
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val username: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun setUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun setPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun toggleShow() = _state.update { it.copy(showPassword = !it.showPassword) }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.loading) return
        if (s.username.isBlank() || s.password.isEmpty()) {
            _state.update { it.copy(error = "아이디와 비밀번호를 모두 적어 주세요.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.login(s.username, s.password)) {
                is Outcome.Ok -> {
                    _state.update { it.copy(loading = false, password = "") }
                    onSuccess()
                }
                is Outcome.Fail -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}

@Composable
fun LoginScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    val vm = appViewModel { LoginViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("작가 로그인", onBack)

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "처음 한 번만 로그인하면, 다음부터는 저절로 로그인돼요.",
                style = MaterialTheme.typography.bodyLarge,
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = vm::setUsername,
                label = { Text("아이디") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = vm::setPassword,
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation =
                    if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = vm::toggleShow) {
                Text(
                    if (state.showPassword) "비밀번호 숨기기" else "비밀번호 보기",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            state.error?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }

            BigButton(
                text = if (state.loading) "로그인 중..." else "로그인",
                onClick = { vm.login(onSuccess) },
                enabled = !state.loading,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
