package caio.caminha.utils

inline fun <reified T> T.toJsonString(): String {
    return KotlinSerializationJsonParser
        .DEFAULT_KOTLIN_SERIALIZATION_PARSER.encodeToString(this)
}