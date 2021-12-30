package jp.panta.misskeyandroidclient.api


import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.Version

/**
 * MisskeyAPIとBaseURLとVersionをいい感じに管理する
 */
class MisskeyAPIProvider {


    private val baseURLAndMisskeyAPI = mutableMapOf<String, MisskeyAPI>()
    private val baseURLAndVersion = mutableMapOf<String, Version?>()

    fun get(baseURL: String, version: Version? = null): MisskeyAPI {
        synchronized(baseURLAndMisskeyAPI) {
            var api = baseURLAndMisskeyAPI[baseURL]

            // NOTE BaseURLに対応するインスタンスが生成されていない＆＆鯖のバージョンに対応するインスタンスが生成されていなければ生成する
            if(api == null) {
                api = if(version == null) MisskeyAPIServiceBuilder.build(baseURL) else MisskeyAPIServiceBuilder.build(baseURL, version)
            }else if((baseURLAndVersion[baseURL] == null || baseURLAndVersion[baseURL] != version) && version != null) {
                api = MisskeyAPIServiceBuilder.build(baseURL, version)
            }
            baseURLAndMisskeyAPI[baseURL] = api
            return api
        }
    }

    fun get(account: Account): MisskeyAPI {
        return get(account.instanceDomain)
    }
}