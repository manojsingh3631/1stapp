package com.financeapp.calculator.data.repository

import com.financeapp.calculator.data.api.FinanceApi
import com.financeapp.calculator.data.api.dto.CalculationCreate
import com.financeapp.calculator.data.api.dto.CalculationDto
import com.financeapp.calculator.data.local.dao.CalculationDao
import com.financeapp.calculator.data.local.entity.CalculationEntity
import com.financeapp.calculator.utils.Outcome
import com.financeapp.calculator.utils.safeCall
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculationRepository @Inject constructor(
    private val api: FinanceApi,
    private val dao: CalculationDao,
    private val moshi: Moshi
) {

    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any>>(mapType)

    fun observeForUser(userId: String): Flow<List<CalculationEntity>> = dao.observeForUser(userId)

    suspend fun save(create: CalculationCreate): Outcome<CalculationDto> = safeCall {
        val saved = api.saveCalculation(create)
        dao.upsert(saved.toEntity())
        saved
    }

    suspend fun refreshFromServer(): Outcome<Unit> = safeCall {
        val remote = api.listCalculations()
        dao.upsertAll(remote.map { it.toEntity() })
    }

    suspend fun delete(id: String): Outcome<Unit> = safeCall {
        api.deleteCalculation(id)
        dao.deleteById(id)
    }

    suspend fun clearLocal(userId: String) = dao.clearForUser(userId)

    private fun CalculationDto.toEntity() = CalculationEntity(
        id = id,
        userId = userId,
        type = type,
        inputsJson = mapAdapter.toJson(inputs),
        resultsJson = mapAdapter.toJson(results),
        savedName = savedName,
        createdAt = createdAt,
        pendingSync = false
    )
}
