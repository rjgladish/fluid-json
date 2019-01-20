package com.github.fluidsonic.fluid.json


internal class StandardDecoder<out Context : JSONCodingContext>(
	override val context: Context,
	private val codecProvider: JSONCodecProvider<Context>,
	source: JSONReader
) : JSONDecoder<Context>, JSONReader by source {

	override fun readValue() =
		super<JSONDecoder>.readValue()


	override fun <Value : Any> readValueOfType(valueType: JSONCodingType<Value>) =
		codecProvider.decoderCodecForType(valueType)
			?.run { decode(valueType = valueType) }
			?: throw JSONException("no decoder codec registered for $valueType")
}
