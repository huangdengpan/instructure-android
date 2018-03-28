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
package com.instructure.teacher.ui.pages

import android.support.test.InstrumentationRegistry
import com.instructure.soseedy.Quiz
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.PageWithIdAssert

class QuizDetailsPage : BasePage(), PageAssert by PageWithIdAssert(R.id.quizDetailsPage) {

    private val backButton by OnViewWithContentDescription(R.string.abc_action_bar_up_description)
    private val toolbarTitle by OnViewWithText(R.string.quiz_details)
    private val quizTitleTextView by OnViewWithId(R.id.quizTitleTextView)
    private val pointsTextView by OnViewWithId(R.id.pointsTextView, autoAssert = false)
    private val publishStatusIconView by OnViewWithId(R.id.publishStatusIconView)
    private val publishStatusTextView by OnViewWithId(R.id.publishStatusTextView)
    private val dueSectionLabel by OnViewWithId(R.id.dueSectionLabel)
    private val instructionsSectionLabel by OnViewWithId(R.id.instructionsSectionLabel, autoAssert = false)
    private val editButton by OnViewWithId(R.id.menu_edit)
    private val dueDatesLayout by OnViewWithId(R.id.dueLayout)
    private val submissionsLayout by OnViewWithId(R.id.submissionsLayout)
    private val viewAllSubmissions by OnViewWithId(R.id.viewAllSubmissions)
    private val instructionsWebView by WaitForViewWithId(R.id.instructionsWebView, autoAssert = false)
    private val noInstructionsTextView by WaitForViewWithId(R.id.noInstructionsTextView, autoAssert = false)
    private val availabilityTextView by OnViewWithId(R.id.availabilityTextView, autoAssert = false)
    private val availabilityLayout by OnViewWithId(R.id.availabilityLayout, autoAssert = false)
    private val availableFromTextView by OnViewWithId(R.id.availableFromTextView, autoAssert = false)
    private val availableToTextView by OnViewWithId(R.id.availableToTextView, autoAssert = false)
    private val gradedDonut by OnViewWithId(R.id.gradedWrapper)
    private val ungradedDonut by OnViewWithId(R.id.ungradedWrapper)
    private val notSubmittedDonut by OnViewWithId(R.id.notSubmittedWrapper)

    fun assertDisplaysInstructions() {
        instructionsWebView.scrollTo()
        instructionsWebView.assertVisible()
    }

    fun assertDisplaysNoInstructionsView() {
        noInstructionsTextView.assertVisible()
    }

    fun openAllDatesPage() {
        dueDatesLayout.click()
    }

    fun openEditPage() {
        editButton.click()
    }

    fun openSubmissionsPage() {
        viewAllSubmissions.scrollTo()
        viewAllSubmissions.click()
    }

    fun assertQuizDetails(quiz: Quiz) {
        quizTitleTextView.assertHasText(quiz.title)
        if (quiz.published) {
            publishStatusTextView.assertHasText(R.string.published)
        } else {
            publishStatusTextView.assertHasText(R.string.not_published)
        }
    }

    fun assertQuizClosed() {
        availableFromTextView.assertNotDisplayed()
        availableToTextView.assertNotDisplayed()
        availabilityLayout.assertDisplayed()
        availabilityTextView.assertHasText(com.instructure.teacher.R.string.closed)
    }

    fun assertToFilledAndFromEmpty() {
        availableFromTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableToTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    fun assertFromFilledAndToEmpty() {
        availableToTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableFromTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    fun assertQuizNameChanged(newQuizName: String) {
        quizTitleTextView.assertHasText(newQuizName)
    }

    fun assertQuizPointsChanged(newQuizPoints: String) {
        pointsTextView.assertContainsText(newQuizPoints)
    }

    fun assertHasSubmitted() {
        val resources = InstrumentationRegistry.getTargetContext()
        gradedDonut.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_graded).format(1, 1))
    }

    fun assertNotSubmitted() {
        val resources = InstrumentationRegistry.getTargetContext()
        notSubmittedDonut.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_unsubmitted).format(1, 1))
    }


    fun assertQuizTitleChanged(newQuizTitle: String) {
        quizTitleTextView.assertHasText(newQuizTitle)
    }

    fun assertAccessCodeChanged(newCode: String) {
        //TODO: accessCodeTextView.assertHasText(newCode)
    }
}
