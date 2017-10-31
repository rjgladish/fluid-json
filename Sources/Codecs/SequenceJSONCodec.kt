package com.github.fluidsonic.fluid.json


object SequenceJSONCodec : AbstractJSONCodec<Sequence<*>, JSONCoderContext>() {

	override fun decode(valueType: JSONCodableType<in Sequence<*>>, decoder: JSONDecoder<out JSONCoderContext>): Sequence<*> {
		val elementType = valueType.arguments.single()

		return decoder.readListByElement {
			readValueOfTypeOrNull(elementType)
		}.asSequence()
	}


	override fun encode(value: Sequence<*>, encoder: JSONEncoder<out JSONCoderContext>) =
		encoder.writeList(value)


	val nonRecursive = NonRecursiveJSONCodec.create<Sequence<*>>()
}
