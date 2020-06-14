package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.persistence.status.StatusRecord
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecord

interface StorageListener {
    fun onStatusRecordStorage(statusRecord: StatusRecord)
    fun onStreetPassRecordStorage(streetPassRecord: StreetPassRecord)
}