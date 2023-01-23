package net.pantasystem.milktea.api.misskey.app

import kotlinx.serialization.Serializable

@Serializable
data class CreateApp(
    val i: String?,
    val name: String,
    val description: String,
    val callbackUrl: String,
    val permission: List<String>
)

//api/app/create
//api/my/apps

//api/app/show

/*
0: "write:user-groups"
1: "read:user-groups"
2: "read:page-likes"
3: "write:page-likes"
4: "write:pages"
5: "read:pages"
6: "write:votes"
7: "write:reactions"
8: "read:reactions"
9: "write:notifications"
10: "read:notifications"
11: "write:notes"
12: "write:mutes"
13: "read:mutes"
14: "read:account"
15: "write:account"
16: "read:blocks"
17: "write:blocks"
18: "read:drive"
19: "write:drive"
20: "read:favorites"
21: "write:favorites"
22: "read:following"
23: "write:following"
24: "read:messaging"
25: "write:messaging"
 */