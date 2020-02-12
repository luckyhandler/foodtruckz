package entity

data class FoodtruckzWrapper(
    val tours: List<ToursItem>?,
    val operators: List<OperatorsItem>?,
    val error: Boolean?,
    val message: String?,
    val code: String?
)

data class ToursItem(
    val id: String?,
    val operatorid: String?,
    val type: Int?,
    val name: String?,
    val description: String?,
    val start: String?,
    val end: String?,
    val timezone: String?,
    val soldout: Boolean?,
    val location: Location?
)

data class Location(
    val name: String?,
    val street: String?,
    val number: String?,
    val zipcode: String?,
    val city: String?,
    val countryId: String?,
    val sponsor: String?,
    val map: Map?,
    val country: Country?
)

data class Map(
    val longitude: String?,
    val latitude: String?,
    val icon: String?,
    val colors: Colors?
)

data class Country(
    val id: String?,
    val iso: String?,
    val addressType: String?,
    val dateFormat: String?,
    val timeFormat: String?
)

data class OperatorsItem(
    val id: String?,
    val name: String?,
    val nameShort: String?,
    val description: String?,
    val url: String?,
    val offer: String?,
    val tags: List<String>?,
    val region: String?,
    val logo: String?,
    val impressions: List<String>?,
    val colors: Colors?,
    val premium: Boolean?,
    val nameUrl: String?
)

data class Colors(
    val truck: String?,
    val text: String?
)