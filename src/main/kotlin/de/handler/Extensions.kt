package de.handler

import com.google.gson.Gson
import de.handler.entity.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val gson = Gson()

fun FoodtruckzWrapper.filterForDate(from: ZonedDateTime, to: ZonedDateTime): FoodtruckzWrapper {
    val tours = this.tours?.filter { tour: ToursItem ->
        val startDate = ZonedDateTime.parse(tour.start)
        startDate.isAfter(from) && startDate.isBefore(to)
    }.orEmpty()

    val operators = this.operators?.filter { operator: OperatorsItem ->
        tours.any { toursItem -> toursItem.operatorid == operator.id }
    }.orEmpty()

    return this.copy(
        tours = tours,
        operators = operators
    )
}

fun FoodtruckzWrapper.mapToLocalEntityList(): List<Foodtruck> {
    val (tours, operators, _, _, _) = this
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
                        " (${dateFormatter.format(ZonedDateTime.parse(toursItem.start))}.)",
                url = operators?.get(index)?.name_url ?: ""
            )
        }.orEmpty()
}

fun List<Foodtruck>.mapToChatMessage(): ChatMessage {
    val stringBuilder = StringBuilder()
    if (this.isEmpty()) {
        stringBuilder.append("**Heute gibt es leider keine Foodtrucks**   :cry:   ")
    } else {
        stringBuilder.append("**Foodtrucks heute**   :hamburger:   ")
        this.forEach {
            stringBuilder.append("\n\n**${it.name}**\n\n${it.description}\n\n${it.location}\n\n${it.time}\n\n${it.url}\n\n")
        }
    }

    return gson.fromJson(
        """{"text":  "$stringBuilder"}""".trimMargin(),
        ChatMessage::class.java
    )
        ?: throw IllegalStateException("Mapping the chatMessage string to a ChatMessage object failed")
}