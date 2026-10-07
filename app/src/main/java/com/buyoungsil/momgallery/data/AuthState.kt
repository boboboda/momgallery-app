package com.buyoungsil.momgallery.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// "지금 엄마(관리자)로 로그인된 상태인가"를 앱 전체가 같이 봐요.
class AuthState(
    private val store: SecureStore,
    private val jar: PersistentCookieJar,
) {
    private val _isAdmin = MutableStateFlow(store.hasCredentials())
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    fun signedIn(user: String, pass: String) {
        store.saveCredentials(user, pass)
        _isAdmin.value = true
    }

    // 로그아웃하거나, 저장된 비밀번호로도 로그인이 안 될 때(비밀번호가 바뀐 경우)
    fun signedOut() {
        store.clearCredentials()
        jar.clear()
        _isAdmin.value = false
    }
}