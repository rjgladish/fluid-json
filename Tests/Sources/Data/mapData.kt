package tests


// FIXME check element order, encodable keys
internal val mapData = TestData<Map<*, *>>(
	symmetric = mapOf(
		emptyMap<Any?, Any?>() to "{}",
		mapOf("key" to 1) to """{"key":1}""",
		mapOf(
			"key0" to true,
			"key1" to "hey",
			"key2" to null
		) to """{"key0":true,"key1":"hey","key2":null}""",
		mapOf(
			"key0" to emptyMap<String, Any>(),
			"key1" to mapOf("key" to 1)
		) to """{"key0":{},"key1":{"key":1}}"""
	),
	decodableOnly = mapOf(
		" { \t\n\r} " to emptyMap<Any?, Any?>(),
		"""{ "key0": true, "key1" :"hey", "key2" : null }""" to mapOf(
			"key0" to true,
			"key1" to "hey",
			"key2" to null
		)
	)
)
