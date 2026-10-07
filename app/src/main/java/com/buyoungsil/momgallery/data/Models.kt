package com.buyoungsil.momgallery.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

@Serializable
data class CategoryRef(val id: Int, val name: String)

@Serializable
data class Artwork(
    val id: Int,
    val title: String,
    val description: String = "",
    val imageUrl: String,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val year: Int? = null,
    val medium: String = "",
    val size: String = "",
    val featured: Boolean = false,
    val category: CategoryRef? = null,
    val createdAt: String = "",
)

@Serializable
data class Category(val id: Int, val name: String, val artworkCount: Int)

// 관리자가 보는 문의
@Serializable
data class Inquiry(
    val id: Int,
    val kind: String,
    val name: String,
    val contact: String,
    val message: String,
    val artworkId: Int? = null,
    val artworkTitle: String = "",
    val handled: Boolean = false,
    val createdAt: String = "",
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class InquiryRequest(
    val kind: String, // PURCHASE, COMMISSION, OTHER
    val name: String,
    val contact: String,
    val message: String,
    val artworkId: Int? = null,
)

@Serializable
data class HandledRequest(val handled: Boolean)

@Serializable
data class OkResponse(
    val ok: Boolean = true,
    val id: Int? = null,
    val handled: Boolean? = null,
)

@Serializable
data class ErrorBody(val error: String = "")

// 작품 등록·수정 입력값
data class ArtworkForm(
    val title: String,
    val description: String = "",
    val year: String = "",
    val medium: String = "유화",
    val size: String = "",
    val categoryName: String = "",
    val featured: Boolean = false,
)

// 문의 종류 (서버 값, 화면에 보일 이름)
val inquiryKinds = listOf(
    "PURCHASE" to "작품 구매",
    "COMMISSION" to "그림 주문",
    "OTHER" to "기타 문의",
)