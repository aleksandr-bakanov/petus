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
                    isDead = pet.isDead,
                    petData = Json.encodeToString(pet),
                )
            )
    }

    suspend fun getAllAlivePets(): List<Pet> {
        return database.getDao()
            .selectAllAlivePets()
            .map { entity ->
                Json.decodeFromString<Pet>(entity.petData).copy(
                    id = entity.id,
                    isDead = entity.isDead,
                )
            }
    }

    fun getAllAlivePetsFlow(): Flow<List<Pet>> {
        return database.getDao()
            .selectAllAlivePetsFlow()
            .map { pets ->
                pets.map { entity ->
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                        isDead = entity.isDead,
                    )
                }
            }
    }

    fun getAllDeadPetsFlow(): Flow<List<Pet>> {
        return database.getDao()
            .selectAllDeadPetsFlow()
            .map { pets ->
                pets.map { entity ->
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                        isDead = entity.isDead,
                    )
                }
            }
    }

    fun getPetByIdFlow(id: Long): Flow<Pet?> {
        return database.getDao()
            .selectPetByIdFlow(id)
            .map { entity ->
                entity?.let {
                    Json.decodeFromString<Pet>(entity.petData).copy(
                        id = entity.id,
                        isDead = entity.isDead,
                    )
                }
            }
    }
}