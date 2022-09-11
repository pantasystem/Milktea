package net.pantasystem.milktea.model.url

interface UrlPreviewStore{

    fun get(url: String): UrlPreview?
}