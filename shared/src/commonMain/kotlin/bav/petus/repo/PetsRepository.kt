package bav.petus.repo

import bav.petus.cache.PetEntity
import bav.petus.cache.PetsDatabase
import bav.petus.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Responsibilities:
 *   - Get pets from DB
 *   - Add pet to DB
 *   - Update pets in DB
 *   - Convert PetEntity to Pet (may be moved to mapper later)
 */
class PetsRepository(
    private val database: PetsDatabase
) {
    suspend fun insertPet(pet: Pet) {
        database.getDao()
            .insert(
                PetEntity(
                    petData = Json.encodeToString(pet)
                )
            )
    }

    suspend fun updatePet(pet: Pet) {
        database.getDao()
            .update(
                PetEntity(
                    id = pet.id,
                    isDead = pet.isDead,
                    petData = Json.encodeToString(pet),
                )
            )
    }

    suspend fun getAllPets(): List<Pet> {
        return database.getDao()
            .selectAllPets()
            .map { entity ->
                Json.decodeFromString<Pet>(entity.petData).copy(
                    id = entity.id,
                    isDead = entity.isDead,
                )
            }
    }

    fun getAllPetsFlow(): Flow<List<Pet>> {
        return database.getDao()
            .selectAllPetsAsFlow()
            .map { list ->
                list.map { entity ->
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                        isDead = entity.isDead,
                    )
                }
            }
    }
}