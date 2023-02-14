package net.pantasystem.milktea.api.misskey

import retrofit2.Response
import retrofit2.http.GET


interface InstanceInfosAPI {

    @GET("instances.json")
    suspend fun getInstances(): Response<List<Any>>
}