package com.github.fluidsonic.fluid.json

import java.time.DateTimeException
import java.time.ZonedDateTime


object ZonedDateTimeJSONCodec : AbstractJSONCodec<ZonedDateTime, JSONCodingContext>() {

	override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<in ZonedDateTime>) =
		readString().let { raw ->
			try {
				ZonedDateTime.parse(raw)!!
			}
			catch (e: DateTimeException) {
				throw JSONException("Cannot parse ZonedDateTime value: $raw")
			}
		}


	override fun JSONEncoder<JSONCodingContext>.encode(value: ZonedDateTime) =
		writeString(value.toString())
}
