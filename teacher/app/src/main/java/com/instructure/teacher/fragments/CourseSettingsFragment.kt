/*
* Copyright (C) 2017 - present Instructure, Inc.
*
*     This program is free software: you can redistribute it and/or modify
*     it under the terms of the GNU General Public License as published by
*     the Free Software Foundation, version 3 of the License.
*
*     This program is distributed in the hope that it will be useful,
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*     GNU General Public License for more details.
*
*     You should have received a copy of the GNU General Public License
*     along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.instructure.teacher.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.globalName
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.R
import com.instructure.teacher.dialog.EditCourseNameDialog
import com.instructure.teacher.dialog.RadioButtonDialog
import com.instructure.teacher.factory.CourseSettingsFragmentPresenterFactory
import com.instructure.teacher.presenters.CourseSettingsFragmentPresenter
import com.instructure.teacher.utils.*
import com.instructure.teacher.viewinterface.CourseSettingsFragmentView
import kotlinx.android.synthetic.main.fragment_course_settings.*
import kotlinx.android.synthetic.main.view_edit_course_homepage.*
import kotlinx.android.synthetic.main.view_rename_course.*

class CourseSettingsFragment : BasePresenterFragment<
        CourseSettingsFragmentPresenter,
        CourseSettingsFragmentView>(), CourseSettingsFragmentView {

    private var mCourse: Course by ParcelableArg(default = Course())

    private val mHomePages: Map<String, String> by lazy {
        hashMapOf(
                Pair("feed", getString(R.string.course_activity_stream)),
                Pair("wiki", getString(R.string.pages_front_page)),
                Pair("modules", getString(R.string.course_modules)),
                Pair("assignments", getString(R.string.assignments_list)),
                Pair("syllabus", getString(R.string.syllabus))
        )
    }

    private val mCourseColor by lazy { ColorKeeper.getOrGenerateColor(mCourse) }

    override fun layoutResId() = R.layout.fragment_course_settings
    override fun getPresenterFactory() = CourseSettingsFragmentPresenterFactory()

    override fun onReadySetGo(presenter: CourseSettingsFragmentPresenter?) {
        setupToolbar()
        courseImage.setCourseImage(mCourse, mCourseColor)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            updateCourseName(mCourse)
            updateCourseHomePage(mCourse.homePage)
        } else {
            updateCourseName(mCourse)
        }
    }

    override fun onPresenterPrepared(presenter: CourseSettingsFragmentPresenter?) {
        editCourseNameRoot.onClickWithRequireNetwork {
            presenter?.editCourseNameClicked()
        }

        editHomeRoot.onClickWithRequireNetwork {
            presenter?.editCourseHomePageClicked()
        }
    }

    private fun setupToolbar() {
        toolbar.setupBackButton(this)
        toolbar.title = getString(R.string.course_settings)
        ViewStyler.colorToolbarIconsAndText(activity, toolbar, Color.BLACK)
        ViewStyler.setStatusBarLight(activity)
        toolbar.setSubtitleTextColor(mCourseColor)
    }

    override fun showEditCourseNameDialog() {
        val dialog: EditCourseNameDialog = EditCourseNameDialog.getInstance(activity.supportFragmentManager, mCourse) { newName ->
            presenter?.editCourseName(newName, mCourse)
        }

        dialog.show(activity.supportFragmentManager, EditCourseNameDialog::class.java.simpleName)
    }

    override fun showEditCourseHomePageDialog() {
        val (keys, values) = mHomePages.toList().unzip()
        val selectedIdx = keys.indexOf(mCourse.homePage)
        val dialog = RadioButtonDialog.getInstance(activity.supportFragmentManager, getString(R.string.set_home_to), values as ArrayList<String>, selectedIdx) { idx ->
                presenter?.editCourseHomePage(keys[idx], mCourse)
        }

       dialog.show(activity.supportFragmentManager, RadioButtonDialog::class.java.simpleName)
    }

    override fun updateCourseName(course: Course) {
        courseName.text = course.globalName
        toolbar.subtitle = course.globalName
        mCourse.globalName = course.globalName
        setResult()
    }

    override fun updateCourseHomePage(newHomePage: String) {
        courseHomePage.text = mHomePages[newHomePage]
        mCourse.homePage = newHomePage
        setResult()
    }

    private fun setResult() {
        activity.setResult(Activity.RESULT_OK, Intent().apply { putExtra(Const.COURSE, mCourse as Parcelable) })
    }

    override fun onRefreshFinished() {
        // TODO: ???
    }

    override fun onRefreshStarted() {
        // TODO: ???
    }

    companion object {
        @JvmStatic
        fun newInstance(course: Course) = CourseSettingsFragment().apply {
            mCourse = course
        }
    }
}
