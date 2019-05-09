package com.github.fluidsonic.fluid.json

import java.time.*


object LocalTimeJSONCodec : AbstractJSONCodec<LocalTime, JSONCodingContext>() {

	override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<LocalTime>) =
		readString().let { raw ->
			try {
				LocalTime.parse(raw)!!
			}
			catch (e: DateTimeException) {
				invalidValueError("time in ISO-8601 format expected, got '$raw'")
			}
		}


	override fun JSONEncoder<JSONCodingContext>.encode(value: LocalTime) =
		writeString(value.toString())
}
