package net.pantasystem.milktea.common.paginator


/**
 * DTOをEntityに変換し共通のDataStoreにEntityを追加するためのInterface
 */
interface EntityConverter<DTO, E> {
    suspend fun convertAll(list: List<DTO>) : List<E>
}
