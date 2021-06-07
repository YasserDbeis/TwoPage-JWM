/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.example.twopagejwm

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.window.DisplayFeature
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.example.twopagejwm.R
import com.example.twopagejwm.adapters.PagerAdapter
import com.example.twopagejwm.fragments.FirstPageFragment
import com.example.twopagejwm.fragments.FourthPageFragment
import com.example.twopagejwm.fragments.SecondPageFragment
import com.example.twopagejwm.fragments.ThirdPageFragment
import com.example.twopagejwm.view.VerticalViewPager
import com.example.twopagejwm.viewModels.PagerViewModel
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity(), OnPageChangeListener {

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter

    private lateinit var wm: WindowManager
    private val layoutStateChangeCallback = LayoutStateChangeCallback()

    private lateinit var pagerViewModel: PagerViewModel

    private fun runOnUiThreadExecutor(): Executor {
        val handler = Handler(Looper.getMainLooper())
        return Executor() {
            handler.post(it)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wm = WindowManager(this)

        setupPagerViewModel()

        setupToolbar()

        setupPagerAdapter()

        setContentView(R.layout.activity_two_page)
    }

    private fun setupPagerViewModel() {
        pagerViewModel = ViewModelProviders.of(this)[PagerViewModel::class.java]
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        wm.registerLayoutChangeCallback(
            runOnUiThreadExecutor(),
            layoutStateChangeCallback
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        wm.unregisterLayoutChangeCallback(layoutStateChangeCallback)
    }

    enum class ScreenConfig {
        DualPortrait,
        DualLandscape,
        Single
    }

    inner class LayoutStateChangeCallback : androidx.core.util.Consumer<WindowLayoutInfo> {

        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            layoutStateChangeHandler(newLayoutInfo)
        }
        private fun layoutStateChangeHandler(newLayoutInfo: WindowLayoutInfo) {

            val isDualScreen = isDualMode(newLayoutInfo)
            val isPortrait = isInPortrait(newLayoutInfo)

            val dualLandscapePager = isDualScreen && !isPortrait
            val screenConfig = if (isDualScreen) {
                if(isPortrait) {
                    ScreenConfig.DualPortrait
                } else {
                    ScreenConfig.DualLandscape
                }
            } else {
                ScreenConfig.Single
            }

            pagerAdapter.showTwoPages = isDualScreen && isPortrait


            if(screenConfig == ScreenConfig.DualPortrait) {

                val foldingFeatureWidth = newLayoutInfo.displayFeatures.first().bounds.width().toFloat()
                val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                val nonHingePercent = ((screenWidth - foldingFeatureWidth).toFloat() / screenWidth.toFloat()).toFloat()

                pagerAdapter.dualPageWidth = (nonHingePercent / 2)

                setupViewPager(false)

                viewPager.pageMargin = newLayoutInfo.displayFeatures.first().bounds.width()

            }
            else {
                setupViewPager(dualLandscapePager)
            }

            // this is customizable to your liking -- in this case I want scroll enabled everywhere
            // you can adapt this to only enable scroll in certain screen configurations
            // e.g. pageAdapter.pageContentScrollEnabled = isDualScreen && isPortrait
            pagerAdapter.pageContentScrollEnabled = true
        }

        private fun isInPortrait(newLayoutInfo: WindowLayoutInfo) : Boolean {

            val displayFeatures = newLayoutInfo.displayFeatures

            return if(displayFeatures.isNotEmpty()) {
                val foldingFeature = displayFeatures.first() as FoldingFeature
                foldingFeature.orientation == FoldingFeature.ORIENTATION_VERTICAL
            } else true
        }

        private fun isDualMode(newLayoutInfo: WindowLayoutInfo) : Boolean {
            for (displayFeature : DisplayFeature in newLayoutInfo.displayFeatures) {
                if (displayFeature is FoldingFeature && displayFeature.occlusionMode == FoldingFeature.OCCLUSION_NONE){
                    return true     // spanned across a fold
                }
                if (displayFeature is FoldingFeature && displayFeature.occlusionMode == FoldingFeature.OCCLUSION_FULL){
                    return true     // spanned across a hinge
                }
            }
            return false
        }

    }

    private fun setupPagerAdapter() {
        val fragments = SparseArray<Fragment>()

        fragments.put(0, FirstPageFragment())
        fragments.put(1, SecondPageFragment())
        fragments.put(2, ThirdPageFragment())
        fragments.put(3, FourthPageFragment())

        pagerAdapter = PagerAdapter(supportFragmentManager, fragments)
    }

    private fun setupViewPager(dualLandscapePager: Boolean) {
        if (::viewPager.isInitialized) {
            viewPager.adapter = null
        }

        val showPagerID = if(dualLandscapePager) R.id.pager_vert_scroll else R.id.pager_horiz_scroll
        val hidePagerID = if(showPagerID == R.id.pager_vert_scroll) R.id.pager_horiz_scroll else R.id.pager_vert_scroll

        val currentPage = pagerViewModel.getPageSelectionLiveData().value ?: 0

        findViewById<ViewPager>(hidePagerID).visibility = ViewPager.GONE
        viewPager = findViewById<ViewPager>(showPagerID).also {
            it.adapter = pagerAdapter
            it.currentItem = currentPage
            it.addOnPageChangeListener(this)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(newPosition: Int) {
        pagerViewModel.setPageSelectionLiveData(newPosition)
    }

    override fun onPageScrollStateChanged(state: Int) {}
}