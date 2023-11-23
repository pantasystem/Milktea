package net.pantasystem.milktea.common.serializations

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class FallbackDefaultValueSerializer<T>(private val serializer: KSerializer<T>, private val defaultValue: T) :
    KSerializer<T> {

    // Alternative to taking values in param, take clazz: Class<T>
    // - private val values = clazz.enumConstants
    override val descriptor: SerialDescriptor = serializer.descriptor


    override fun serialize(encoder: Encoder, value: T) {
        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): T {
        return try {
            serializer.deserialize(decoder)
        } catch (e: Exception) {
            defaultValue
        }
    }
}
