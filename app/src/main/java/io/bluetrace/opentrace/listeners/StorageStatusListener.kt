package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.persistence.status.StatusRecord

interface StorageStatusListener {
    suspend fun onStatusRecordStorage(statusRecord: StatusRecord)
}