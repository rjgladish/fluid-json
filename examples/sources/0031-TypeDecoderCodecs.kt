package examples

import com.github.fluidsonic.fluid.json.AbstractJSONDecoderCodec
import com.github.fluidsonic.fluid.json.JSONCodingContext
import com.github.fluidsonic.fluid.json.JSONCodingParser
import com.github.fluidsonic.fluid.json.JSONCodingType
import com.github.fluidsonic.fluid.json.JSONDecoder
import com.github.fluidsonic.fluid.json.JSONException
import com.github.fluidsonic.fluid.json.parseValueOfType
import com.github.fluidsonic.fluid.json.readFromMapByElementValue
import com.github.fluidsonic.fluid.json.readValueOfType
import java.time.Instant


object DecodingExample {

	@JvmStatic
	fun main() {
		// Using a codec for decoding specific classes simplifies JSON parsing a lot

		val parser = JSONCodingParser.builder()
			.decodingWith(EventCodec)
			.build()

		val json = parser.parseValueOfType<List<Event>>("""
			[
			   {
			      "id":1,
			      "date":"2001-09-09T01:46:40Z",
			      "title":"One"
			   },
			   {
			      "id":2,
			      "date":"2033-05-18T03:33:20Z",
			      "title":"Two"
			   },
			   {
			      "id":3,
			      "date":"2065-01-24T05:20:00Z",
			      "title":"Three"
			   },
			   {
			      "id":4,
			      "date":"2096-10-02T07:06:40Z",
			      "title":"Four"
			   },
			   {
			      "id":5,
			      "date":"2128-06-11T08:53:20Z",
			      "title":"Five"
			   },
			   {
			      "id":6,
			      "title":"Six"
			   }
			]
		""")

		println(json)
	}


	private data class Event(
		val id: Int,
		val date: Instant? = null,
		val title: String
	)


	private object EventCodec : AbstractJSONDecoderCodec<Event, JSONCodingContext>() {

		override fun JSONDecoder<JSONCodingContext>.decode(valueType: JSONCodingType<in Event>): Event {
			var id: Int? = null
			var date: Instant? = null
			var title: String? = null

			readFromMapByElementValue { key ->
				when (key) {
					"id" -> id = readInt()
					"date" -> date = readValueOfType()
					"title" -> title = readString()
					else -> skipValue()
				}
			}

			return Event(
				id = id ?: throw JSONException("missing id"),
				date = date,
				title = title ?: throw JSONException("missing title")
			)
		}
	}
}
