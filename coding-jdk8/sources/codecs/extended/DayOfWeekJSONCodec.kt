package com.github.fluidsonic.fluid.json

import java.time.DayOfWeek


// TODO use Enum codec once implemented
object DayOfWeekJSONCodec : AbstractJSONCodec<DayOfWeek, JSONCodingContext>() {

	override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<in DayOfWeek>) =
		readString().let { raw ->
			when (raw) {
				"monday" -> DayOfWeek.MONDAY
				"tuesday" -> DayOfWeek.TUESDAY
				"wednesday" -> DayOfWeek.WEDNESDAY
				"thursday" -> DayOfWeek.THURSDAY
				"friday" -> DayOfWeek.FRIDAY
				"saturday" -> DayOfWeek.SATURDAY
				"sunday" -> DayOfWeek.SUNDAY
				else -> throw  JSONException("Invalid DayOfWeek value: $raw")
			}
		}


	override fun JSONEncoder<JSONCodingContext>.encode(value: DayOfWeek) =
		writeString(when (value) {
			DayOfWeek.MONDAY -> "monday"
			DayOfWeek.TUESDAY -> "tuesday"
			DayOfWeek.WEDNESDAY -> "wednesday"
			DayOfWeek.THURSDAY -> "thursday"
			DayOfWeek.FRIDAY -> "friday"
			DayOfWeek.SATURDAY -> "saturday"
			DayOfWeek.SUNDAY -> "sunday"
		})
}
