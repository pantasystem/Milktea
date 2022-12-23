package net.pantasystem.milktea.api.milktea

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MilkteaAPIService {

    @GET("/api/instances")
    suspend fun getInstances(): Response<List<InstanceInfoResponse>>

    @POST("/api/instances")
    suspend fun createInstance(@Body body: CreateInstanceRequest): Response<Unit>
}