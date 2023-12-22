package net.pantasystem.milktea.api.milktea.instance.ticker

import net.pantasystem.milktea.api.misskey.infos.SimpleInstanceInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface InstanceTickerAPIService {

    @GET("/instances")
    suspend fun getInstanceInfo(@Query("host") host: String): Response<InstanceTickerNetworkDTO>

    @GET("instances-search")
    suspend fun getInstances(
        @Query("query") name: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): Response<List<SimpleInstanceInfo>>
}