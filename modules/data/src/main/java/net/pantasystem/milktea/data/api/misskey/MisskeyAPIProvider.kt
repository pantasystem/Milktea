package net.pantasystem.milktea.data.api.misskey


import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.model.account.Account
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

    fun get(baseURL: String): MisskeyAPI {
        synchronized(baseURLAndMisskeyAPI) {
            var api = baseURLAndMisskeyAPI[baseURL]

            // NOTE BaseURLに対応するインスタンスが生成されていない＆＆鯖のバージョンに対応するインスタンスが生成されていなければ生成する
            if(api == null) {
                api = misskeyAPIServiceBuilder.build(baseURL)
            }
            baseURLAndMisskeyAPI[baseURL] = api
            return api
        }
    }

    fun get(account: Account): MisskeyAPI {
        return get(account.normalizedInstanceUri)
    }

}