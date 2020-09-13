package jp.panta.misskeyandroidclient.model.account.page

import androidx.room.*
import java.io.Serializable

@Entity(
    tableName = "page_table",
    indices = [Index("weight"), Index("accountId")]
)
data class Page(
    val accountId: Long,
    val title: String,
    var weight: Int,
    @Embedded val pageParams: PageParams
) : Serializable {

    constructor(accountId: Long, title: String, weight: Int, pageable: Pageable)
            : this(accountId, title, weight, pageable.toParams())


    @PrimaryKey(autoGenerate = true) var pageId: Long = -1


    @Ignore
    fun pageable(): Pageable{
        return pageParams.toPageable()
    }
}