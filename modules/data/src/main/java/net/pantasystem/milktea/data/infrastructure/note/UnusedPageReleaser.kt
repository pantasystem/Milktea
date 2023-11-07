package net.pantasystem.milktea.data.infrastructure.note

import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.StateLocker

internal suspend fun<T, V> releaseUnusedPage(pageableStore: T, position: Int, offset: Int, removeDiffCount: Int = 20) where T : PaginationState<V>, T : StateLocker {
    if (pageableStore.mutex.isLocked) {
        return
    }

    pageableStore.mutex.withLock {
        val state = pageableStore.getState()
        val newState = releaseUnusedPage(state, position, offset, removeDiffCount) ?: return@withLock

        pageableStore.setState(
            newState
        )
    }
}

internal fun<V> releaseUnusedPage(state: PageableState<List<V>>, position: Int, offset: Int, removeDiffCount: Int = 20): PageableState<List<V>>? {
    val items = when(val content = state.content) {
        is StateContent.Exist -> content.rawContent
        is StateContent.NotExist -> return null
    }

    // 末端の削除位置を求める
    var end = position + offset
    if (end >= items.size || end < 0) {
        end = items.size
    }
    var diffCount = items.size - end

    // 先頭の削除位置を求める
    var start = position - offset
    if (start < 0 || start >= items.size) {
        start = 0
    }

    diffCount += start

    // 削除件数が20件未満の時は削除しない
    if (diffCount < removeDiffCount) {
        return null
    }

    // 削除し状態に反映する
    return state.convert {
        items.subList(start, end)
    }
}