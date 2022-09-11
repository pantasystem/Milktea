package net.pantasystem.milktea.common_android.ui

import androidx.databinding.InverseMethod

/**
 * オーバーロードがわからないバカAndroidStudioのDataBindingのせいで名前を変更した関数を追加する
 */
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
    @androidx.databinding.InverseMethod("box")
    fun unbox(boxed: Float?): Float{
        return boxed?: 0F
    }

    @JvmStatic
    fun box(boxed: Float): Float?{
        return boxed
    }

    @JvmStatic
    @androidx.databinding.InverseMethod("box")
    fun unbox(boxed: Int?): Int{
        return boxed?: 0
    }

    @JvmStatic
    fun box(boxed: Int): Int?{
        return boxed
    }

    @JvmStatic
    @androidx.databinding.InverseMethod("boxString")
    fun unboxString(boxed: String?) = boxed?: ""

    @JvmStatic
    fun boxString(unboxed: String): String? = unboxed

    @JvmStatic
    @androidx.databinding.InverseMethod("boxInt")
    fun unboxInt(boxed: Int?) = boxed?: 0

    @JvmStatic
    fun boxInt(unboxed: Int): Int? = unboxed

    @JvmStatic
    @androidx.databinding.InverseMethod("boxBool")
    fun unboxBool(boxed: Boolean?) = boxed?: false

    @JvmStatic
    fun boxBool(unboxed: Boolean): Boolean? = unboxed

}