package io.bluetrace.opentrace.persistence.streetpass

import androidx.lifecycle.LiveData
import io.bluetrace.opentrace.infraestructura.db.dao.StreetPassRecordDao
import io.bluetrace.opentrace.infraestructura.db.entidades.StreetPassRecordEntity

class StreetPassRecordRepository(private val recordDao: StreetPassRecordDao) {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRecords: LiveData<List<StreetPassRecordEntity>> = recordDao.getRecords()

    suspend fun insert(word: StreetPassRecordEntity) {
        recordDao.insert(word)
    }
}
