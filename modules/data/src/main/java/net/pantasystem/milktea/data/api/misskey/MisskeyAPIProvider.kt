package net.pantasystem.milktea.data.api.misskey


import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.Version
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MisskeyAPIとBaseURLとVersionをいい感じに管理する
 */
@Singleton
class MisskeyAPIProvider @Inject constructor(
    val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder
){


    private val baseURLAndMisskeyAPI = mutableMapOf<String, MisskeyAPI>()
    private val baseURLAndVersion = mutableMapOf<String, Version?>()

    fun get(baseURL: String, version: Version? = null): MisskeyAPI {
        synchronized(baseURLAndMisskeyAPI) {
            var api = baseURLAndMisskeyAPI[baseURL]

            // NOTE BaseURLに対応するインスタンスが生成されていない＆＆鯖のバージョンに対応するインスタンスが生成されていなければ生成する
            if(api == null) {
                api = if(version == null) misskeyAPIServiceBuilder.build(baseURL) else misskeyAPIServiceBuilder.build(
                    baseURL,
                    version
                )
            }else if((baseURLAndVersion[baseURL] == null || baseURLAndVersion[baseURL] != version) && version != null) {
                api = misskeyAPIServiceBuilder.build(baseURL, version)
            }
            baseURLAndMisskeyAPI[baseURL] = api
            return api
        }
    }

    fun get(account: Account, version: Version? = null): MisskeyAPI {
        return get(account.normalizedInstanceUri, version)
    }

    fun applyVersion(baseURL: String, version: Version) {
        get(baseURL, version)
    }
}