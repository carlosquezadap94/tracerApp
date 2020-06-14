package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.persistence.status.StatusRecord
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecord

interface StorageListener {
    suspend fun onStatusRecordStorage(statusRecord: StatusRecord)
    suspend fun onStreetPassRecordStorage(streetPassRecord: StreetPassRecord)
}