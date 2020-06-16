package io.bluetrace.opentrace.persistence.status

import android.content.Context
import io.bluetrace.opentrace.infraestructura.db.TraceDatabase
import io.bluetrace.opentrace.infraestructura.db.entidades.StatusRecordEntity

class StatusRecordStorage(val context: Context) {

     val statusDao = TraceDatabase.getDatabase(context).statusDao()

    suspend fun saveRecord(recordEntity: StatusRecordEntity) {
        statusDao.insert(recordEntity)
    }

    fun nukeDb() {
        statusDao.nukeDb()
    }

    fun getAllRecords(): List<StatusRecordEntity> {
        return statusDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        statusDao.purgeOldRecords(before)
    }

}
