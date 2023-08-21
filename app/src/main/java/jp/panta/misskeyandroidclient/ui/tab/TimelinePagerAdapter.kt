@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient.ui.tab

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Page
import java.util.UUID


internal class TimelinePagerAdapter(
    fragmentManager: FragmentManager,
    private val pageableFragmentFactory: PageableFragmentFactory,
    list: List<Page>,
    loggerFactory: Logger.Factory
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val logger by lazy {
        loggerFactory.create("TimelinePagerAdapter")
    }

    companion object {
        const val FRAGMENT_TAG = "TimelinePagerAdapter.FRAGMENT_TAG"
    }

    private var requestBaseList: List<Page> = list
    private var oldRequestBaseSetting = requestBaseList

    private val fragmentIds = mutableSetOf<String>()

    override fun getCount(): Int {
        return requestBaseList.size
    }

    override fun getItem(position: Int): Fragment {
        val item = requestBaseList[position]
        val fragment = pageableFragmentFactory.create(item)
        val fragmentId = UUID.randomUUID().toString()
        when(val args = fragment.arguments) {
            null -> {
                val bundle = Bundle()
                bundle.putString(FRAGMENT_TAG, fragmentId)
                fragment.arguments = bundle
            }
            else -> {
                args.putString(FRAGMENT_TAG, fragmentId)
            }
        }
        fragmentIds.add(fragmentId)
//        mFragments.add(fragment)
        return fragment
    }


    override fun getPageTitle(position: Int): String {
        val page = requestBaseList[position]
        return page.title
    }


    override fun getItemPosition(any: Any): Int {
        val target = any as Fragment
        val fragmentId = target.arguments?.getString(FRAGMENT_TAG)
        if (fragmentId != null && fragmentIds.contains(fragmentId)) {
            return PagerAdapter.POSITION_UNCHANGED
        }

        return PagerAdapter.POSITION_NONE
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        logger.log("restoreState")
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            logger.error("restoreState error", e)
            e.printStackTrace()
        }
    }

    fun setList(list: List<Page>) {
        oldRequestBaseSetting = requestBaseList
        requestBaseList = list
        fragmentIds.clear()
        if (requestBaseList != oldRequestBaseSetting) {
            notifyDataSetChanged()
        }
    }

    fun onDestroy() {
        fragmentIds.clear()
    }
}