package com.example.model

data class HistoryItem(
    val id: String,
    val operationTitle: String,
    val details: String,
    val timestampFormatted: String,
    val sizeBytes: Long? = null,
    val uriString: String? = null
)
