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
import java.time.format.DateTimeFormatter
import java.util.*


private const val longitude: String = "11.017400"
private const val latitude: String = "49.429017"

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

        val preparedString = withContext(Dispatchers.Default) {
            prepareFoodtruckzString(foodtrucks)
        }

        withContext(Dispatchers.IO) {
            postFoodtruckz(httpClient, preparedString)
        }
    }
}

private suspend fun getFoodtruckz(httpClient: HttpClient): List<Foodtruck> {
    val encoded: String = Base64.getEncoder()
        .encodeToString(
            ("token" + ":" + Keys.token).toByteArray(StandardCharsets.UTF_8)
        )
    val foodtruckzWrapper = httpClient.get<FoodtruckzWrapper> {
        url(
            "" +
                    "https://www.craftplaces-business.com/api/locations/getTours.json" +
                    "?longitude=$longitude" +
                    "&latitude=$latitude" +
                    "&rd=2&version=2"
        )
        header(
            key = "Authorization",
            value = "Basic $encoded"
        )
    }

    return mapFoodtruckz(foodtruckzWrapper)
}

private fun mapFoodtruckz(foodtruckzWrapper: FoodtruckzWrapper): List<Foodtruck> {
    val (tours, operators, _, _, _) = foodtruckzWrapper
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.getDefault())

    return tours
        ?.filter {
            val startDate = ZonedDateTime.parse(it.start)
            startDate.isAfter(ZonedDateTime.now()) && startDate.isBefore(ZonedDateTime.now().plusDays(1))
        }
        ?.mapIndexed { index, toursItem ->
            Foodtruck(
                name = operators?.get(index)?.name ?: "",
                description = operators?.get(index)?.description ?: "",
                location = "" +
                        "${toursItem.location?.name}\n" +
                        "${toursItem.location?.zipcode} " +
                        "${toursItem.location?.city}, \n" +
                        "${toursItem.location?.street} " +
                        "${toursItem.location?.number}",
                time = timeFormatter.format(ZonedDateTime.parse(toursItem.start)) +
                        " - " +
                        timeFormatter.format(ZonedDateTime.parse(toursItem.end)) +
                        " (${dateFormatter.format(ZonedDateTime.parse(toursItem.start))}.)"
            )
        }.orEmpty()
}

private suspend fun prepareFoodtruckzString(foodtrucks: List<Foodtruck>): String {
    return withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        foodtrucks.forEach {
            stringBuilder.append("\n${it.name}\n${it.description}\n${it.location}\n${it.time}\n")
        }

        """
            "{
                "text": ":hamburger: ${stringBuilder.toString()}"
            }"
        """
    }
}

private suspend fun postFoodtruckz(httpClient: HttpClient, post: String) {
    httpClient.request<String> {
        this.method = HttpMethod.Post
        this.url(Hooks.hook)
        this.header("Content-Type", "application/json")
        this.body = post
    }
}
