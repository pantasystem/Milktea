package net.pantasystem.milktea.app_store.handler

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.pantasystem.milktea.common_android.resource.StringSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * アプリ全体で共通としてユーザの操作によって発生したエラーをハンドリングするクラスです。
 * エラーレベルに応じて、UIに対して表示の仕分けを行います。
 * 基本的にはViewModelクラスから呼び出すことを前提とします。
 * 複数のレイヤーから呼び出してしまうと、重複して同じエラーを報告してしまう可能性が考えられるため、ViewModelから呼び出すことを前提としています。
 * また古い実装の場合このクラスを経由しないケースがあるため、このクラスに必ずエラーが報告されないケースもあるため、エラーの発生元を確認するようにしてください。
 */
@Singleton
class UserActionAppGlobalErrorStore @Inject constructor() {
    private val _errorFlow = MutableSharedFlow<AppGlobalError>(
        extraBufferCapacity = 999,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val errorFlow = _errorFlow.asSharedFlow()
    fun dispatch(e: AppGlobalError) {
        _errorFlow.tryEmit(e)
    }
}

/**
 * @param tag エラーの発生元を示すタグです。
 * @param level エラーのレベルです。
 * @param message エラーのメッセージです。
 * @param throwable エラーの発生元となった例外です。
 */
data class AppGlobalError(
    val tag: String,
    val level: ErrorLevel,
    val message: StringSource,
    val throwable: Throwable? = null,
) {
    enum class ErrorLevel {
        /**
         * ロギング程度の軽微なエラーレベルです。
         */
        Info,

        /**
         * ユーザーに対してToastやSnackBarを用いて警告を行うエラーレベルです。
         */
        Warning,

        /**
         * ユーザーに対してダイアログを用いて警告を行うエラーレベルです。
         */
        Error,
    }
}