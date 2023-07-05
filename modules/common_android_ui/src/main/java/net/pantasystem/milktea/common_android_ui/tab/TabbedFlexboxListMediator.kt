package net.pantasystem.milktea.common_android_ui.tab


import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

/**
 * This class is made to provide the ability to sync between RecyclerView's specific items with
 * TabLayout tabs.
 *
 * @param mRecyclerView     The RecyclerView that is going to be synced with the TabLayout
 * @param mTabLayout        The TabLayout that is going to be synced with the RecyclerView specific
 *                          items.
 * @param mIndices          The indices of the RecyclerView's items that is going to be playing a
 *                          role of "check points" for the syncing operation.
 * @param mIsSmoothScroll   Defines the ability of smooth scroll when clicking the tabs of the
 *                          TabLayout.
 */
class TabbedFlexboxListMediator(
    private val mRecyclerView: RecyclerView,
    private val mTabLayout: TabLayout,
    private var mIndices: List<Int>,
    private var mIsSmoothScroll: Boolean = false
) {

    private var mIsAttached = false

    private var mRecyclerState = RecyclerView.SCROLL_STATE_IDLE
    private var mTabClickFlag = false

    private val smoothScroller: SmoothScroller =
        object : LinearSmoothScroller(mRecyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

    private var tabViewCompositeClickListener: TabViewCompositeClickListener =
        TabViewCompositeClickListener(mTabLayout)

    /**
     * Calling this method will ensure that the data that has been provided to the mediator is
     * valid for use, and start syncing between the the RecyclerView and the TabLayout.
     *
     * Call this method when you have:
     *      1- provided a RecyclerView Adapter,
     *      2- provided a TabLayout with the appropriate number of tabs,
     *      3- provided indices of the recyclerview items that you are syncing the tabs with. (You
     *         need to be providing indices of at most the number of Tabs inflated in the TabLayout.)
     */
    fun attach() {
        mRecyclerView.adapter
            ?: throw RuntimeException("Cannot attach with no Adapter provided to RecyclerView")

        if (mTabLayout.tabCount == 0)
            throw RuntimeException("Cannot attach with no tabs provided to TabLayout")

        if (mIndices.size > mTabLayout.tabCount)
            throw RuntimeException("Cannot attach using more indices than the available tabs")

        notifyIndicesChanged()
        mIsAttached = true
    }

    /**
     * Calling this method will ensure to stop the synchronization between the RecyclerView and
     * the TabLayout.
     */

    fun detach() {
        clearListeners()
        mIsAttached = false
    }

    /**
     * This method will ensure that the synchronization is up-to-date with the data provided.
     */
    private fun reAttach() {
        detach()
        attach()
    }

    /**
     * Calling this method will
     */
    fun updateMediatorWithNewIndices(newIndices: List<Int>): TabbedFlexboxListMediator {
        mIndices = newIndices

        if (mIsAttached) {
            reAttach()
        }

        return this
    }

    /**
     * This method will ensure that any listeners that have been added by the mediator will be
     * removed, including the one listener from
     * @see TabbedListMediator#addOnViewOfTabClickListener((TabLayout.Tab, int) -> Unit)
     */

    private fun clearListeners() {
        mRecyclerView.clearOnScrollListeners()
        for (i in 0 until mTabLayout.tabCount) {
            mTabLayout.getTabAt(i)!!.view.setOnClickListener(null)
        }
        for (i in tabViewCompositeClickListener.getListeners().indices) {
            tabViewCompositeClickListener.getListeners().toMutableList().removeAt(i)
        }
        mTabLayout.removeOnTabSelectedListener(onTabSelectedListener)
        mRecyclerView.removeOnScrollListener(onScrollListener)
    }

    /**
     * This method will attach the listeners required to make the synchronization possible.
     */

    private fun notifyIndicesChanged() {
        tabViewCompositeClickListener.addListener { _, _ -> mTabClickFlag = true }
        tabViewCompositeClickListener.build()
        mTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        mRecyclerView.addOnScrollListener(onScrollListener)
    }

    private val onTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {

            if (!mTabClickFlag) return

            val position = tab.position

            if (mIsSmoothScroll) {
                smoothScroller.targetPosition = mIndices[position]
                mRecyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            } else {
//                (mRecyclerView.layoutManager as FlexboxLayoutManager?)?.scrollToPositionWithOffset(
//                    mIndices[position],
//                    0
//                )
                (mRecyclerView.layoutManager as FlexboxLayoutManager?)?.scrollToPosition(mIndices[position])
                mTabClickFlag = false
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            mRecyclerState = newState
            if (mIsSmoothScroll && newState == RecyclerView.SCROLL_STATE_IDLE) {
                mTabClickFlag = false
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mTabClickFlag) {
                return
            }

            val flexboxLayoutManager: FlexboxLayoutManager =
                recyclerView.layoutManager as FlexboxLayoutManager?
                    ?: throw RuntimeException("No FlexboxLayoutManager attached to the RecyclerView.")

            var itemPosition =
                flexboxLayoutManager.findFirstCompletelyVisibleItemPosition()

            if (itemPosition == -1) {
                itemPosition =
                    flexboxLayoutManager.findFirstVisibleItemPosition()
            }

            if (mRecyclerState == RecyclerView.SCROLL_STATE_DRAGGING
                || mRecyclerState == RecyclerView.SCROLL_STATE_SETTLING
            ) {
                for (i in mIndices.indices) {
                    if (itemPosition == mIndices[i]) {
                        if (!mTabLayout.getTabAt(i)!!.isSelected) {
                            mTabLayout.getTabAt(i)!!.select()
                        }
                        if (flexboxLayoutManager.findLastCompletelyVisibleItemPosition() == mIndices[mIndices.size - 1]) {
                            if (!mTabLayout.getTabAt(mIndices.size - 1)!!.isSelected) {
                                mTabLayout.getTabAt(mIndices.size - 1)!!.select()
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the state of the mediator, either attached or not.
     */

    fun isAttached(): Boolean {
        return mIsAttached
    }

    /**
     * @return the state of the mediator, is smooth scrolling or not.
     */

    fun isSmoothScroll(): Boolean {
        return mIsSmoothScroll
    }

    /**
     * @param smooth sets up the mediator with smooth scrolling
     */

    fun setSmoothScroll(smooth: Boolean) {
        mIsSmoothScroll = smooth
    }

    /**
     * @param listener the listener the will applied on "the view" of the tab. This method is useful
     * when attaching a click listener on the tabs of the TabLayout.
     * Note that this method is REQUIRED in case of the need of adding a click listener on the view
     * of a tab layout. Since the mediator uses a click flag @see TabbedListMediator#mTabClickFlag
     * it's taking the place of the normal on click listener, and thus the need of the composite click
     * listener pattern, so adding listeners should be done using this method.
     */

    fun addOnViewOfTabClickListener(
        listener: (tab: TabLayout.Tab, position: Int) -> Unit
    ) {
        tabViewCompositeClickListener.addListener(listener)
        if (mIsAttached) {
            notifyIndicesChanged()
        }
    }
}