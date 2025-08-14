package caio.caminha.utils

import kotlinx.serialization.json.Json

class KotlinSerializationJsonParser  {

    companion object {
        val DEFAULT_KOTLIN_SERIALIZATION_PARSER = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = true
            isLenient = true
        }
    }



}