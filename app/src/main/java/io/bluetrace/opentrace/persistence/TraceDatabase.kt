package io.bluetrace.opentrace.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.bluetrace.opentrace.persistence.status.StatusRecord
import io.bluetrace.opentrace.persistence.status.StatusRecordDao
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecord
import io.bluetrace.opentrace.persistence.streetpass.StreetPassRecordDao


@Database(
    entities = arrayOf(StreetPassRecord::class, StatusRecord::class),
    version = 1,
    exportSchema = true
)
abstract class TraceDatabase : RoomDatabase() {

    abstract fun recordDao(): StreetPassRecordDao
    abstract fun statusDao(): StatusRecordDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TraceDatabase? = null

        fun getDatabase(context: Context): TraceDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    TraceDatabase::class.java,
                    "record_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
