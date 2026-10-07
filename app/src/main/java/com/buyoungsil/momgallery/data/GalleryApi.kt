package com.buyoungsil.momgallery.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface GalleryApi {

    // 방문자용 (로그인 필요 없음)
    @GET("api/v1/artworks")
    suspend fun artworks(
        @Query("category") category: Int? = null,
        @Query("featured") featured: Boolean? = null,
    ): List<Artwork>

    @GET("api/v1/artworks/{id}")
    suspend fun artwork(@Path("id") id: Int): Artwork

    @GET("api/v1/categories")
    suspend fun categories(): List<Category>

    @POST("api/v1/inquiries")
    suspend fun sendInquiry(@Body body: InquiryRequest): OkResponse

    // 관리자용
    @POST("api/v1/admin/login")
    suspend fun login(@Body body: LoginRequest): OkResponse

    @POST("api/v1/admin/logout")
    suspend fun logout(): OkResponse

    @GET("api/v1/admin/inquiries")
    suspend fun adminInquiries(@Query("open") open: Boolean? = null): List<Inquiry>

    @PATCH("api/v1/admin/inquiries/{id}")
    suspend fun setHandled(@Path("id") id: Int, @Body body: HandledRequest): OkResponse

    @Multipart
    @POST("api/v1/admin/artworks")
    suspend fun createArtwork(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part,
    ): OkResponse

    @Multipart
    @PATCH("api/v1/admin/artworks/{id}")
    suspend fun updateArtwork(
        @Path("id") id: Int,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): OkResponse

    @DELETE("api/v1/admin/artworks/{id}")
    suspend fun deleteArtwork(@Path("id") id: Int): OkResponse
}