import entity.Foodtruck
import entity.FoodtruckzWrapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
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

        val chatMessage = Mapper.mapToChatMessage(foodtrucks)

        withContext(Dispatchers.IO) {
            postFoodtruckz(httpClient, chatMessage)
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
    val foodtrucksForToday = Mapper.filterForDate(
        foodtruckzWrapper = foodtruckzWrapper,
        from = ZonedDateTime.now(),
        to = ZonedDateTime.now().plusDays(1)
    )

    // Map to local entities and return
    return Mapper.mapToLocalEntityList(foodtrucksForToday)
}

private suspend fun postFoodtruckz(httpClient: HttpClient, post: String) {
    httpClient.request<String> {
        this.method = HttpMethod.Post
        this.url(Hooks.hook)
        this.header("Content-Type", "application/json")
        this.body = post
    }
}
