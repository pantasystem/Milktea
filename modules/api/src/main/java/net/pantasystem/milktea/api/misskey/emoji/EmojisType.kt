package net.pantasystem.milktea.api.misskey.emoji

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import net.pantasystem.milktea.model.emoji.Emoji


@kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class)
sealed interface EmojisType {
    @kotlinx.serialization.Serializable(with = TypeArraySerializer::class)
    data class TypeArray(val emojis: List<Emoji>) : EmojisType

    @kotlinx.serialization.Serializable(with = TypeObjectSerializer::class)
    data class TypeObject(val emojis: Map<String, TypeObjectValueType>) : EmojisType

    @kotlinx.serialization.Serializable
    object None : EmojisType
}

@kotlinx.serialization.Serializable(with = TypeObjectValueTypeSerializer::class)
sealed interface TypeObjectValueType {

    @kotlinx.serialization.Serializable(with = TypeObjectValuesValueTypeSerializer::class)
    data class Value(val value: String) : TypeObjectValueType

    @kotlinx.serialization.Serializable(with = TypeObjectValuesObjectTypeSerializer::class)
    data class Obj(val emoji: Emoji) : TypeObjectValueType
}

@kotlinx.serialization.Serializable
data class TestNoteObject(
    @kotlinx.serialization.Serializable(with = CustomEmojisTypeSerializer::class) val emojis: EmojisType
)

class TypeObjectValueTypeSerializer : JsonContentPolymorphicSerializer<TypeObjectValueType>(TypeObjectValueType::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out TypeObjectValueType> {
        if (element is JsonObject) {
            return TypeObjectValuesObjectTypeSerializer()
        }

        return TypeObjectValuesValueTypeSerializer()
    }
}

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
    private val mapSerializer = MapSerializer(String.serializer(), TypeObjectValueType.serializer())
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

class TypeObjectValuesValueTypeSerializer : KSerializer<TypeObjectValueType.Value> {
    private  val strSerializer = JsonPrimitive.serializer()
    override val descriptor: SerialDescriptor = strSerializer.descriptor

    override fun deserialize(decoder: Decoder): TypeObjectValueType.Value {
        return TypeObjectValueType.Value(strSerializer.deserialize(decoder).content)
    }

    override fun serialize(encoder: Encoder, value: TypeObjectValueType.Value) {
        return strSerializer.serialize(encoder, JsonPrimitive(value.value))
    }

}

class TypeObjectValuesObjectTypeSerializer : KSerializer<TypeObjectValueType.Obj> {
    private val emojiSerializer = Emoji.serializer()
    override val descriptor: SerialDescriptor = emojiSerializer.descriptor

    override fun deserialize(decoder: Decoder): TypeObjectValueType.Obj {
        return TypeObjectValueType.Obj(emojiSerializer.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: TypeObjectValueType.Obj) {
        return emojiSerializer.serialize(encoder, value.emoji)
    }
}
