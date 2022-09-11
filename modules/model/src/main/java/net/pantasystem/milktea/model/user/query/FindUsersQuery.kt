package net.pantasystem.milktea.model.user.query

data class FindUsersQuery(
    val origin: Origin?,
    val sort: OrderBy?,
    val state: State?
) {
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

fun FindUsersQuery.OrderBy.Companion.from(str: String): FindUsersQuery.OrderBy? {
    return when(str) {
        FindUsersQuery.Order.Follower.asc.str() -> FindUsersQuery.Order.Follower.asc
        FindUsersQuery.Order.Follower.desc.str() -> FindUsersQuery.Order.Follower.desc
        FindUsersQuery.Order.CreatedAt.asc.str() -> FindUsersQuery.Order.CreatedAt.asc
        FindUsersQuery.Order.CreatedAt.desc.str() -> FindUsersQuery.Order.CreatedAt.desc
        FindUsersQuery.Order.UpdatedAt.asc.str() -> FindUsersQuery.Order.UpdatedAt.asc
        FindUsersQuery.Order.UpdatedAt.desc.str() -> FindUsersQuery.Order.UpdatedAt.desc
        else -> null
    }
}
fun FindUsersQuery.State.Companion.from(str: String): FindUsersQuery.State? {

    return when(str) {
        FindUsersQuery.State.All.state -> FindUsersQuery.State.All
        FindUsersQuery.State.Admin.state -> FindUsersQuery.State.Admin
        FindUsersQuery.State.Moderator.state -> FindUsersQuery.State.Moderator
        FindUsersQuery.State.AdminOrModerator.state -> FindUsersQuery.State.AdminOrModerator
        FindUsersQuery.State.Alive.state -> FindUsersQuery.State.Alive
        else -> null
    }
}

fun FindUsersQuery.Origin.Companion.from(str: String): FindUsersQuery.Origin? {
    return when(str) {
        FindUsersQuery.Origin.Local.origin -> FindUsersQuery.Origin.Local
        FindUsersQuery.Origin.Combined.origin -> FindUsersQuery.Origin.Combined
        FindUsersQuery.Origin.Remote.origin -> FindUsersQuery.Origin.Remote
        else -> null
    }
}
val FindUsersQuery.Order.desc: FindUsersQuery.OrderBy
    get() {
        return when (this) {
            FindUsersQuery.Order.CreatedAt -> FindUsersQuery.OrderBy.CreatedAt(FindUsersQuery.OrderBy.By.Desc)
            FindUsersQuery.Order.Follower -> FindUsersQuery.OrderBy.Follower(FindUsersQuery.OrderBy.By.Desc)
            FindUsersQuery.Order.UpdatedAt -> FindUsersQuery.OrderBy.UpdatedAt(FindUsersQuery.OrderBy.By.Desc)
        }
    }

val FindUsersQuery.Order.asc: FindUsersQuery.OrderBy
    get() {
        return when (this) {
            FindUsersQuery.Order.CreatedAt -> FindUsersQuery.OrderBy.CreatedAt(FindUsersQuery.OrderBy.By.Asc)
            FindUsersQuery.Order.Follower -> FindUsersQuery.OrderBy.Follower(FindUsersQuery.OrderBy.By.Asc)
            FindUsersQuery.Order.UpdatedAt -> FindUsersQuery.OrderBy.UpdatedAt(FindUsersQuery.OrderBy.By.Asc)
        }
    }

fun FindUsersQuery.Companion.trendingUser(): FindUsersQuery {
    return FindUsersQuery(
        origin = FindUsersQuery.Origin.Local,
        sort = FindUsersQuery.Order.Follower.asc,
        state = FindUsersQuery.State.Alive
    )
}

fun FindUsersQuery.Companion.usersWithRecentActivity(): FindUsersQuery {
    return FindUsersQuery(
        origin = FindUsersQuery.Origin.Local,
        sort = FindUsersQuery.Order.UpdatedAt.asc,
        state = null,
    )
}

fun FindUsersQuery.Companion.newlyJoinedUsers(): FindUsersQuery {
    return FindUsersQuery(
        origin = FindUsersQuery.Origin.Local,
        sort = FindUsersQuery.Order.CreatedAt.asc,
        state = FindUsersQuery.State.Alive
    )
}

fun FindUsersQuery.Companion.remoteTrendingUser(): FindUsersQuery {
    return FindUsersQuery(
        origin = FindUsersQuery.Origin.Remote,
        sort = FindUsersQuery.Order.Follower.asc,
        state = FindUsersQuery.State.Alive
    )
}

fun FindUsersQuery.Companion.remoteUsersWithRecentActivity(): FindUsersQuery {
    return FindUsersQuery(
        origin = FindUsersQuery.Origin.Combined,
        sort = FindUsersQuery.Order.UpdatedAt.asc,
        state = FindUsersQuery.State.Alive
    )
}

fun FindUsersQuery.Companion.newlyDiscoveredUsers(): FindUsersQuery {
    return FindUsersQuery.from(FindUsersQuery.Origin.Combined)
        .whereState(FindUsersQuery.State.All)
        .orderBy(FindUsersQuery.Order.CreatedAt.asc)
}


infix fun FindUsersQuery.orderBy(order: FindUsersQuery.OrderBy): FindUsersQuery {
    return this.copy(sort = order)
}

fun FindUsersQuery.Companion.from(origin: FindUsersQuery.Origin): FindUsersQuery {
    return FindUsersQuery(origin = origin, null, null)
}

infix fun FindUsersQuery.whereState(state: FindUsersQuery.State): FindUsersQuery {
    return this.copy(state = state)
}
