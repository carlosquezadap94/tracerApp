package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.infraestructura.db.entidades.StatusRecordEntity

interface StorageStatusListener {
    suspend fun onStatusRecordStorage(statusRecordEntity: StatusRecordEntity)
}