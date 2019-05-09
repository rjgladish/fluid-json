package com.github.fluidsonic.fluid.json

import java.time.*


object OffsetTimeJSONCodec : AbstractJSONCodec<OffsetTime, JSONCodingContext>() {

	override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<OffsetTime>) =
		readString().let { raw ->
			try {
				OffsetTime.parse(raw)!!
			}
			catch (e: DateTimeException) {
				invalidValueError("time and time zone in ISO-8601 format expected, got '$raw'")
			}
		}


	override fun JSONEncoder<JSONCodingContext>.encode(value: OffsetTime) =
		writeString(value.toString())
}
