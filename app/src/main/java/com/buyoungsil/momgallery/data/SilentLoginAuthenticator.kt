package com.buyoungsil.momgallery.data

import kotlinx.serialization.encodeToString
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

// 서버가 401(로그인 필요)을 주면, 저장해 둔 아이디·비밀번호로 조용히 다시 로그인하고
// 하려던 요청을 이어서 보내요. 엄마는 로그인 화면을 다시 볼 일이 없어요.
class SilentLoginAuthenticator(
    private val store: SecureStore,
    private val auth: AuthState,
    private val loginClient: OkHttpClient, // Authenticator가 없는 클라이언트 (무한 반복 방지)
    private val baseUrl: String,
) : Authenticator {

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 로그인 요청 자체가 401이면 그냥 실패 (아이디·비밀번호가 틀린 것)
        if (response.request.url.encodedPath.endsWith("/admin/login")) return null
        if (responseCount(response) >= 2) return null

        val user = store.username
        val pass = store.password
        if (user.isNullOrEmpty() || pass.isNullOrEmpty()) return null

        synchronized(this) {
            val body = AppJson.encodeToString(LoginRequest(user, pass)).toRequestBody(jsonType)
            val request = Request.Builder()
                .url(baseUrl + "api/v1/admin/login")
                .post(body)
                .build()

            try {
                loginClient.newCall(request).execute().use { result ->
                    if (result.isSuccessful) {
                        // 새 쿠키는 CookieJar가 저장했어요. 오래된 Cookie 헤더만 빼고 다시 보내요.
                        return response.request.newBuilder().removeHeader("Cookie").build()
                    }
                    // 비밀번호가 서버에서 바뀐 경우: 저장된 정보를 지워요
                    if (result.code == 401) auth.signedOut()
                }
            } catch (_: IOException) {
                // 인터넷 문제면 저장 정보는 그대로 두고 이번 요청만 실패시켜요
            }
            return null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}