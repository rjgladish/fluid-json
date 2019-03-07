package codecProvider

import com.github.fluidsonic.fluid.json.*


@JSON.CodecProvider(
	externalTypes = [
		JSON.ExternalType(KT30280::class, JSON(codecPackageName = "externalType"), targetName = "codecProvider.KT30280"),
		JSON.ExternalType(KT30280Primitive::class, JSON(codecPackageName = "externalType"), targetName = "codecProvider.KT30280Primitive"),
		JSON.ExternalType(Pair::class, JSON(
			codecName = "ExternalPairCodec",
			codecPackageName = "externalType",
			codecVisibility = JSON.CodecVisibility.publicRequired
		))
	]
)
interface CustomContextCodecProvider : JSONCodecProvider<CustomCodingContext>


interface CustomCodingContext : JSONCodingContext

inline class KT30280(val value: String)
inline class KT30280Primitive(val value: Double)
