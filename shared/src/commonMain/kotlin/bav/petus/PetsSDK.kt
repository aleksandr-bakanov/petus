package bav.petus

import bav.petus.cache.Database
import bav.petus.cache.DatabaseDriverFactory
import bav.petus.entity.WeatherDto
import bav.petus.network.WeatherApi

class PetsSDK(databaseDriverFactory: DatabaseDriverFactory, val api: WeatherApi) {
    private val database = Database(databaseDriverFactory)

    @Throws(Exception::class)
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherDto {
        return api.getWeather(latitude, longitude)
    }

    @Throws(Exception::class)
    fun getPets(): List<Pet> {
        return database.getAllPets()
    }

    fun addPet(pet: Pet) {
        database.insertPet(pet)
    }
}