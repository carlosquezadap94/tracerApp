package io.bluetrace.opentrace.persistence.status

import android.content.Context
import io.bluetrace.opentrace.persistence.TraceDatabase

class StatusRecordStorage(val context: Context) {

    val statusDao = TraceDatabase.getDatabase(context).statusDao()

    suspend fun saveRecord(record: StatusRecord) {
        statusDao.insert(record)
    }

    fun nukeDb() {
        statusDao.nukeDb()
    }

    fun getAllRecords(): List<StatusRecord> {
        return statusDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        statusDao.purgeOldRecords(before)
    }
}
