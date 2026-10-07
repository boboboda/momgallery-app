package com.buyoungsil.momgallery

import android.app.Application
import com.buyoungsil.momgallery.data.AppJson
import com.buyoungsil.momgallery.data.AuthState
import com.buyoungsil.momgallery.data.GalleryApi
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.data.PersistentCookieJar
import com.buyoungsil.momgallery.data.SecureStore
import com.buyoungsil.momgallery.data.SilentLoginAuthenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class GalleryApp : Application() {

    lateinit var repository: GalleryRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val baseUrl = BuildConfig.BASE_URL
        val store = SecureStore(this)
        val jar = PersistentCookieJar(store, baseUrl.toHttpUrl())
        val auth = AuthState(store, jar)

        val plain = OkHttpClient.Builder()
            .cookieJar(jar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS) // 사진 업로드
            .build()

        val client = plain.newBuilder()
            .authenticator(SilentLoginAuthenticator(store, auth, plain, baseUrl))
            .apply {
                if (BuildConfig.DEBUG) {
                    // BASIC: 주소와 결과 코드만 기록해요 (비밀번호가 로그에 남지 않게)
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
                    )
                }
            }
            .build()

        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(AppJson.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build()
            .create(GalleryApi::class.java)

        repository = GalleryRepository(api, auth, baseUrl)
    }
}