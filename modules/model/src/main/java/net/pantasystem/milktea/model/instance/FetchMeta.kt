package net.pantasystem.milktea.model.instance

interface FetchMeta {

    suspend fun fetch(instanceDomain: String, isForceFetch: Boolean = false) : Meta

}