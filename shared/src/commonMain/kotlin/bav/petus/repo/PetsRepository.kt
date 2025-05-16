package bav.petus.repo

import bav.petus.cache.PetEntity
import bav.petus.cache.PetsDatabase
import bav.petus.model.Pet
import bav.petus.model.Place
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
            .insertPet(
                PetEntity(
                    petData = Json.encodeToString(pet)
                )
            )
    }

    suspend fun updatePet(pet: Pet) {
        database.getDao()
            .updatePet(
                PetEntity(
                    id = pet.id,
                    petData = Json.encodeToString(pet),
                )
            )
    }

    suspend fun getAllPetsInZoo(): List<Pet> {
        return database.getDao()
            .selectAllPets()
            .map { entity ->
                Json.decodeFromString<Pet>(entity.petData).copy(
                    id = entity.id,
                )
            }
            .filter { pet -> pet.place == Place.Zoo }
    }

    fun getAllPetsInZooFlow(): Flow<List<Pet>> {
        return database.getDao()
            .selectAllPetsFlow()
            .map { pets: List<PetEntity> ->
                pets.map { entity ->
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                    )
                }.filter { pet -> pet.place == Place.Zoo }
            }
    }

    fun getAllPetsInCemeteryFlow(): Flow<List<Pet>> {
        return database.getDao()
            .selectAllPetsFlow()
            .map { pets ->
                pets.map { entity ->
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                    )
                }.filter { pet -> pet.place == Place.Cemetery }
            }
    }

    fun getPetByIdFlow(id: Long): Flow<Pet?> {
        return database.getDao()
            .selectPetByIdFlow(id)
            .map { entity ->
                entity?.let {
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                    )
                }
            }
    }
}