/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:JvmName("ViewUtils")
@file:Suppress("unused")

package com.instructure.teacher.utils

import android.annotation.SuppressLint
import android.support.annotation.DrawableRes
import android.support.annotation.MenuRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.style.URLSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.router.RouteMatcher
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.RouteContext
import de.hdodenhof.circleimageview.CircleImageView


fun CircleImageView.setAnonymousAvatar() = setImageResource(R.drawable.vd_user)

/**
 * Loads the given resource as this Toolbar's icon, assigns it the given content description, and
 * propagates its clicks to the provided function.
 */
@JvmName("setupToolbarNavButtonWithCallback")
fun Toolbar?.setupNavButtonWithCallback(
        @DrawableRes iconResId: Int,
        @StringRes contentDescriptionResId: Int,
        onClick: () -> Unit) {
    if (this == null) return
    setNavigationIcon(iconResId)
    setNavigationContentDescription(contentDescriptionResId)
    setNavigationOnClickListener { onClick() }
    requestAccessibilityFocus()
}

/**
 * Loads the given resource as this Toolbar's icon, assigns it the given content description, and
 * propagates its clicks to the provided function.
 */
@JvmName("setupToolbarNavButtonWithoutCallback")
fun Toolbar?.setupNavButtonIcon(
        @DrawableRes iconResId: Int,
        @StringRes contentDescriptionResId: Int) {
    if (this == null) return
    setNavigationIcon(iconResId)
    setNavigationContentDescription(contentDescriptionResId)
    requestAccessibilityFocus()
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events on this icon are propagated to the
 * provided function
 */
@SuppressLint("PrivateResource")
@JvmName("setupToolbarBackButton")
fun Toolbar?.setupBackButton(onClick: () -> Unit) = setupNavButtonWithCallback(
        android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material,
        android.support.v7.appcompat.R.string.abc_action_bar_up_description,
        onClick
)

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarBackButton")
fun Toolbar?.setupBackButton(fragment: Fragment?) = setupBackButton {
    if(fragment != null && fragment.activity != null && fragment.activity is MasterDetailInteractions) {
        fragment.activity.finish()
    } else {
        fragment?.activity?.onBackPressed()
    }
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarBackButtonAsBackPressedOnly")
fun Toolbar?.setupBackButtonAsBackPressedOnly(fragment: Fragment?) = setupBackButton {
    fragment?.activity?.onBackPressed()
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupBackButtonWithExpandCollapseAndBack")
fun Toolbar?.setupBackButtonWithExpandCollapseAndBack(
        fragment: Fragment?,
        onClick: () -> Unit) {
    if(fragment != null && fragment.isTablet) {
        if(fragment.activity != null && fragment.activity is MasterDetailInteractions) {
            if((fragment.activity as MasterDetailInteractions).isMasterVisible) {
                setupNavButtonWithCallback(R.drawable.vd_collapse_horizontal, R.string.contentDescriptionCollapsePage, onClick)
            } else {
                setupNavButtonWithCallback(R.drawable.vd_expand_horizontal, R.string.contentDescriptionExpandPage, onClick)
            }
        }
    } else {
        setupBackButton {
            fragment?.activity?.onBackPressed()
        }
    }
}

/**
 * Changes this Toolbar's icon to a back arrow. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("updateToolbarExpandCollapseIcon")
fun Toolbar?.updateToolbarExpandCollapseIcon(fragment: Fragment?) {
    if (this == null) return
    if(fragment != null && fragment.isTablet && fragment.activity != null && fragment.activity is MasterDetailInteractions) {
        if((fragment.activity as MasterDetailInteractions).isMasterVisible) {
            setNavigationIcon(R.drawable.vd_collapse_horizontal)
            setNavigationContentDescription(R.string.contentDescriptionCollapsePage)
        } else {
            setNavigationIcon(R.drawable.vd_expand_horizontal)
            setNavigationContentDescription(R.string.contentDescriptionExpandPage)
        }
    }
}

/**
 * Changes this Toolbar's icon to a close (X) icon. Click events on this icon are propagated to the
 * provided function.
 */
@SuppressLint("PrivateResource")
@JvmName("setupToolbarCloseButton")
fun Toolbar?.setupCloseButton(onClick: () -> Unit) = setupNavButtonWithCallback(
        android.support.v7.appcompat.R.drawable.abc_ic_clear_material, R.string.close,
        onClick
)


/**
 * Changes this Toolbar's icon to a close (X) icon. Click events will attempt to call onBackPressed()
 * on the given fragment's activity
 */
@JvmName("setupToolbarCloseButton")
fun Toolbar?.setupCloseButton(fragment: Fragment?) = setupCloseButton { fragment?.activity?.onBackPressed() }


/**
 * Inflates the provided menu resource into this Toolbar and propagates menu item click events
 * to the provided callback
 *
 * Note: This clears any existing menu items. It is safe to call multiple times throughout
 * the Activity/Fragment lifecycle, but should probably not be used when it is expected that
 * the menu will also be populated by other sources.
 */
@JvmName("setupToolbarMenu")
fun Toolbar?.setupMenu(@MenuRes menuResId: Int, callback: (MenuItem) -> Unit) {
    if (this == null) return
    menu.clear()
    inflateMenu(menuResId)
    setOnMenuItemClickListener { callback(it); true }
}


/**
 * Attempts to apply the Toolbar's text style to this TextView
 */
fun TextView?.adoptToolbarStyle(toolbar: Toolbar?) {
    if (this == null || toolbar == null) return
    toolbar.post {
        val preserveColors = textColors

        val field = Toolbar::class.java.getDeclaredField("mTitleTextAppearance")
        field.isAccessible = true
        val appearance = field.get(toolbar) as Int

        @Suppress("DEPRECATION")
        if (appearance != 0) setTextAppearance(context, appearance)

        setTextColor(preserveColors)
    }
}

fun TextView.linkifyTextView() {
    val current = this.text as SpannableString
    val spans = current.getSpans(0, current.length, URLSpan::class.java)

    for (span in spans) {
        val start = current.getSpanStart(span)
        val end = current.getSpanEnd(span)

        current.removeSpan(span)
        current.setSpan(DefensiveURLSpan(span.url), start, end,
                0)
    }
}

class DefensiveURLSpan(private val url: String) : URLSpan(url) {

    override fun onClick(widget: View) {
        if(RouteMatcher.getInternalRoute(url, ApiPrefs.domain) != null) {
            RouteMatcher.routeUrl(widget.context, url, ApiPrefs.domain, RouteContext.EXTERNAL)
        } else {
            val intent = InternalWebViewActivity.createIntent(widget.context, url, "", false)
            widget.context.startActivity(intent)
        }
    }
}

