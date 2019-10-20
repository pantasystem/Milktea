package jp.panta.misskeyandroidclient.model.streming

data class Body<T>(val id: String,
                val channel: String? = null,
                val type: String? = null,
                val body: T? = null,
                val params: Map<String, Any?>? = null)