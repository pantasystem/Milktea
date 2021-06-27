package jp.panta.misskeyandroidclient.util.date

import jp.panta.misskeyandroidclient.GsonFactory
import java.util.*

fun Date.toCurrentLocaleDate() : Date {
    val formatter = GsonFactory.createSimpleDateFormat()
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val formatted = formatter.format(this)
    return GsonFactory.createSimpleDateFormat().apply{
        timeZone = TimeZone.getDefault()
    }.parse(formatted)?: Date()
}