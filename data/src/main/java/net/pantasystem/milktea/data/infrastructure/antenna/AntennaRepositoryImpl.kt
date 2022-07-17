package net.pantasystem.milktea.data.infrastructure.antenna

import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaQuery
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaToAdd
import net.pantasystem.milktea.api.misskey.v12.antenna.from
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaRepository
import net.pantasystem.milktea.model.antenna.SaveAntennaParam
import javax.inject.Inject

class AntennaRepositoryImpl @Inject constructor(
    val getAccount: GetAccount,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption
) : AntennaRepository {

    override suspend fun findByAccountId(accountId: Long): Result<List<Antenna>> = runCatching {
        val account = getAccount.get(accountId)
        val body = (misskeyAPIProvider.get(account) as MisskeyAPIV12).getAntennas(
            AntennaQuery(
                i = account.getI(encryption),
                limit = null,
                antennaId = null,
            )
        ).throwIfHasError().body()
        body?.map {
            it.toEntity(account)
        } ?: emptyList()
    }

    override suspend fun delete(antennaId: Antenna.Id): Result<Unit> = runCatching {
        val account = getAccount.get(antennaId.accountId)
        (misskeyAPIProvider.get(account) as MisskeyAPIV12).deleteAntenna(
            AntennaQuery(
                antennaId = antennaId.antennaId,
                i = account.getI(encryption),
                limit = null
            )
        ).throwIfHasError()
    }

    override suspend fun create(accountId: Long, params: SaveAntennaParam): Result<Antenna> = runCatching {
        val account = getAccount.get(accountId)
        val request = AntennaToAdd.from(account.getI(encryption), params,)
        (misskeyAPIProvider.get(account) as MisskeyAPIV12).createAntenna(request)
            .throwIfHasError()
            .body()
            ?.toEntity(account)!!
    }

    override suspend fun update(antennaId: Antenna.Id, params: SaveAntennaParam): Result<Antenna>  = runCatching {
        val account = getAccount.get(antennaId.accountId)
        val request = AntennaToAdd.from(account.getI(encryption), params, antennaId.antennaId)
        (misskeyAPIProvider.get(account) as MisskeyAPIV12).updateAntenna(request)
            .throwIfHasError()
            .body()
            ?.toEntity(account)!!
    }
}