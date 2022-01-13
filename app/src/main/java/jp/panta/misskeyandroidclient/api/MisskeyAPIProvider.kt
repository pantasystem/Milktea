package jp.panta.misskeyandroidclient.api


import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.util.blockingWithLockWithCheckTimeout
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MisskeyAPIとBaseURLとVersionをいい感じに管理する
 */
@Singleton
class MisskeyAPIProvider @Inject constructor(
    private val misskeyAPIServiceFactory: MisskeyAPIServiceFactory
){

    private val baseURLAndMisskeyAPI = mutableMapOf<String, MisskeyAPI>()
    private val baseURLAndVersion = mutableMapOf<String, Version?>()

    private val lock = Mutex()
    fun get(baseURL: String, version: Version? = null): MisskeyAPI {
        return lock.blockingWithLockWithCheckTimeout(timeMillis = 100) {
            var api = baseURLAndMisskeyAPI[baseURL]

            // NOTE BaseURLに対応するインスタンスが生成されていない＆＆鯖のバージョンに対応するインスタンスが生成されていなければ生成する
            if(api == null) {
                api = if(version == null) misskeyAPIServiceFactory.create(baseURL) else misskeyAPIServiceFactory.create(baseURL, version)
            }else if((baseURLAndVersion[baseURL] == null || baseURLAndVersion[baseURL] != version) && version != null) {
                api = misskeyAPIServiceFactory.create(baseURL, version)
            }
            baseURLAndMisskeyAPI[baseURL] = api
            return@blockingWithLockWithCheckTimeout api
        }
    }

    fun get(account: Account, version: Version? = null): MisskeyAPI {
        return get(account.instanceDomain, version)
    }
}