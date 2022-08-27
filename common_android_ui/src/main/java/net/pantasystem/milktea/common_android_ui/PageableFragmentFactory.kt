package net.pantasystem.milktea.common_android_ui

import androidx.fragment.app.Fragment
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable

interface PageableFragmentFactory {
    fun create(page: Page, initialStartAt: Instant? = null): Fragment
    fun create(pageable: Pageable, initialStartAt: Instant? = null): Fragment
}
