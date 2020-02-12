import com.google.gson.Gson
import entity.Foodtruck
import entity.FoodtruckzWrapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class FoodtruckzKtTest {
    private lateinit var client: HttpClient

    @BeforeEach
    fun setUp() {
        client = HttpClient(MockEngine) {
            engine {
                addHandler { request: HttpRequestData ->
                    when (request.url.toString()) {
                        foodtruckUrl -> {
                            val responseHeaders = headersOf("Content-Type" to listOf("application/json"))
                            val response = this.javaClass.getResource("/foodtruckz.json").readText()
                            respond(response, headers = responseHeaders)
                        }
                        else -> error("Unhandled ${request.url}")
                    }
                }
            }
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }

    @Test
    fun `json should be correctly parsed`() {
        runBlocking {
            val response: FoodtruckzWrapper = client.get(foodtruckUrl)
            assert(!response.error)
            assert(response.tours != null)
            assert(response.tours!!.isNotEmpty())
            assert(response.operators?.size == response.tours.size)
        }
    }

    @Test
    fun `dtos should be correctly mapped to local objects`() {
        runBlocking {
            val response: FoodtruckzWrapper = client.get(foodtruckUrl)
            val foodtrucks: List<Foodtruck> = Mapper.mapToLocalEntityList(response)
            assert(foodtrucks.isNotEmpty())
            foodtrucks.forEach { (name, description, location, time) ->
                assert(name.isNotEmpty())
                assert(description.isNotEmpty())
                assert(location.isNotEmpty())
                assert(time.isNotEmpty())
            }
            assert(response.tours?.size == foodtrucks.size)
        }
    }

    @Test
    fun `dtos should be correctly filtered for specified date`() {
        runBlocking {
            val response: FoodtruckzWrapper = client.get(foodtruckUrl)
            val foodtrucksForToday: FoodtruckzWrapper = Mapper.filterForDate(
                foodtruckzWrapper = response,
                from = ZonedDateTime.parse("2020-02-11T00:00:00+01:00"),
                to = ZonedDateTime.parse("2020-02-11T00:00:00+01:00").plusDays(1)
            )

            assert(foodtrucksForToday.tours?.size == 1)
        }
    }

    @Test
    fun `local entities should be correctly mapped into a chat message`() {
        runBlocking {
            val response: FoodtruckzWrapper = client.get(foodtruckUrl)
            val foodtrucks = Mapper.mapToLocalEntityList(response)

            val foodtruckzMessage: String = Mapper.mapToChatMessage(foodtrucks)

            assert(!foodtruckzMessage.isBlank())
            val chatMessage = Gson().fromJson<ChatMessage>(foodtruckzMessage, ChatMessage::class.java)
            assert(chatMessage != null)
        }
    }

    data class ChatMessage(
        val text: String?
    )
}