package net.pantasystem.milktea.data.api.misskey


import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.api.Version
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MisskeyAPIとBaseURLとVersionをいい感じに管理する
 */
@Singleton
class MisskeyAPIProvider @Inject constructor(){


    private val baseURLAndMisskeyAPI = mutableMapOf<String, MisskeyAPI>()
    private val baseURLAndVersion = mutableMapOf<String, Version?>()

    fun get(baseURL: String, version: Version? = null): MisskeyAPI {
        synchronized(baseURLAndMisskeyAPI) {
            var api = baseURLAndMisskeyAPI[baseURL]

            // NOTE BaseURLに対応するインスタンスが生成されていない＆＆鯖のバージョンに対応するインスタンスが生成されていなければ生成する
            if(api == null) {
                api = if(version == null) MisskeyAPIServiceBuilder.build(baseURL) else MisskeyAPIServiceBuilder.build(
                    baseURL,
                    version
                )
            }else if((baseURLAndVersion[baseURL] == null || baseURLAndVersion[baseURL] != version) && version != null) {
                api = MisskeyAPIServiceBuilder.build(baseURL, version)
            }
            baseURLAndMisskeyAPI[baseURL] = api
            return api
        }
    }

    fun get(account: Account, version: Version? = null): MisskeyAPI {
        return get(account.instanceDomain, version)
    }
}