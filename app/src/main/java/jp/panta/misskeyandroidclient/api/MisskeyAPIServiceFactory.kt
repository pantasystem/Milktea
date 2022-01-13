package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.model.api.Version

interface MisskeyAPIServiceFactory {
    fun create(baseUrl: String): MisskeyAPI
    fun create(baseUrl: String, version: Version): MisskeyAPI
}