package com.buyoungsil.momgallery.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

// 화면은 성공(Ok) 아니면 실패(Fail + 쉬운 한국어 메시지)만 받아요.
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Fail(val message: String) : Outcome<Nothing>
}

class GalleryRepository(
    private val api: GalleryApi,
    private val auth: AuthState,
    private val baseUrl: String,
) {
    val isAdmin: StateFlow<Boolean> = auth.isAdmin

    // 서버가 주는 이미지 주소("/uploads/...")를 전체 주소로
    fun imageUrl(path: String): String =
        if (path.startsWith("http")) path else baseUrl.trimEnd('/') + path

    // 방문자용
    suspend fun artworks(categoryId: Int? = null) = safeCall { api.artworks(category = categoryId) }
    suspend fun featured() = safeCall { api.artworks(featured = true) }
    suspend fun artwork(id: Int) = safeCall { api.artwork(id) }
    suspend fun categories() = safeCall { api.categories() }

    suspend fun sendInquiry(request: InquiryRequest) = safeCall { api.sendInquiry(request) }

    // 로그인·로그아웃
    suspend fun login(username: String, password: String): Outcome<Unit> {
        val user = username.trim()
        return when (val r = safeCall { api.login(LoginRequest(user, password)) }) {
            is Outcome.Ok -> {
                auth.signedIn(user, password)
                Outcome.Ok(Unit)
            }
            is Outcome.Fail -> r
        }
    }

    suspend fun logout() {
        safeCall { api.logout() } // 실패해도 이 폰에서는 로그아웃해요
        auth.signedOut()
    }

    // 관리자용
    suspend fun adminInquiries(onlyOpen: Boolean = false) =
        safeCall { api.adminInquiries(if (onlyOpen) true else null) }

    suspend fun setHandled(id: Int, handled: Boolean) =
        safeCall { api.setHandled(id, HandledRequest(handled)) }

    suspend fun createArtwork(form: ArtworkForm, image: ByteArray) =
        safeCall { api.createArtwork(form.toParts(), image.toPart()) }

    suspend fun updateArtwork(id: Int, form: ArtworkForm, image: ByteArray?) =
        safeCall { api.updateArtwork(id, form.toParts(), image?.toPart()) }

    suspend fun deleteArtwork(id: Int) = safeCall { api.deleteArtwork(id) }

    private fun ArtworkForm.toParts(): Map<String, RequestBody> {
        val text = "text/plain; charset=utf-8".toMediaType()
        fun s(value: String) = value.toRequestBody(text)
        return mapOf(
            "title" to s(title),
            "description" to s(description),
            "year" to s(year),
            "medium" to s(medium),
            "size" to s(size),
            "categoryName" to s(categoryName),
            "featured" to s(featured.toString()),
        )
    }

    private fun ByteArray.toPart(): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            "image",
            "photo.jpg",
            toRequestBody("image/jpeg".toMediaType()),
        )

    private suspend fun <T> safeCall(block: suspend () -> T): Outcome<T> =
        try {
            Outcome.Ok(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Outcome.Fail(messageFor(e))
        } catch (e: IOException) {
            Outcome.Fail("인터넷 연결을 확인해 주세요.")
        } catch (e: SerializationException) {
            Outcome.Fail("서버 응답을 읽지 못했어요. 잠시 뒤에 다시 해 주세요.")
        }

    private fun messageFor(e: HttpException): String {
        val fromServer = try {
            e.response()?.errorBody()?.string()
                ?.let { AppJson.decodeFromString<ErrorBody>(it).error }
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        return fromServer ?: when (e.code()) {
            401 -> "로그인이 필요해요."
            404 -> "찾을 수 없어요."
            413 -> "사진이 너무 커요."
            in 500..599 -> "서버에 문제가 있어요. 잠시 뒤에 다시 해 주세요."
            else -> "잠시 뒤에 다시 해 주세요."
        }
    }
}