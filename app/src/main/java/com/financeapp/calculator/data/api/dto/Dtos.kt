package com.financeapp.calculator.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionRequest(
    @Json(name = "id_token") val idToken: String
)

@JsonClass(generateAdapter = true)
data class SessionResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val email: String,
    val name: String?,
    val picture: String?
)

@JsonClass(generateAdapter = true)
data class StatusResponse(val status: String)

@JsonClass(generateAdapter = true)
data class CalculationCreate(
    val type: String,
    val inputs: Map<String, Any>,
    val results: Map<String, Any>,
    @Json(name = "saved_name") val savedName: String? = null
)

@JsonClass(generateAdapter = true)
data class CalculationDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val type: String,
    val inputs: Map<String, Any>,
    val results: Map<String, Any>,
    @Json(name = "saved_name") val savedName: String?,
    @Json(name = "created_at") val createdAt: String
)
