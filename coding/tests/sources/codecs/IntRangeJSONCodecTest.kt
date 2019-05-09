package tests.coding

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.verbs.*
import com.github.fluidsonic.fluid.json.*
import org.junit.jupiter.api.*


@Suppress("UNCHECKED_CAST")
internal object IntRangeJSONCodecTest {

	val codecs = JSONCodecProvider(
		IntJSONCodec,
		IntRangeJSONCodec
	)

	val parser = JSONCodingParser.builder()
		.decodingWith(codecs, base = null)
		.build()

	val serializer = JSONCodingSerializer.builder()
		.encodingWith(codecs, base = null)
		.build()


	@Test
	fun testDecodesIntRange() {
		assert(parser.parseValueOfType<IntRange>("""{"start":0,"endInclusive":1}"""))
			.toBe(IntRange(0, 1))
	}


	@Test
	fun testEncodesIntRange() {
		assert(serializer.serializeValue(IntRange(0, 1)))
			.toBe("""{"start":0,"endInclusive":1}""")
	}
}
