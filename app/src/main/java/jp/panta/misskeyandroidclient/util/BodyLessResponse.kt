package jp.panta.misskeyandroidclient.util

import retrofit2.Response

class BodyLessResponse (private val response: Response<*>){
    fun message() = response.message()
    fun code() = response.code()
    val isSeccessful = response.isSuccessful
    fun errorBody() = response.errorBody()
    fun headers() = response.headers()
    fun raw() = response.raw()

}