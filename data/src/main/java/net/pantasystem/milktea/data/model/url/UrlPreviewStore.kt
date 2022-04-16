package net.pantasystem.milktea.data.model.url

interface UrlPreviewStore{

    fun get(url: String): UrlPreview?
}