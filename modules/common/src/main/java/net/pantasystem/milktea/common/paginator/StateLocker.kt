package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.sync.Mutex

/**
 * 状態をロックすることのできることを表すインターフェース
 */
interface StateLocker {
    val mutex: Mutex
}