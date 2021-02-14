package jp.panta.misskeyandroidclient.serializations

import kotlinx.serialization.*
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date>{
    const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")


    @ImplicitReflectionSerializer
    override fun deserialize(decoder: Decoder): Date {
        return SimpleDateFormat(DATE_FORMAT, Locale.Builder().build()).parse(decoder.decode())
            ?: throw IllegalStateException("Dateでコード時に何らかの異常が発生しました")
    }

    @ImplicitReflectionSerializer
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encode(SimpleDateFormat(DATE_FORMAT, Locale.Builder().build()).format(value))
    }

}