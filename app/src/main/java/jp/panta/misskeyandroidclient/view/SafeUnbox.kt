package jp.panta.misskeyandroidclient.view

import androidx.databinding.InverseMethod

object SafeUnbox {

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: Boolean?): Boolean {
        return boxed ?: false
    }

    @JvmStatic
    fun box(unboxed: Boolean): Boolean?{
        return unboxed
    }

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: String?) : String{
        return boxed?: String()
    }

    @JvmStatic
    fun box(unboxed: String): String?{
        return unboxed
    }

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: Float?): Float{
        return boxed?: 0F
    }

    @JvmStatic
    fun box(boxed: Float): Float?{
        return boxed
    }

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: Int?): Int{
        return boxed?: 0
    }

    @JvmStatic
    fun box(boxed: Int): Int?{
        return boxed
    }

}