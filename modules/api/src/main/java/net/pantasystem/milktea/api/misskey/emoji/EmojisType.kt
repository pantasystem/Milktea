package net.pantasystem.milktea.api.misskey.emoji

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import net.pantasystem.milktea.model.emoji.Emoji


@kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
sealed interface EmojisType {
    @kotlinx.serialization.Serializable(with = TypeArraySerializer::class)
    data class TypeArray(val emojis: List<Emoji>) : EmojisType

    @kotlinx.serialization.Serializable(with = TypeObjectSerializer::class)
    data class TypeObject(val emojis: Map<String, String>) : EmojisType

    @kotlinx.serialization.Serializable
    object None : EmojisType
}

@kotlinx.serialization.Serializable
data class TestNoteObject(
    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class) val emojis: EmojisType
)



class CustomEmojisTypeSerializer : JsonContentPolymorphicSerializer<EmojisType>(EmojisType::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out EmojisType> {
        if (element is JsonArray) {
            return TypeArraySerializer()
        }
        if (element is JsonObject) {
            return TypeObjectSerializer
        }

        return EmojisType.None.serializer()
    }

}

object TypeObjectSerializer : KSerializer<EmojisType.TypeObject> {
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())
    override val descriptor: SerialDescriptor = mapSerializer.descriptor
    override fun deserialize(decoder: Decoder): EmojisType.TypeObject {
        return EmojisType.TypeObject(mapSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: EmojisType.TypeObject) {
        mapSerializer.serialize(encoder, value.emojis)
    }
}

class TypeArraySerializer : KSerializer<EmojisType.TypeArray> {
    private val listSerializer = ListSerializer(Emoji.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): EmojisType.TypeArray {
        return EmojisType.TypeArray(listSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: EmojisType.TypeArray) {
        return listSerializer.serialize(encoder, value.emojis)
    }
}

