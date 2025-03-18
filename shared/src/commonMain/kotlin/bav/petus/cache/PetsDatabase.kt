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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

@Database(entities = [PetEntity::class], version = 1)
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
    suspend fun insert(item: PetEntity)

    @Update
    suspend fun update(item: PetEntity)

    @Query("DELETE FROM PetEntity")
    suspend fun removeAllPets()

    @Query("SELECT * FROM PetEntity")
    suspend fun selectAllPets(): List<PetEntity>

    @Query("SELECT * FROM PetEntity")
    fun selectAllPetsAsFlow(): Flow<List<PetEntity>>
}

@Entity
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isDead: Boolean = false,
    val petData: String,
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