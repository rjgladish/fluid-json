package com.github.fluidsonic.fluid.json

import java.time.*


object ZoneOffsetJSONCodec : AbstractJSONCodec<ZoneOffset, JSONCodingContext>() {

	override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<ZoneOffset>) =
		readString().let { raw ->
			try {
				ZoneOffset.of(raw)!!
			}
			catch (e: DateTimeException) {
				invalidValueError("time offset in ISO-8601 format expected, got '$raw'")
			}
		}


	override fun JSONEncoder<JSONCodingContext>.encode(value: ZoneOffset) =
		writeString(value.id)
}
