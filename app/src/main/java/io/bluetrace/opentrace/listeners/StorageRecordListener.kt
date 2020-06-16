package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.infraestructura.db.entidades.StreetPassRecordEntity

interface StorageRecordListener {

    suspend fun onStreetPassRecordStorage(streetPassRecordEntity: StreetPassRecordEntity)

}