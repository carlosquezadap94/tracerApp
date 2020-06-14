package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecord

interface StorageRecordListener {

    suspend fun onStreetPassRecordStorage(streetPassRecord: StreetPassRecord)

}