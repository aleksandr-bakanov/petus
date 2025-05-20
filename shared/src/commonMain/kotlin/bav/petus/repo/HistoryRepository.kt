package bav.petus.repo

import bav.petus.cache.PetHistoryRecord
import bav.petus.cache.PetsDatabase
import bav.petus.core.time.getTimestampSecondsSinceEpoch
import bav.petus.model.HistoryEvent

class HistoryRepository(
    private val database: PetsDatabase,
) {

    suspend fun getLatestPetId(): Long? {
        return database.getDao().getLatestPetId()
    }

    suspend fun recordHistoryEvent(
        petId: Long,
        event: HistoryEvent,
        timestamp: Long = getTimestampSecondsSinceEpoch(),
        info: String? = null,
    ) {
        val record = PetHistoryRecord(
            timestampSecondsSinceEpoch = timestamp,
            petId = petId,
            event = event,
            info = info,
        )
        database.getDao().insertPetHistoryRecord(record)
    }
}