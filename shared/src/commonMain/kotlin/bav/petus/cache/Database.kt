package bav.petus.cache

import bav.petus.Pet
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun getAllPets(): List<Pet> {
        return dbQuery.selectAllPets(::mapPetsSelecting).executeAsList()
    }

    internal fun insertPet(pet: Pet) {
        dbQuery.insertPet(
            id = null,
            isDead = pet.isDead,
            petData = Json.encodeToString(pet)
        )
    }

    private fun mapPetsSelecting(
        id: Long,
        isDead: Boolean,
        petData: String,
    ) : Pet {
        return Json.decodeFromString<Pet>(petData)
    }

}