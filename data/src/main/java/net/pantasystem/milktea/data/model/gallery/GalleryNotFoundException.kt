package net.pantasystem.milktea.data.model.gallery

class GalleryNotFoundException(id: GalleryPost.Id) : NoSuchElementException("存在しません:$id")