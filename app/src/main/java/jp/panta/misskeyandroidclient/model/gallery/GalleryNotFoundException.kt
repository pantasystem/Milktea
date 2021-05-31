package jp.panta.misskeyandroidclient.model.gallery

class GalleryNotFoundException(id: GalleryPost.Id) : NoSuchElementException("存在しません:$id")