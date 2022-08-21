package net.pantasystem.milktea.common_android.ui

import androidx.fragment.app.Fragment
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable

interface PageableFragmentFactory {
    fun create(page: Page): Fragment
    fun create(pageable: Pageable): Fragment
}
