package jp.panta.misskeyandroidclient.util

import android.graphics.Color
import android.util.Log
import java.lang.IllegalArgumentException

object ColorUtil{


    fun matchOpaqueAndColor(opaque: Int, color: Int): Int{
        if(opaque in 0..255){

            val o = opaque.shl(8 * 3)
            val mask = 0xffffff
            val masked = color and mask
            //14000000
            //println(Integer.toHexString(o))
            val matched =  (o.toLong() or masked.toLong()).toInt()
            Log.d("ColorUtil", "matched:${Integer.toHexString(matched)}")
            return matched
        }else{
            throw IllegalArgumentException("opacity is 0..255")
        }
    }
}