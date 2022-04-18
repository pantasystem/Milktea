package net.pantasystem.milktea.common.serializations

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalSerializationApi
@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date>{
    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("DateSerializer", PrimitiveKind.STRING)


    override fun deserialize(decoder: Decoder): Date {
        return SimpleDateFormat(DATE_FORMAT, Locale.Builder().build()).parse(decoder.decodeString())
            ?: throw IllegalStateException("Dateでコード時に何らかの異常が発生しました")
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(SimpleDateFormat(DATE_FORMAT, Locale.Builder().build()).format(value))
    }


}