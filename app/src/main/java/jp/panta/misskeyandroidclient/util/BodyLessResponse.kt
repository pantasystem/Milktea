package jp.panta.misskeyandroidclient.util

import retrofit2.Response

class BodyLessResponse (private val response: Response<*>?){
    fun message() = response?.message()?: "app network error"
    fun code() = response?.code()?: 404
    fun headers() = response?.headers()
    fun raw() = response?.raw()

}