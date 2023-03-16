package net.pantasystem.milktea.data.infrastructure.report

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.users.report.ReportDTO
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.user.report.Report
import net.pantasystem.milktea.model.user.report.ReportRepository
import javax.inject.Inject

internal class ReportRepositoryImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
): ReportRepository {

    override suspend fun create(report: Report): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val account = getAccount.get(report.userId.accountId)
            val api = misskeyAPIProvider.get(account)
            val res = api.report(
                ReportDTO(
                    i = account.token,
                    comment = report.comment,
                    userId = report.userId.id
                )
            )
            res.throwIfHasError()
        }
    }
}