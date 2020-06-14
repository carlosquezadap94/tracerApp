package io.bluetrace.opentrace.persistence.streetpass

import android.content.Context
import io.bluetrace.opentrace.persistence.TraceDatabase

class StreetPassRecordStorage(val context: Context) {

    private val recordDao = TraceDatabase.getDatabase(context).recordDao()

    suspend fun saveRecord(record: StreetPassRecord) {
        recordDao.insert(record)
    }

    fun nukeDb() {
        recordDao.nukeDb()
    }

    fun getAllRecords(): List<StreetPassRecord> {
        return recordDao.getCurrentRecords()
    }

    suspend fun purgeOldRecords(before: Long) {
        recordDao.purgeOldRecords(before)
    }
}
