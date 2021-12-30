package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta

class MetaMemCache {
    private val map = HashMap<String, Meta>()
    fun put(meta: Meta) {
        map[meta.uri] = meta
    }

    fun get(uri: String): Meta? {
        return map[uri]
    }
}