package net.pantasystem.milktea.model.user.query

sealed interface FindUsersQuery

sealed interface FindUsersQuery4Mastodon : FindUsersQuery {
    data class SuggestUsers(val limit: Int? = null) : FindUsersQuery4Mastodon
}

data class FindUsersQuery4Misskey(
    val origin: Origin?,
    val sort: OrderBy?,
    val state: State?,
) : FindUsersQuery {
    companion object;

    sealed interface Order {

        object Follower : Order
        object CreatedAt : Order
        object UpdatedAt : Order
    }

    sealed interface OrderBy {
        sealed interface By {
            object Desc : By
            object Asc : By

        }

        val type: By

        data class Follower(override val type: By) : OrderBy
        data class CreatedAt(override val type: By) : OrderBy
        data class UpdatedAt(override val type: By) : OrderBy
        companion object

        fun str(): String {
            val order = when (this) {
                is CreatedAt -> "createdAt"
                is Follower -> "follower"
                is UpdatedAt -> "updatedAt"
            }
            val sort = when (type) {
                By.Asc -> "+"
                By.Desc -> "-"
            }
            return "$sort$order"
        }


    }

    sealed class State(val state: String) {
        object All : State("all")
        object Admin : State("admin")
        object Moderator : State("moderator")
        object AdminOrModerator : State("adminOrModerator")
        object Alive : State("alive")
        companion object

    }

    sealed class Origin(val origin: String) {
        object Local : Origin("local")
        object Combined : Origin("combined")
        object Remote : Origin("remote")
        companion object

    }

}

fun FindUsersQuery4Misskey.OrderBy.Companion.from(str: String): FindUsersQuery4Misskey.OrderBy? {
    return when (str) {
        FindUsersQuery4Misskey.Order.Follower.asc.str() -> FindUsersQuery4Misskey.Order.Follower.asc
        FindUsersQuery4Misskey.Order.Follower.desc.str() -> FindUsersQuery4Misskey.Order.Follower.desc
        FindUsersQuery4Misskey.Order.CreatedAt.asc.str() -> FindUsersQuery4Misskey.Order.CreatedAt.asc
        FindUsersQuery4Misskey.Order.CreatedAt.desc.str() -> FindUsersQuery4Misskey.Order.CreatedAt.desc
        FindUsersQuery4Misskey.Order.UpdatedAt.asc.str() -> FindUsersQuery4Misskey.Order.UpdatedAt.asc
        FindUsersQuery4Misskey.Order.UpdatedAt.desc.str() -> FindUsersQuery4Misskey.Order.UpdatedAt.desc
        else -> null
    }
}

fun FindUsersQuery4Misskey.State.Companion.from(str: String): FindUsersQuery4Misskey.State? {

    return when (str) {
        FindUsersQuery4Misskey.State.All.state -> FindUsersQuery4Misskey.State.All
        FindUsersQuery4Misskey.State.Admin.state -> FindUsersQuery4Misskey.State.Admin
        FindUsersQuery4Misskey.State.Moderator.state -> FindUsersQuery4Misskey.State.Moderator
        FindUsersQuery4Misskey.State.AdminOrModerator.state -> FindUsersQuery4Misskey.State.AdminOrModerator
        FindUsersQuery4Misskey.State.Alive.state -> FindUsersQuery4Misskey.State.Alive
        else -> null
    }
}

fun FindUsersQuery4Misskey.Origin.Companion.from(str: String): FindUsersQuery4Misskey.Origin? {
    return when (str) {
        FindUsersQuery4Misskey.Origin.Local.origin -> FindUsersQuery4Misskey.Origin.Local
        FindUsersQuery4Misskey.Origin.Combined.origin -> FindUsersQuery4Misskey.Origin.Combined
        FindUsersQuery4Misskey.Origin.Remote.origin -> FindUsersQuery4Misskey.Origin.Remote
        else -> null
    }
}

