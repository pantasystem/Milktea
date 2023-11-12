package net.pantasystem.milktea.note.timeline.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.NoteStreaming

internal class NoteStreamingCollector(
    val coroutineScope: CoroutineScope,
    val timelineStore: TimelineStore,
    val accountStore: AccountStore,
    val noteStreaming: NoteStreaming,
    val logger: Logger,
    val pageable: Pageable,
    val currentAccountWatcher: CurrentAccountWatcher,
) {

    private var job: Job? = null

    fun suspendStreaming() {
        synchronized(this) {
            job?.cancel()
            job = null
        }
    }

    fun resumeStreaming() {
        startObserveStreaming()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startObserveStreaming() {
        synchronized(this) {
            if (job != null) {
                return
            }
            job = accountStore.observeCurrentAccount.filterNotNull().distinctUntilChanged()
                .flatMapLatest {
                    noteStreaming.connect(currentAccountWatcher::getAccount, pageable)
                }.map {
                    timelineStore.onReceiveNote(it.id)
                }.catch {
                    logger.error("receive not error", it)
                }.launchIn(coroutineScope + Dispatchers.IO)
        }
    }
}