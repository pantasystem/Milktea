package net.pantasystem.milktea.model.user.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
){
    operator fun invoke(state: ReportState): Flow<ReportState> {
        return flow {
            when (state) {
                is ReportState.None -> {
                    return@flow
                }
                is ReportState.Sending -> {
                    return@flow
                }
                is ReportState.Specify -> {
                    emit(ReportState.Sending.Doing(state.userId, state.comment))

                    withContext(Dispatchers.Main) {
                    }
                    val r = Report(
                        state.userId,
                        state.comment
                    )
                    runCancellableCatching {
                        reportRepository.create(
                            r
                        )
                    }.onSuccess {
                        emit(ReportState.Sending.Success(state.userId, state.comment))
                    }.onFailure {
                        emit(ReportState.Sending.Failed(state.userId, state.comment))
                    }
                }
            }
        }

    }
}