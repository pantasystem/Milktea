package net.pantasystem.milktea.api_streaming.mastodon

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

sealed interface Event {

    data class Update(val status: TootStatusDTO) : Event
    data class Delete(val id: String) : Event
    data class Notification(val tmp: String) : Event
}