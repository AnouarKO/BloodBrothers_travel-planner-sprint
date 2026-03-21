package com.example.bbtraveling.domain

sealed interface OperationResult {
    data object Success : OperationResult
    data class Failure(
        val fieldErrors: Map<String, String> = emptyMap(),
        val message: String? = null
    ) : OperationResult
}
