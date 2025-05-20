package bav.petus.cache

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Update
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import bav.petus.model.HistoryEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

@Database(entities = [PetEntity::class, WeatherRecord::class, PetHistoryRecord::class], version = 1)
@ConstructedBy(PetsDatabaseConstructor::class)
abstract class PetsDatabase : RoomDatabase() {
    abstract fun getDao(): PetDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object PetsDatabaseConstructor : RoomDatabaseConstructor<PetsDatabase> {
    override fun initialize(): PetsDatabase
}

@Dao
interface PetDao {
    @Insert
    suspend fun insertPet(item: PetEntity)

    @Update
    suspend fun updatePet(item: PetEntity)

    @Query("SELECT MAX(id) FROM PetEntity")
    suspend fun getLatestPetId(): Long?

    @Query("SELECT * FROM PetEntity WHERE id = :id")
    fun selectPetByIdFlow(id: Long): Flow<PetEntity?>

    @Query("SELECT * FROM PetEntity")
    suspend fun selectAllPets(): List<PetEntity>

    @Query("SELECT * FROM PetEntity")
    fun selectAllPetsFlow(): Flow<List<PetEntity>>

    @Query("SELECT * FROM WeatherRecord WHERE timestampSecondsSinceEpoch = :timestamp")
    suspend fun selectWeatherRecordByTimestamp(timestamp: Long): WeatherRecord?

    @Insert
    suspend fun insertWeatherRecord(item: WeatherRecord)

    @Query("SELECT * FROM WeatherRecord")
    suspend fun selectAllWeatherRecords(): List<WeatherRecord>

    @Query("SELECT * FROM WeatherRecord")
    fun selectAllWeatherRecordsFlow(): Flow<List<WeatherRecord>>

    @Insert
    suspend fun insertPetHistoryRecord(item: PetHistoryRecord)

    @Query("SELECT * FROM PetHistoryRecord WHERE petId = :id")
    suspend fun selectHistoryRecordsForPet(id: Long): List<PetHistoryRecord>
}

@Entity
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petData: String,
)

@Entity
data class WeatherRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Since midnight January 1, 1970 UTC
    val timestampSecondsSinceEpoch: Long,
    val cloudPercentage: Int?,
    val humidity: Int?,
    val temperature: Int?,
    val windSpeed: Double?,
    // Additional random information
    val info: String?,
)

@Entity
data class PetHistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Since midnight January 1, 1970 UTC
    val timestampSecondsSinceEpoch: Long,
    val petId: Long,
    val event: HistoryEvent,
    // Additional random information
    val info: String?,
)

fun getPetsDatabase(
    builder: RoomDatabase.Builder<PetsDatabase>
): PetsDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}