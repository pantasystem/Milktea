package net.pantasystem.milktea.api.milktea.instance.ticker

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface InstanceTickerAPIService {

    @GET("/instances")
    suspend fun getInstanceInfo(@Query("host") host: String): Response<InstanceTickerNetworkDTO>
}