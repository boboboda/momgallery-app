package com.buyoungsil.momgallery.data

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

// 로그인 쿠키를 앱을 껐다 켜도 유지해요.
class PersistentCookieJar(
    private val store: SecureStore,
    private val baseUrl: HttpUrl,
) : CookieJar {

    private val cookies: MutableList<Cookie> =
        store.loadCookies().mapNotNull { Cookie.parse(baseUrl, it) }.toMutableList()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        for (incoming in cookies) {
            this.cookies.removeAll {
                it.name == incoming.name && it.domain == incoming.domain && it.path == incoming.path
            }
            // 만료된 쿠키(삭제 요청)는 넣지 않아요
            if (incoming.expiresAt > now) this.cookies.add(incoming)
        }
        persist()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val before = cookies.size
        cookies.removeAll { it.expiresAt <= now }
        if (cookies.size != before) persist()
        return cookies.filter { it.matches(url) }
    }

    @Synchronized
    fun clear() {
        cookies.clear()
        persist()
    }

    private fun persist() {
        store.saveCookies(cookies.map { it.toString() })
    }
}