val FindUsersQuery4Misskey.Order.desc: FindUsersQuery4Misskey.OrderBy
    get() {
        return when (this) {
            FindUsersQuery4Misskey.Order.CreatedAt -> FindUsersQuery4Misskey.OrderBy.CreatedAt(
                FindUsersQuery4Misskey.OrderBy.By.Desc
            )
            FindUsersQuery4Misskey.Order.Follower -> FindUsersQuery4Misskey.OrderBy.Follower(
                FindUsersQuery4Misskey.OrderBy.By.Desc
            )
            FindUsersQuery4Misskey.Order.UpdatedAt -> FindUsersQuery4Misskey.OrderBy.UpdatedAt(
                FindUsersQuery4Misskey.OrderBy.By.Desc
            )
        }
    }

val FindUsersQuery4Misskey.Order.asc: FindUsersQuery4Misskey.OrderBy
    get() {
        return when (this) {
            FindUsersQuery4Misskey.Order.CreatedAt -> FindUsersQuery4Misskey.OrderBy.CreatedAt(
                FindUsersQuery4Misskey.OrderBy.By.Asc
            )
            FindUsersQuery4Misskey.Order.Follower -> FindUsersQuery4Misskey.OrderBy.Follower(
                FindUsersQuery4Misskey.OrderBy.By.Asc
            )
            FindUsersQuery4Misskey.Order.UpdatedAt -> FindUsersQuery4Misskey.OrderBy.UpdatedAt(
                FindUsersQuery4Misskey.OrderBy.By.Asc
            )
        }
    }

fun FindUsersQuery4Misskey.Companion.trendingUser(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(
        origin = FindUsersQuery4Misskey.Origin.Local,
        sort = FindUsersQuery4Misskey.Order.Follower.asc,
        state = FindUsersQuery4Misskey.State.Alive
    )
}

fun FindUsersQuery4Misskey.Companion.usersWithRecentActivity(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(
        origin = FindUsersQuery4Misskey.Origin.Local,
        sort = FindUsersQuery4Misskey.Order.UpdatedAt.asc,
        state = null,
    )
}

fun FindUsersQuery4Misskey.Companion.newlyJoinedUsers(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(
        origin = FindUsersQuery4Misskey.Origin.Local,
        sort = FindUsersQuery4Misskey.Order.CreatedAt.asc,
        state = FindUsersQuery4Misskey.State.Alive
    )
}

fun FindUsersQuery4Misskey.Companion.remoteTrendingUser(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(
        origin = FindUsersQuery4Misskey.Origin.Remote,
        sort = FindUsersQuery4Misskey.Order.Follower.asc,
        state = FindUsersQuery4Misskey.State.Alive
    )
}

fun FindUsersQuery4Misskey.Companion.remoteUsersWithRecentActivity(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(
        origin = FindUsersQuery4Misskey.Origin.Combined,
        sort = FindUsersQuery4Misskey.Order.UpdatedAt.asc,
        state = FindUsersQuery4Misskey.State.Alive
    )
}

fun FindUsersQuery4Misskey.Companion.newlyDiscoveredUsers(): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey.from(FindUsersQuery4Misskey.Origin.Combined)
        .whereState(FindUsersQuery4Misskey.State.All)
        .orderBy(FindUsersQuery4Misskey.Order.CreatedAt.asc)
}


infix fun FindUsersQuery4Misskey.orderBy(order: FindUsersQuery4Misskey.OrderBy): FindUsersQuery4Misskey {
    return this.copy(sort = order)
}

fun FindUsersQuery4Misskey.Companion.from(origin: FindUsersQuery4Misskey.Origin): FindUsersQuery4Misskey {
    return FindUsersQuery4Misskey(origin = origin, null, null)
}

infix fun FindUsersQuery4Misskey.whereState(state: FindUsersQuery4Misskey.State): FindUsersQuery4Misskey {
    return this.copy(state = state)
}
