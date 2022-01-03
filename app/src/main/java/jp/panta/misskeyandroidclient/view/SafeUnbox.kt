package jp.panta.misskeyandroidclient.view

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
    fun box(unboxed: Boolean): Boolean{
        return unboxed
    }

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: String?) : String{
        return boxed?: String()
    }

    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: Float?): Float{
        return boxed?: 0F
    }



    @JvmStatic
    @InverseMethod("box")
    fun unbox(boxed: Int?): Int{
        return boxed?: 0
    }


    @JvmStatic
    @InverseMethod("boxString")
    fun unboxString(boxed: String?) = boxed?: ""


    @JvmStatic
    @InverseMethod("boxInt")
    fun unboxInt(boxed: Int?) = boxed?: 0


    @JvmStatic
    @InverseMethod("boxBool")
    fun unboxBool(boxed: Boolean?) = boxed?: false

    @JvmStatic
    fun boxBool(unboxed: Boolean): Boolean = unboxed

}