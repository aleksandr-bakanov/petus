package bav.petus

import bav.petus.entity.WeatherDto
import bav.petus.model.Pet
import bav.petus.repo.PetsRepository
import bav.petus.repo.WeatherRepository

class PetsSDK(
    private val petsRepository: PetsRepository,
    private val weatherRepository: WeatherRepository,
) {

    @Throws(Exception::class)
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherDto {
        return weatherRepository.getWeather(latitude, longitude)
    }

    @Throws(Exception::class)
    suspend fun getPets(): List<Pet> {
        return petsRepository.getAllPets()
    }

    suspend fun addPet(pet: Pet) {
        petsRepository.insertPet(pet)
    }
}