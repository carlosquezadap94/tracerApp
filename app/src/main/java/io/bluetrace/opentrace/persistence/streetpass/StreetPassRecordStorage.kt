package io.bluetrace.opentrace.persistence.streetpass

import android.content.Context
import io.bluetrace.opentrace.infraestructura.db.TraceDatabase
import io.bluetrace.opentrace.infraestructura.db.entidades.StreetPassRecordEntity

class StreetPassRecordStorage(val context: Context) {

    private val recordDao = TraceDatabase.getDatabase(context).recordDao()

    suspend fun saveRecord(recordEntity: StreetPassRecordEntity) {
        recordDao.insert(recordEntity)
    }

    fun nukeDb() {
        recordDao.nukeDb()
    }

    fun getAllRecords(): List<StreetPassRecordEntity> {
        return recordDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}
