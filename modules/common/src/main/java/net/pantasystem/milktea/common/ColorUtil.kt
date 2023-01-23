package net.pantasystem.milktea.common

import android.util.Log
import java.lang.IllegalArgumentException

object ColorUtil{

    val hexMap = mapOf(
        '0' to 0,
        '1' to 1,
        '2' to 2,
        '3' to 3,
        '4' to 4,
        '5' to 5,
        '6' to 6,
        '7' to 7,
        '8' to 8,
        '9' to 9,
        'A' to 10,
        'B' to 11,
        'C' to 12,
        'D' to 13,
        'E' to 14,
        'F' to 15,
        'a' to 10,
        'b' to 11,
        'c' to 12,
        'd' to 13,
        'e' to 14,
        'f' to 15,
    )

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


    /**
     * 任意の文字列から任意の色を取得するためのメソッド　
     * @param str 色を取得するソースとなる文字列
     */
    fun getRGBFromString(str: String): Triple<Int, Int, Int> {
        val hash = Hash.sha256(str)
        val rBase = hash.substring(0 until 10)
        val gBase = hash.substring(10 until 21)
        val bBase = hash.substring(22 until 32)
        val r = generateColorElement(rBase)
        val g = generateColorElement(gBase)
        val b = generateColorElement(bBase)

        return Triple(r, g, b)
    }

    private fun generateColorElement(str: String): Int {
        val map = mapOf(
            '0' to 0,
            '1' to 1,
            '2' to 2,
            '3' to 3,
            '4' to 4,
            '5' to 5,
            '6' to 6,
            '7' to 7,
            '8' to 8,
            '9' to 9,
            'A' to 10,
            'B' to 11,
            'C' to 12,
            'D' to 13,
            'E' to 14,
            'F' to 15
        )
        return str.map {
            map[it]!!
        }.sum() % 255
    }
}

fun String.getRGB(): Triple<Int, Int, Int> {
    return ColorUtil.getRGBFromString(this)
}