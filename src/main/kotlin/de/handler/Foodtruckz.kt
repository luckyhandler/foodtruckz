package de.handler

import de.handler.entity.ChatMessage
import de.handler.entity.Foodtruck
import de.handler.entity.FoodtruckzWrapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.*


const val longitude: String = "11.017400"
const val latitude: String = "49.429017"
const val foodtruckUrl: String = "https://www.craftplaces-business.com/api/locations/getTours.json" +
        "?longitude=$longitude" +
        "&latitude=$latitude" +
        "&rd=2&version=2"

fun main() {
    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    runBlocking {
        val foodtrucks = withContext(Dispatchers.IO) {
            getFoodtruckz(httpClient)
        }

        val chatMessage: ChatMessage = foodtrucks.mapToChatMessage()

        withContext(Dispatchers.IO) {
            //postFoodtruckz(httpClient, chatMessage)
            println(chatMessage)
        }
    }
}

private suspend fun getFoodtruckz(httpClient: HttpClient): List<Foodtruck> {
    // Encode string for basic auth
    val auth: String = Base64.getEncoder()
        .encodeToString(
            ("token" + ":" + Keys.token).toByteArray(StandardCharsets.UTF_8)
        )

    // Issue request
    val foodtruckzWrapper = httpClient.get<FoodtruckzWrapper> {
        url(foodtruckUrl)
        header(
            key = "Authorization",
            value = "Basic $auth"
        )
    }

    // Filter foodtrucks for specified date
    val now = ZonedDateTime.now()
    val foodtrucksForToday = foodtruckzWrapper.filterForDate(
        from = ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, 10, 0, 0, 0, now.zone),
        to = ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, 15, 0, 0, 0, now.zone).plusDays(1)
    )

    // Map to local entities and return
    return foodtrucksForToday.mapToLocalEntityList()
}

private suspend fun postFoodtruckz(httpClient: HttpClient, chatMessage: ChatMessage) {
    httpClient.post<Unit>(Hooks.hook) {
        this.header("Content-Type", "application/json")
        this.body = chatMessage
    }
}
