package jp.panta.misskeyandroidclient.util

import android.graphics.Color
import java.lang.IllegalArgumentException

object ColorUtil{

    fun matchOpaqueAndColor(opacity: Int, color: String): Int{
        val parsedColor = Color.parseColor(color)
        return matchOpaqueAndColor(opacity, parsedColor)
    }

    fun matchOpaqueAndColor(opaque: Int, color: Int): Int{
        if(opaque in 0..255){
            return opaque.shl(8 * 3) or color
        }else{
            throw IllegalArgumentException("opacity is 0..255")
        }
    }
}