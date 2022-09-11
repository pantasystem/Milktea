package net.pantasystem.milktea.model.gallery

class GalleryNotFoundException(id: GalleryPost.Id) : NoSuchElementException("存在しません:$id")