import entity.Foodtruck
import entity.FoodtruckzWrapper
import entity.OperatorsItem
import entity.ToursItem
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Mapper {
    fun filterForDate(foodtruckzWrapper: FoodtruckzWrapper, from: ZonedDateTime, to: ZonedDateTime) : FoodtruckzWrapper {
        val tours = foodtruckzWrapper.tours?.filter { tour: ToursItem ->
            val startDate = ZonedDateTime.parse(tour.start)
            startDate.isAfter(from) && startDate.isBefore(to)
        }.orEmpty()

        val operators = foodtruckzWrapper.operators?.filter { operator: OperatorsItem ->
            tours.any { toursItem -> toursItem.id == operator.id }
        }.orEmpty()

        return foodtruckzWrapper.copy(
            tours = tours,
            operators = operators
        )
    }

    fun mapToLocalEntityList(foodtruckzWrapper: FoodtruckzWrapper): List<Foodtruck> {
        val (tours, operators, _, _, _) = foodtruckzWrapper
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM", Locale.getDefault())

        return tours
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

    fun mapToChatMessage(foodtrucks: List<Foodtruck>): String {
        val stringBuilder = StringBuilder()
        foodtrucks.forEach {
            stringBuilder.append("\n${it.name}\n${it.description}\n${it.location}\n${it.time}\n")
        }

        return """{"text": ":hamburger: $stringBuilder"}"""
    }
}