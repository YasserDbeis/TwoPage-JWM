/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.example.twopagejwm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.example.twopagejwm.R
import kotlinx.android.synthetic.main.fragment_two_page_third_page.*

/**
 * Implementation for the third page
 */
class ThirdPageFragment : BasePageFragment() {
    override fun getScrollingContent(): ScrollView = page3_content

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_two_page_third_page, container, false)
    }
}