package com.financeapp.calculator.data.api

import com.financeapp.calculator.data.api.dto.CalculationCreate
import com.financeapp.calculator.data.api.dto.CalculationDto
import com.financeapp.calculator.data.api.dto.SessionRequest
import com.financeapp.calculator.data.api.dto.SessionResponse
import com.financeapp.calculator.data.api.dto.StatusResponse
import com.financeapp.calculator.data.api.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FinanceApi {

    @GET("api/status")
    suspend fun status(): StatusResponse

    @POST("api/auth/session")
    suspend fun createSession(@Body body: SessionRequest): SessionResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/calculations")
    suspend fun saveCalculation(@Body body: CalculationCreate): CalculationDto

    @GET("api/calculations")
    suspend fun listCalculations(): List<CalculationDto>

    @DELETE("api/calculations/{id}")
    suspend fun deleteCalculation(@Path("id") id: String): Response<Unit>
}
