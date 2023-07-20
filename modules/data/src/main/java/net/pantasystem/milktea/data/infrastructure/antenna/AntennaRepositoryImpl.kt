package net.pantasystem.milktea.data.infrastructure.antenna

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaQuery
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaToAdd
import net.pantasystem.milktea.api.misskey.v12.antenna.from
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaRepository
import net.pantasystem.milktea.model.antenna.SaveAntennaParam
import javax.inject.Inject

class AntennaRepositoryImpl @Inject constructor(
    val getAccount: GetAccount,
    val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : AntennaRepository {

    override suspend fun findByAccountId(accountId: Long): Result<List<Antenna>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(accountId)
            val body = (misskeyAPIProvider.get(account)).getAntennas(
                AntennaQuery(
                    i = account.token,
                    limit = null,
                    antennaId = null,
                )
            ).throwIfHasError().body()
            body?.map {
                it.toEntity(account)
            } ?: emptyList()
        }

    }

    override suspend fun delete(antennaId: Antenna.Id): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(antennaId.accountId)
            (misskeyAPIProvider.get(account)).deleteAntenna(
                AntennaQuery(
                    antennaId = antennaId.antennaId,
                    i = account.token,
                    limit = null
                )
            ).throwIfHasError()
        }
    }

    override suspend fun create(accountId: Long, params: SaveAntennaParam): Result<Antenna> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(accountId)
            val request = AntennaToAdd.from(account.token, params,)
            (misskeyAPIProvider.get(account)).createAntenna(request)
                .throwIfHasError()
                .body()
                ?.toEntity(account)!!
        }
    }

    override suspend fun update(antennaId: Antenna.Id, params: SaveAntennaParam): Result<Antenna>  = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(antennaId.accountId)
            val request = AntennaToAdd.from(account.token, params, antennaId.antennaId)
            (misskeyAPIProvider.get(account)).updateAntenna(request)
                .throwIfHasError()
                .body()
                ?.toEntity(account)!!
        }
    }

    override suspend fun find(antennaId: Antenna.Id): Result<Antenna> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(antennaId.accountId)
            val api = misskeyAPIProvider.get(account)

            val res = api.showAntenna(
                AntennaQuery(
                    i = account.token, antennaId = antennaId.antennaId, limit = null
                )
            )
            res.throwIfHasError()
            res.body()?.toEntity(account)!!
        }
    }
}