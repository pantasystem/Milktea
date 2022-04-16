package net.pantasystem.milktea.data.model.instance

interface FetchMeta {

    suspend fun fetch(instanceDomain: String, isForceFetch: Boolean = false) : Meta

}