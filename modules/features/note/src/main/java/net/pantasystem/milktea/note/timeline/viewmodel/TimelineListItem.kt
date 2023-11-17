package net.pantasystem.milktea.note.timeline.viewmodel

import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import java.io.IOException
import java.net.SocketTimeoutException


sealed interface TimelineListItem {
    data object Loading : TimelineListItem
    data class Note(val note: PlaneNoteViewData) : TimelineListItem
    data class Error(val throwable: Throwable) : TimelineListItem {
        fun getErrorMessage(): StringSource {
            return when (throwable) {
                is SocketTimeoutException -> {
                    StringSource(R.string.timeout_error)
                }
                is IOException -> {
                    StringSource(R.string.timeout_error)
                }
                is APIError -> {
                    APIErrorStringConverter()(throwable)
                }
                is UnauthorizedException -> {
                    StringSource(R.string.unauthorized_error)
                }
                else -> {
                    StringSource("error:$throwable")
                }
            }
        }

        fun isUnauthorizedError(): Boolean {
            return throwable is APIError.AuthenticationException
                    || throwable is APIError.ForbiddenException
                    || throwable is UnauthorizedException
        }
    }

    data object Empty : TimelineListItem
}

internal fun PageableState<List<PlaneNoteViewData>>.toList(): List<TimelineListItem> {
    return when (val content = this.content) {
        is StateContent.Exist -> {
            content.rawContent.map {
                TimelineListItem.Note(it)
            } + if (this is PageableState.Loading.Previous) {
                listOf(TimelineListItem.Loading)
            } else {
                emptyList()
            }
        }
        is StateContent.NotExist -> {
            listOf(
                when (this) {
                    is PageableState.Error -> {
                        TimelineListItem.Error(this.throwable)
                    }
                    is PageableState.Fixed -> {
                        TimelineListItem.Empty
                    }
                    is PageableState.Loading -> TimelineListItem.Loading
                }
            )
        }
    }
}
