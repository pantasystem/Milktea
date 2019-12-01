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

}