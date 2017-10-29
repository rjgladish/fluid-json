package tests

import com.github.fluidsonic.fluid.json.*


internal object MapJSONTestCodec : AbstractJSONCodec<Map<*, *>, JSONCoderContext>(
	additionalProviders = listOf(AnyJSONDecoderCodec, BooleanJSONCodec, LocalDateCodec, NumberJSONCodec, StringJSONCodec)
) {

	override fun decode(valueType: JSONCodableType<in Map<*, *>>, decoder: JSONDecoder<out JSONCoderContext>) =
		MapJSONCodec.decode(valueType, decoder)


	override fun encode(value: Map<*, *>, encoder: JSONEncoder<out JSONCoderContext>) =
		MapJSONCodec.encode(value, encoder)


	object NonRecursive : AbstractJSONCodec<Map<String, *>, JSONCoderContext>(
		additionalProviders = listOf(BooleanJSONCodec, IntJSONCodec, StringJSONCodec)
	) {

		override fun decode(valueType: JSONCodableType<in Map<String, *>>, decoder: JSONDecoder<out JSONCoderContext>) =
			MapJSONCodec.nonRecursive.decode(valueType, decoder)


		override fun encode(value: Map<String, *>, encoder: JSONEncoder<out JSONCoderContext>) =
			MapJSONCodec.nonRecursive.encode(value, encoder)
	}
}
