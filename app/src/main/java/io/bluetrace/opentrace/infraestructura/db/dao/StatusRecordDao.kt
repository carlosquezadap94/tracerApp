package io.bluetrace.opentrace.infraestructura.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.bluetrace.opentrace.infraestructura.db.entidades.StatusRecordEntity

@Dao
interface StatusRecordDao {

    @Query("SELECT * from status_table ORDER BY timestamp ASC")
    fun getRecords(): LiveData<List<StatusRecordEntity>>

    @Query("SELECT * from status_table ORDER BY timestamp ASC")
    fun getCurrentRecords(): List<StatusRecordEntity>

    @Query("SELECT * from status_table where msg = :msg ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentRecord(msg: String): LiveData<StatusRecordEntity?>


    @Query("DELETE FROM status_table")
    fun nukeDb()

    @Query("DELETE FROM status_table WHERE timestamp < :before")
    suspend fun purgeOldRecords(before: Long)

    @RawQuery
    fun getRecordsViaQuery(query: SupportSQLiteQuery): List<StatusRecordEntity>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recordEntity: StatusRecordEntity)

}
