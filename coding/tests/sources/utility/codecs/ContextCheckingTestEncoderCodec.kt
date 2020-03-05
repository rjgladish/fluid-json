package tests.coding

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.*
import io.fluidsonic.json.*


internal class ContextCheckingTestEncoderCodec<in Context : JsonCodingContext>(
	private val expectedContext: Context
) : JsonEncoderCodec<String, Context> {

	override fun JsonEncoder<Context>.encode(value: String) {
		expect(context).toBe(expectedContext)

		StringJsonCodec.run { encode(value) }
	}


	override val encodableClass = String::class
}
