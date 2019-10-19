fluid-json
==========

[![Maven Central](https://img.shields.io/maven-central/v/io.fluidsonic.json/fluid-json?label=Maven%20Central)](https://search.maven.org/artifact/io.fluidsonic.json/fluid-json)
[![JCenter](https://img.shields.io/bintray/v/fluidsonic/kotlin/json?label=JCenter)](https://bintray.com/fluidsonic/kotlin/json)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.50-blue.svg)](https://github.com/JetBrains/kotlin/releases/v1.3.50)
[![Build Status](https://travis-ci.org/fluidsonic/fluid-json.svg?branch=master)](https://travis-ci.org/fluidsonic/fluid-json)
[![#fluid-libraries Slack Channel](https://img.shields.io/badge/slack-%23fluid--libraries-543951.svg)](https://kotlinlang.slack.com/messages/C7UDFSVT2/)

A JSON library written in pure Kotlin.



Table of Contents
-----------------

- [Installation](#installation)
- [Basic Usage](#basic-usage)
- [Annotation Customization](#annotation-customization)
- [Examples](#examples)
- [Manual Coding](#manual-coding)
- [Error Handling](#error-handling)
- [Ktor Client](#ktor-client)
- [Modules](#modules)
- [Testing](#testing)
- [Type Mapping](#type-mapping)
- [Architecture](#architecture)
- [Future Planning](#future-planning)



Installation
------------

`build.gradle.kts`:
```kotlin
plugins {
    kotlin("kapt")
}

dependencies {
    kapt("io.fluidsonic.json:fluid-json-annotation-processor:1.0.0")
    implementation("io.fluidsonic.json:fluid-json-coding-jdk8:1.0.0")
}
```

If you cannot use Java 8, e.g. when supporting Android API 25 or below, replace `fluid-json-coding-jdk8` with `fluid-json-coding`.

If you're using IntelliJ IDEA (not Android Studio) then you have to manually enable the following project setting in order to use annotation processing directly
within the IDE (this is an [open issue](https://youtrack.jetbrains.com/issue/KT-15040) in IntelliJ IDEA):  
_Preferences > Build, Execution, Deployment > Build Tools > Gradle > Runner > Delegate IDE build/run actions to gradle_



Basic Usage
-----------

`fluid-json` uses `@Json`-annotations for automatically generating codec classes at compile-time which are responsible for decoding and encoding from and to
JSON.  
You can also [create these codecs on your own](#manual-coding) instead of relying on annotation processing.


```kotlin
import io.fluidsonic.json.*

@Json
data class Event(
    val attendees: Collection<Attendee>,
    val description: String,
    val end: Instant,
    val id: Int,
    val start: Instant,
    val title: String
)

@Json
data class Attendee(
    val emailAddress: String,
    val firstName: String,
    val lastName: String,
    val rsvp: RSVP?
)

enum class RSVP {
    notGoing,
    going
}
```

Then create a parser and a serializer which make use of the generated codecs:

```kotlin
import io.fluidsonic.json.*

fun main() {
    val data = Event(
       attendees = listOf(
           Attendee(emailAddress = "marc@knaup.io", firstName = "Marc", lastName = "Knaup", rsvp = RSVP.going),
           Attendee(emailAddress = "john@doe.com", firstName = "John", lastName = "Doe", rsvp = null)
       ),
       description = "Discussing the fluid-json library.",
       end = Instant.now() + Duration.ofHours(2),
       id = 1,
       start = Instant.now(),
       title = "fluid-json MeetUp"
   )

    val serializer = JsonCodingSerializer.builder()
        .encodingWith(EventJsonCodec, AttendeeJsonCodec)
        .build()

    val serialized = serializer.serializeValue(data)
    println("serialized: $serialized")

    val parser = JsonCodingParser.builder()
        .decodingWith(EventJsonCodec, AttendeeJsonCodec)
        .build()

    val parsed = parser.parseValueOfType<Event>(serialized)
    println("parsed: $parsed")
}
```

Prints this:
```
serialized: {"attendees":[{"emailAddress":"marc@knaup.io","firstName":"Marc","lastName":"Knaup","rsvp":"going"},{"emailAddress":"john@doe.com","firstName":"John","lastName":"Doe","rsvp":null}],"description":"Discussing the fluid-json library.","end":"2019-03-05T00:45:08.335Z","id":1,"start":"2019-03-04T22:45:08.339Z","title":"fluid-json MeetUp"}

parsed: Event(attendees=[Attendee(emailAddress=marc@knaup.io, firstName=Marc, lastName=Knaup, rsvp=going), Attendee(emailAddress=john@doe.com, firstName=John, lastName=Doe, rsvp=null)], description=Discussing the fluid-json library., end=2019-03-05T00:45:08.335Z, id=1, start=2019-03-04T22:45:08.339Z, title=fluid-json MeetUp)
```
(nope, no [pretty serialization](https://github.com/fluidsonic/fluid-json/issues/15) yet)



Annotation Customization
------------------------

In this section are a few examples on how JSON codec generation can be customized.

The full documentation on all annotations and properties controlling the JSON codec generation can be found in the
[KDoc for `@Json`](https://github.com/fluidsonic/fluid-json/blob/master/annotations/sources/Json.kt).

### Collect all generated codecs in one codec provider

All codecs in your module generated by annotation processing can automatically be added to a single codec provider which makes using these codecs much simpler.
It also frees

```kotlin
@Json.CodecProvider
interface MyCodecProvider: JsonCodecProvider<JsonCodingContext>

fun main() {
    val parser = JsonCodingParser.builder()
        .decodingWith(JsonCodecProvider.generated(MyCodecProvider::class))
        .build()
    // …
}
```

### Customize the generated codec

```kotlin
@Json(
    codecName        = "MyCoordinateCodec",            // customize the JsonCodec's name
    codecPackageName = "some.other.location",          // customize the JsonCodec's package
    codecVisibility  = Json.CodecVisibility.publicRequired  // customize the JsonCodec's visibility
)
data class GeoCoordinate2(
    val latitude: Double,
    val longitude: Double
)
```

### Customize what constructor is used for decoding

```kotlin
@Json(
    decoding = Json.Decoding.annotatedConstructor  // require one constructor to be annotated explicitly
)
data class GeoCoordinate3(
    val altitude: Double,
    val latitude: Double,
    val longitude: Double
) {

    @Json.Constructor
    constructor(latitude: Double, longitude: Double) : this(
        altitude = -1.0,
        latitude = latitude,
        longitude = longitude
    )
}

// input:  {"latitude":50.051961,"longitude":14.431521}
// output: {"altitude":-1.0,"latitude":50.051961,"longitude":14.431521}
```

### Customize what properties are used for encoding (opt-in)

```kotlin
@Json(
    encoding = Json.Encoding.annotatedProperties  // only encode properties annotated explicitly
)
data class User(
    @Json.Property val id: String,
    @Json.Property val name: String,
    val passwordHash: String
)

// input:  {"id":1,"name":"Some User","passwordHash":"123456"}
// output: {"id":1,"name":"Some User"}
```

### Customize what properties are used for encoding (opt-out)

```kotlin
@Json
data class User(
    val id: String,
    val name: String,
    @Json.Excluded val passwordHash: String
)

// input:  {"id":1,"name":"Some User","passwordHash":"123456"}
// output: {"id":1,"name":"Some User"}
```

### Encode extension properties

```kotlin
@Json
data class Person(
    val firstName: String,
    val lastName: String
)

@Json.Property
val Person.name get() = "$firstName $lastName"

// input:  {"firstName":"Marc","lastName":"Knaup"}
// output: {"firstName":"Marc","lastName":"Knaup","name":"Marc Knaup"}
```

### Customize JSON property names

Some prefer it that way ¯\\\_(ツ)\_/¯.

```kotlin
@Json
data class Person(
    @Json.Property("first_name") val firstName: String,
    @Json.Property("last_name") val lastName: String
)

// input/input: {"first_name":"John","last_name":"Doe"}
```

### Inline a single value

```kotlin
@Json(
    representation = Json.Representation.singleValue  // no need to wrap in a structured JSON object
)
class EmailAddress(val value: String)

// input:  "e@mail.com"
// output: "e@mail.com"
```

### Prevent encoding completely

```kotlin
@Json(
    encoding       = Json.Encoding.none,              // prevent encoding altogether
    representation = Json.Representation.singleValue  // no need to wrap in a structured JSON object
)
class Password(val secret: String)

// input:  "123456"
// output: not possible
```

### Prevent decoding completely

```kotlin
@Json(
    decoding = Json.Decoding.none  // prevent decoding altogether
)
class Response<Result>(val result: result)

// input:  not possible
// output: {"result":…}
```

### Add properties depending on the context

```kotlin
@Json(
    decoding = Json.Decoding.none,                // prevent decoding altogether
    encoding = Json.Encoding.annotatedProperties  // only encode properties annotated explicitly
)
data class User(
    @Json.Property val id: String,
    @Json.Property val name: String,
    val emailAddress: String
)


@Json.CustomProperties  // function will be called during encoding
fun JsonEncoder<MyContext>.writeCustomProperties(value: User) {
    if (context.authenticatedUserId == value.id)
        writeMapElement("emailAddress", value = value.emailAddress)
}


@Json.CodecProvider
interface MyCodecProvider: JsonCodecProvider<MyContext>


data class MyContext(
    val authenticatedUserId: String?
): JsonCodingContext


fun main() {
    val serializer = JsonCodingSerializer
        .builder(MyContext(authenticatedUserId = "5678"))
        .encodingWith(JsonCodecProvider.generated(MyCodecProvider::class))
        .build()

    println(serializer.serializeValue(listOf(
        User(id = "1234", name = "Some Other User", emailAddress = "email@hidden.com"),
        User(id = "5678", name = "Authenticated User", emailAddress = "own@email.com")
    )))
}

// input:  not possible
// output: [{"id":"1234","name":"Some Other User"},{"id":"5678","name":"Authenticated User","emailAddress":"own@email.com"}]
```

### Annotate types without having the source code

If a type is not part of your module you can still annotate it indirectly in order to automatically generate a codec for it.
Note that this currently does not work correctly if the type has internal properties or an internal primary constructor.

```kotlin
@Json.CodecProvider(
    externalTypes = [
        Json.ExternalType(Triple::class, Json(
            codecVisibility = Json.CodecVisibility.publicRequired
        ))
    ]
)
interface MyCodecProvider: JsonCodecProvider<JsonCodingContext>
```



Examples
--------

Have a look at the [examples](https://github.com/fluidsonic/fluid-json/tree/master/examples/sources) directory. If you've checked out this project locally then
you can run them directly from within [IntelliJ IDEA](https://www.jetbrains.com/idea/).



Manual Coding
-------------

Instead of using annotations to generate codecs, JSON can be written either directly using low-level APIs or by manually creating codecs to decode and encode
classes from and to JSON. 


### Simple Parsing

```kotlin
… = JsonParser.default.parseValue("""{ "hello": "world", "test": 123 }""")

// returns a value like this:
mapOf(
    "hello" to "world",
    "test" to 123
)
```

You can also accept a `null` value by using `parseValueOrNull` instead.

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0010-Parsing.kt)


### Simple Serializing

```kotlin
JsonSerializer.default.serializeValue(mapOf(
    "hello" to "world",
    "test" to 123
))

// returns a string:
// {"hello":"world","test":123}
```

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0020-Serializing.kt)


### Using Reader and Writer

While the examples above parse and return JSON as `String` you can also use `Reader` and `Writer`:

```kotlin
val reader: Reader = …
… = JsonParser.default.parseValue(source = reader)

val writer: Writer = …
JsonSerializer.default.serializeValue(…, destination = writer)
```

Full example [for Reader](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0011-ParsingFromReader.kt)
and [for Writer](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0021-SerializingToWriter.kt)


### Parsing Lists and Maps

You can also parse lists and maps in a type-safe way directly. Should it not be possible to parse the input as the requested Kotlin type a `JsonException` is
thrown. Note that this requires the `-coding` library variant.

```kotlin
val parser = JsonCodingParser.default

parser.parseValueOfType<List<*>>(…)              // returns List<*>
parser.parseValueOfType<List<String?>>(…)        // returns List<String?>
parser.parseValueOfType<Map<*,*>>(…)             // returns Map<*,*>
parser.parseValueOfType<Map<String,String?>>(…)  // returns Map<String,String?>
```

Note that you can also specify non-nullable `String` instead of nullable `String?`. But due to a limitation of Kotlin and the JVM the resulting list/map can
always contain `null` keys and values. This can cause an unexpected `NullPointerException` at runtime if the source data contains `null`s.

Full example [for Lists](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0012-ParsingLists.kt)
and [for Maps](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0013-ParsingMaps.kt)


### Streaming Parser

`JsonReader` provides an extensive API for reading JSON values from a `Reader`.

```kotlin
val input = StringReader("""{ "data": [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ] }""")
JsonReader.build(input).use { reader ->
    reader.readFromMapByElementValue { key ->
        println(key)

        readFromListByElement {
            println(readInt())
        }
    }
}
```

Full example
[using higher-order functions](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0014-ParsingAsStream.kt) and
[using low-level functions](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0015-ParsingAsStreamLowLevel.kt)


### Streaming Writer

`JsonWriter` provides an extensive API for writing JSON values to a `Writer`.

```kotlin
val output = StringWriter()
JsonWriter.build(output).use { writer ->
    writer.writeIntoMap {
        writeMapElement("data") {
            writeIntoList {
                for (value in 0 .. 10) {
                    json.writeInt(value)
                }
            }
        }
    }
}
```

Full example
[using higher-order functions](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0022-SerializingAsStream.kt) and
[using low-level functions](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0023-SerializingAsStreamLowLevel.kt)


### Type Encoder Codecs

While many basic Kotlin types like `String`, `List`, `Map` and `Boolean` are serialized automatically to their respective JSON counterparts you can easily add
support for other types. Just write a codec for the type you'd like to serialize by implementing `JsonEncoderCodec` and pass an instance to the builder of
either `JsonCodingSerializer` (high-level API) or `JsonEncoder` (streaming API).

Codecs in turn can write other encodable values and `JsonEncoder` will automatically look up the right codec and use it to serialize these values.

If your codec encounters an inappropriate value which it cannot encode then it will throw a `JsonException` in order to stop the serialization process.

Because `JsonEncoderCodec` is simply an interface you can use `AbstractJsonEncoderCodec` as base class for your codec which simplifies implementing that
interface.

```kotlin
data class MyType(…)

object MyTypeCodec : AbstractJsonEncoderCodec<MyType, JsonCodingContext>() {

    override fun JsonEncoder<JsonCodingContext>.encode(value: MyType) {
        // write JSON for `value` directly using the encoder (the receiver)
    }
}
```

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0030-TypeEncoderCodecs.kt)


### Type Decoder Codecs

While all JSON types are parsed automatically using appropriate Kotlin couterparts like `String`, `List`, `Map` and `Boolean` you can easily add support for
other types. Just write a codec for the type you'd like to parse by implementing `JsonDecoderCodec` and pass an instance to the builder of either
`JsonCodingParser` (high-level API) or `JsonDecoder` (streaming API).

Codecs in turn can read other decodable values and `JsonDecoder` will automatically look up the right codec and use it to parse these values.

If your codec encounters inappropriate JSON data which it cannot decode then it will throw a `JsonException` in order to stop the parsing process.

Because `JsonDecoderCodec` is simply an interface you can use `AbstractJsonDecoderCodec` as base class for your codec which simplifies implementing that
interface.

```kotlin
data class MyType(…)

object MyTypeCodec : AbstractJsonDecoderCodec<MyType, JsonCodingContext>() {

    override fun JsonDecoder<JsonCodingContext>.decode(valueType: JsonCodingType<MyType>): MyType {
        // read JSON using and create an instance of `MyType` using decoder (the receiver)
    }
}
```

A `JsonDecoderCodec` can also decode generic types. The instance passed to `JsonCodingType` contains information about generic arguments expected by the call
which caused this codec to be invoked. For `List<Something>` for example a single generic argument of type `Something` would be reported which allows for
example the list codec to serialize the list value's directly as `Something` using the respective codec.

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0031-TypeDecoderCodecs.kt)


### Type Codecs

If you want to be able to encode and decode the same type you can implement the interface `JsonCodec` which in turn extends `JsonEncoderCodec` and
`JsonDecoderCodec`. That way you can reuse the same codec class for both, encoding and decoding.

Because `JsonCodec` is simply an interface you can use `AbstractJsonCodec` as base class for your codec which simplifies implementing that interface.

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0032-TypeCodecs.kt)


### Coding and Streaming

You can use encoding and decoding codecs not just for high-level encoding and decoding using `JsonCodingSerializer` and `JsonCodingParser` but also for
streaming-based encoding and decoding using `JsonEncoder` and `JsonDecoder`.

[Full example](https://github.com/fluidsonic/fluid-json/blob/master/examples/sources/0033-CodingAsStream.kt)


### Thread Safety

All implementations of `JsonParser`, `JsonSerializer`, `JsonCodecProvider` as well as all codecs provided by this library are thread-safe and can be used from
multiple threads without synchronization. It's strongly advised, though not required, that custom implementations are also thread-safe by default.

All other classes and interfaces are not thread-safe and must be used with approriate synchronization in place. It's recommended however to simply use a
separate instance per thread and not share these mutable instances at all.



Error Handling
--------------

Errors occuring during I/O operations in the underlying `Reader` or `Writer` cause an `IOException`.  
Errors occuring due to unsupported or mismatching types, malformed JSON or misused API cause a subclass of `JsonException` being thrown.

Since in Kotlin every method can throw any kind of exception it's recommended to simply catch `Exception` when encoding or decoding JSON - unless handling
errors explicitly is not needed in your use-case. This is especially important if you parse JSON data from an unsafe source like a public API.

### Default `JsonException` subclasses

| Exception                     | Usage
| ----------------------------- | -----
| `JsonException.Parsing`       | Thrown when a `JsonReader` was used improperly, i.e. it's a development error.
| `JsonException.Serialization` | Thrown when a `JsonWriter` was used improperly, e.g. if it would result in malformed JSON.
| `JsonException.Schema`        | Thrown when a `JsonReader` or `JsonDecoder` reads data in an unexpected format, i.e. them schema of the JSON data is wrong.
| `JsonException.Syntax`        | Thrown when a `JsonReader` reads data which is not properly formatted JSON.



Ktor Client
-----------

You can use this library with [`JsonFeature`](https://ktor.io/clients/http-client/features/json-feature.html) of Ktor Client.

`build.gradle.kts`:
```kotlin
dependencies {
    implementation("io.fluidsonic.json:fluid-json-ktor-client:1.0.0")
}
```

Setting up your `HttpClient`:
```kotlin
val client = HttpClient(…) {
    install(JsonFeature) {
        serializer = FluidJsonSerializer(
            parser = JsonCodingParser
                .builder()
                .decodingWith(…)
                .build(),
            serializer = JsonCodingSerializer
                .builder()
                .encodingWith(…)
                .build()
        )
    }
}
```



Modules
-------

| Module                            | Usage
| --------------------------------- | -----
| `fluid-json-annotation-processor` | `@Json`-based `JsonCodec` creation using `kapt`
| `fluid-json-annotations`          | contains `@Json` annotations
| `fluid-json-basic`                | low-level API with `JsonReader`/`JsonParser` and `JsonWriter`/`JsonSerializer`
| `fluid-json-coding`               | `JsonCodec`-based parsing and serialization using `JsonDecoder`/`JsonCodingParser` and `JsonEncoder`/`JsonCodingSerializer`
| `fluid-json-coding-jdk8`          | additional `JsonCodec`s for commonly used Java 8 types on top of `fluid-json-coding`
| `fluid-json-ktor-client`          | plugs in `JsonCodingParser`/`JsonCodingSerializer` to `ktor-client` using its `JsonSerializer`



Testing
-------

This library is tested automatically using [extensive](https://github.com/fluidsonic/fluid-json/tree/master/basic/tests/sources)
[unit](https://github.com/fluidsonic/fluid-json/tree/master/coding/tests/sources)
[tests](https://github.com/fluidsonic/fluid-json/tree/master/coding-jdk8/tests/sources). Some parser tests are imported directly from
[JSONTestSuite](https://github.com/nst/JSONTestSuite) (kudos to [Nicolas Seriot](https://github.com/nst) for that suite).

You can run the tests manually using `Tests` run configuration in IntelliJ IDEA or from the command line by using:

```bash
./gradlew check
```



Type Mapping
------------

### Basic Types

#### Encoding

The default implementations of `JsonWriter` and `JsonSerializer` encode Kotlin types as follows:

| Kotlin          | JSON               | Remarks
| --------------- | ------------------ | -------
| `Array<*>`      | `array<*>`         |
| `Boolean`       | `boolean`          |
| `BooleanArray`  | `array<boolean>`   |
| `Byte`          | `number`           |
| `ByteArray`     | `array<number>`    |
| `Char`          | `string`           |
| `CharArray`     | `array<string>`    |
| `Collection<E>` | `array<*>`         | using decoder/encoder for `E`
| `Double`        | `number`           | must be finite
| `DoubleArray`   | `array<number>`    |
| `Float`         | `number`           | must be finite
| `FloatArray`    | `array<number>`    |
| `Int`           | `number`           |
| `IntArray`      | `array<number>`    |
| `Iterable<E>`   | `array<*>`         | using decoder/encoder for `E`
| `List<E>`       | `array<*>`         | using decoder/encoder for `E`
| `Long`          | `number`           |
| `LongArray`     | `array<number>`    |
| `Map<K,V>`      | `object<string,*>` | key must be `String`, using decoders/encoders for `K` and `V`
| `Number`        | `number`           | unless matched by subclass; encodes as `toDouble()`
| `Sequence<E>`   | `array<*>`         | using decoder/encoder for `E`
| `Set<E>`        | `array<*>`         | using decoder/encoder for `E`
| `Short`         | `number`           |
| `ShortArray`    | `array<number>`    |
| `String`        | `string`           |
| `null`          | `null`             |

#### Decoding

The default implementations of `JsonReader` and `JsonParser` decode JSON types as follows:

| JSON               | Kotlin           | Remarks
| ------------------ | ---------------- | -------
| `array<*>`         | `List<*>`        |
| `boolean`          | `Boolean`        |
| `null`             | `null`           |
| `number`           | `Int`            | if number doesn't include `.` (decimal separator) or `e` (exponent separator) and fits into `Int`
| `number`           | `Long`           | if number doesn't include `.` (decimal separator) or `e` (exponent separator) and fits into `Long`
| `number`           | `Double`         | otherwise
| `object<string,*>` | `Map<String,*>`  |
| `string`           | `String`         |

### Extended Types

The following classes of the can also be decoded and encoded out of the box.  
For types in the `java.time` package the `-coding-jdk8` library variant must be used.

| Kotlin           | JSON                                | Remarks
| ---------------- | ----------------------------------- | -------
| `CharRange`      | `{ "start": …, "endInclusive": … }` | using `string` value
| `ClosedRange<C>` | `{ "start": …, "endInclusive": … }` | using decoder/encoder for `C`
| `Enum`           | `string`                            | uses `.toString()` and converts to `lowerCamelCase` (can be configured)
| `DayOfWeek`      | `string`                            | `"monday"`, …, `"friday"`
| `Duration`       | `string`                            | using `.parse()` / `.toString()`
| `Instant`        | `string`                            | using `.parse()` / `.toString()`
| `IntRange`       | `{ "start": …, "endInclusive": … }` | using `number` values
| `LocalDate`      | `string`                            | using `.parse()` / `.toString()`
| `LocalDateTime`  | `string`                            | using `.parse()` / `.toString()`
| `LocalTime`      | `string`                            | using `.parse()` / `.toString()`
| `LongRange`      | `{ "start": …, "endInclusive": … }` | using `number` values
| `MonthDay`       | `string`                            | using `.parse()` / `.toString()`
| `Month`          | `string`                            | `"january"`, …, `"december"`
| `OffsetDateTime` | `string`                            | using `.parse()` / `.toString()`
| `OffsetTime`     | `string`                            | using `.parse()` / `.toString()`
| `Period`         | `string`                            | using `.parse()` / `.toString()`
| `Year`           | `int`                               | using `.value`
| `YearMonth`      | `string`                            | using `.parse()` / `.toString()`
| `ZonedDateTime`  | `string`                            | using `.parse()` / `.toString()`
| `ZoneId`         | `string`                            | using `.of()` / `.id`
| `ZoneOffset`     | `string`                            | using `.of()` / `.id`



Architecture
------------

-   `JsonReader`/`JsonWriter` are at the lowest level and read/write JSON as a stream of `JsonToken`s:
    - part of `-basic` library variant
    - character-level input/output
    - validation of read/written syntax
    - one instance per parsing/serializing (maintains state & holds reference to `Reader`/`Writer`)
-   `JsonParser`/`JsonSerializer` are built on top of `JsonReader`/`JsonWriter` and read/write a complete JSON value at once.
    - part of `-basic` library variant
    - completely hides usage of underlying `JsonReader`/`JsonWriter`
    - encoding is performed using the actual type of values to be encoded
    - decoding is performed using the type expected by the caller of `JsonParser`'s `parse…` methods and only available for basic types
    - instance can be reused and creates one `JsonReader`/`JsonWriter` per parsing/serialization invocation
    - ease of use is important
-   `JsonDecoder`/`JsonEncoder` are built on top of `JsonReader`/`JsonWriter` and decode/encode arbitrary Kotlin types from/to a stream of `JsonToken`s:
    - part of `-coding` library variant
    - most read/write operations are forwarded to the underlying `JsonReader`/`JsonWriter`
    - some read/write operations are intercepted by `JsonEncoder` to encode compatible types using codecs
    - implementations provided by `JsonDecoderCodec`s and `JsonEncoderCodec`s
    - inspired by MongoDB's [Codec and CodecRegistry](http://mongodb.github.io/mongo-java-driver/3.9/bson/codecs/)
    - one instance per parsing/serialization invocation (holds reference to `JsonReader`/`JsonWriter`)
-   `JsonCodingParser`/`JsonCodingSerializer` are built on top of `JsonDecoder`/`JsonEncoder` and read/write a complete JSON value at once.
    - part of `-coding` library variant
    - completely hides usage of underlying `JsonDecoder`/`JsonEncoder`
    - encoding is performed using the actual type of values to be encoded using a matching `JsonEncoderCodec` implementation
    - decoding is performed using the type expected by the caller of `JsonParser`'s `parse…` methods and a matching `JsonDecoderCodec` implementation
    - instance can be reused and creates one `JsonDecoder`/`JsonEncoder` per parsing/serialization invocation
    - ease of use is important

Most public API is provided as `interface`s in order to allow for plugging in custom behavior and to allow easy unit testing of code which produces or consumes
JSON.

The default implementations of `JsonDecoder`/`JsonEncoder` use a set of pre-defined codecs in order to support decoding/encoding various basic Kotlin types like
`String`, `List`, `Map`, `Boolean` and so on. Codecs for classes which are available only since Java 8 are provided by the `-coding-jdk8` library variant.

### Recursive vs. Non-Recursive

While codec-based decoding/encoding has to be implemented recursively in order to be efficient and easy to use it's sometimes not desirable to parse/serialize
JSON recursively. For that reason the default container codecs like `MapJsonCodec` also provide a `nonRecursive` codec. Since they read/write a whole value at
once using `JsonReader`'s/`JsonWriter`'s primitive `read*`/`write*` methods they will not use any other codecs and thus don't support encoding or decoding other
non-basic types.

`JsonCodingParser.nonRecursive` and `JsonCodingSerializer.nonRecursive` both operate on these codecs and are thus a non-recursive parser/serializer.

### Classes and Interfaces

| Type                       | Description
| -------------------------- | -----------
| `AbstractJsonCodec`        | Abstract base class which simplifies implementing `JsonCodec`.
| `AbstractJsonDecoderCodec` | Abstract base class which simplifies implementing `JsonDecoderCodec`.
| `AbstractJsonEncoderCodec` | Abstract base class which simplifies implementing `JsonEncoderCodec`.
| `DefaultJsonCodecs`        | Contains lists of default codecs which can be used when contructing custom `JsonCodecProvider`s.
| `JsonCodec`                | Interface for classes which implement both, `JsonEncoderCodec` and `JsonDecoderCodec`. Also simplifies creating such codecs.
| `JsonCodecProvider`        | Interface for classes which when given a `JsonCodingType` (for decoding) or `KClass` (for encoding) return a codec which is able to decode/encode values of that type.
| `JsonCodingContext`        | Interface for context types. Instances of context types can be passed to `JsonParser`, `JsonSerializer`, `JsonDecoder` and `JsonEncoder`. They in turn can be used by custom codecs to help decoding/encoding values if needed.
| `JsonCodingParser`         | Interface for high-level reusable JSON parsers with codec providers and context already configured.
| `JsonCodingSerializer`     | Interface for high-level reusable JSON serializers where codec providers and context are already configured.
| `JsonCodingType`           | Roughly describes a Kotlin type which can be decoded from JSON. It includes relevant generic information which allows decoding for example `List<Something>` instead of just `List<*>`. Also known as [type token](http://gafter.blogspot.de/2006/12/super-type-tokens.html)).
| `JsonDecoder`              | Interface which extends `JsonReader` to enable reading values of any Kotlin type from JSON using `JsonCodecProvider`s for type mapping.
| `JsonDecoderCodec`         | Interface for decoding a value of a specific Kotlin type using a `JsonDecoder`.
| `JsonEncoder`              | Interface which extends `JsonWriter` to enable writing values of any Kotlin type as JSON using `JsonCodecProvider`s for type mapping.
| `JsonEncoderCodec`         | Interface for encoding a value of a specific Kotlin type using a `JsonEncoder`.
| `JsonException`            | Exception base class which is thrown whenever JSON cannot be written or read for non-IO reasons (e.g. malformed JSON, wrong state in reader/writer, missing type mapping).
| `JsonParser`               | Interface for high-level reusable JSON parsers which support only basic types.
| `JsonReader`               | Interface for low-level JSON parsing on a token-by-token basis.
| `JsonSerializer`           | Interface for high-level reusable JSON serializers which support only basic types.
| `JsonToken`                | Enum containing all types of tokens a `JsonReader` can read.
| `JsonWriter`               | Interface for low-level JSON serialization on a token-by-token basis.
| `*Codec`                   | The various codec classes are concrete codecs for common Kotlin types.



Future Planning
---------------

This is on the backlog for later consideration, in no specific order:

- [Add KDoc to all public API](https://github.com/fluidsonic/fluid-json/issues/28)
- [Add performance testing](https://github.com/fluidsonic/fluid-json/issues/4)
- [Add low-level support for `BigDecimal` / `BigInteger`](https://github.com/fluidsonic/fluid-json/issues/18)
- [Add pretty serialization](https://github.com/fluidsonic/fluid-json/issues/15)



License
-------

Apache 2.0


--------------------------

[![Awesome Kotlin](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
