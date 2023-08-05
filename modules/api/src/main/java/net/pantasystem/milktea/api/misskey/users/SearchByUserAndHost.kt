package net.pantasystem.milktea.api.misskey.users

import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import retrofit2.Response

class SearchByUserAndHost(
    val misskeyAPI: MisskeyAPI,
    private val nodeInfoRepository: NodeInfoRepository,
    val account: Account,
) {


    suspend fun search(reqUser: RequestUser): Response<List<UserDTO>> {
        val requestUser = reqUser.copy(
            host = reqUser.host ?: ""
        )


        val type = nodeInfoRepository.find(account.getHost()).getOrThrow().type
        val isLegacyApi = type is NodeInfo.SoftwareType.Misskey.Meisskey
                || (type is NodeInfo.SoftwareType.Misskey.Normal
                && type.getVersion() < Version("12.0"))

        return if (isLegacyApi) {
            misskeyAPI.searchUser(
                requestUser.copy(
                    host = "",
                    userName = null,
                    query = requestUser.userName
                )
            ).throwIfHasError()
        } else {
            misskeyAPI.searchByUserNameAndHost(requestUser).throwIfHasError()
        }

    }


}