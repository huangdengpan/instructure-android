/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.presenters

import android.content.Context
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.R
import com.instructure.teacher.events.DiscussionTopicEvent
import com.instructure.teacher.events.DiscussionTopicHeaderEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.viewinterface.DiscussionsDetailsView
import instructure.androidblueprint.FragmentPresenter
import kotlinx.coroutines.experimental.Job
import retrofit2.Response
import java.util.*

class DiscussionsDetailsPresenter(
        var canvasContext: CanvasContext,
        var discussionTopicHeader: DiscussionTopicHeader,
        var discussionTopic: DiscussionTopic,
        val discussionEntryId: Long,
        val discussionSkipId: String,
        val isAnnouncement: Boolean) : FragmentPresenter<DiscussionsDetailsView>() {

    var scrollPosition: Int = 0
    private var mApiCalls: Job? = null
    private var discussionEntryRatingCallback: StatusCallback<Void>? = null
    private var mDiscussionMarkAsReadApiCalls: Job? = null

    override fun loadData(forceNetwork: Boolean) {
        DiscussionManager.getFullDiscussionTopic(canvasContext, discussionTopicHeader.id, forceNetwork, mDiscussionTopicCallback)
    }

    override fun refresh(forceNetwork: Boolean) {
        mDiscussionTopicCallback.reset()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getSubmissionData(forceNetwork: Boolean) {
        mApiCalls?.cancel()
        mApiCalls = weave {
            val assignment = discussionTopicHeader.assignment
            viewCallback?.onRefreshStarted()
            try {
                val submissionSummary = awaitApi<SubmissionSummary> { SubmissionManager.getSubmissionSummary(assignment.courseId, assignment.id, forceNetwork, it) }
                val totalStudents = submissionSummary.graded + submissionSummary.ungraded + submissionSummary.notSubmitted
                viewCallback?.updateSubmissionDonuts(totalStudents, submissionSummary.graded, submissionSummary.ungraded, submissionSummary.notSubmitted)
            } catch (ignore: Throwable) {
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getDiscussionTopicHeader(discussionTopicHeaderId: Long, forceNetwork: Boolean) {
        DiscussionManager.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, object: StatusCallback<DiscussionTopicHeader>() {
            override fun onResponse(response: Response<DiscussionTopicHeader>, linkHeaders: LinkHeaders, type: ApiType) {
                response.body()?.let {
                    discussionTopicHeader = it
                    viewCallback?.populateDiscussionTopicHeader(discussionTopicHeader, false)
                }
            }
        })
    }

    fun getFormattedDueDate(context: Context, date: Date?): String {
        if(date == null) return ""
        val dueDate = DateHelper.getDayMonthDateFormatUniversal().format(date)
        val dueTime = DateHelper.getDayAbbreviationFormat(context).format(date)
        if(isAnnouncement || discussionTopicHeader.assignment == null) {
            return DateHelper.getMonthDayAtTime(context, date, context.getString(R.string.at)) ?: ""
        }
        return context.getString(R.string.due_date_at_time).format(dueDate, dueTime)
    }

    fun updateDiscussionEntryToDiscussionTopic(updatedEntry: DiscussionEntry) {
        val entry = findEntry(updatedEntry.id)
        entry?.message = updatedEntry.message
        entry?.attachments = updatedEntry.attachments
        DiscussionTopicEvent(discussionTopic, getSkipId()).post()
    }

    fun addDiscussionEntryToDiscussionTopic(newEntry: DiscussionEntry, populateAfter: Boolean) {
        //If for some reason, typically due to lifecycle cancel any API work being done
        mDiscussionTopicCallback.cancel()

        if(newEntry.author == null) {
            setAuthorAsSelf(newEntry)
        }

        if(newEntry.parentId == -1L) { //No Parent, add to DiscussionTopicHeader
            discussionTopic.views.add(newEntry)
            notifyEntryAdded(populateAfter)
            return
        }

        //Find the parent, add it to the list of views
        discussionTopic.views.forEach { discussionEntry ->
            if(discussionEntry.id == newEntry.parentId) {
                newEntry.init(discussionTopic, discussionEntry)
                discussionEntry.addReply(newEntry)
                discussionEntry.totalChildren += 1
                notifyEntryAdded(populateAfter)
                return
            } else {
                val parentEntry = recursiveFind(newEntry.parentId, discussionEntry.replies)
                if(parentEntry != null) {
                    newEntry.init(discussionTopic, parentEntry)
                    parentEntry.addReply(newEntry)
                    parentEntry.totalChildren += 1
                    notifyEntryAdded(populateAfter)
                    return
                }
            }
        }
    }

    private fun notifyEntryAdded(populateAfter: Boolean) {
        if(populateAfter) viewCallback?.populateDiscussionTopic(discussionTopicHeader, discussionTopic)
        DiscussionTopicEvent(discussionTopic, getSkipId()).post()

        discussionTopicHeader.incrementDiscussionSubentryCount() //Update subentry count
        discussionTopicHeader.lastReplyAt?.time = Date().time //Update last post time
        DiscussionTopicHeaderEvent(discussionTopicHeader).post()
    }

    private val mDiscussionTopicCallback = object: StatusCallback<DiscussionTopic>(){
        override fun onResponse(response: Response<DiscussionTopic>, linkHeaders: LinkHeaders, type: ApiType) {
            if(response.code() == 403) {
                //forbidden
                viewCallback?.populateAsForbidden()
            } else {
                response.body()?.let {
                    discussionTopic = it
                    discussionTopic.views.forEach {
                        it.init(discussionTopic, it)
                    }
                    viewCallback?.populateDiscussionTopic(discussionTopicHeader, discussionTopic)
                }
            }
        }
    }

    private fun recursiveFind(startEntryId: Long, replies: List<DiscussionEntry>): DiscussionEntry? {
        replies.forEach {
            if(it.id == startEntryId) {
                return it
            } else {
                val items = recursiveFind(startEntryId, it.replies)
                if(items != null) {
                    return items
                }
            }
        }
        return null
    }

    fun findEntry(entryId: Long): DiscussionEntry? {
        discussionTopic.views.forEach { discussionEntry ->
            if(discussionEntry.id == entryId) {
                return discussionEntry
            }

            val entry = recursiveFind(entryId, discussionEntry.replies)
            if(entry != null) {
                return entry
            }
        }
        return null
    }

    private fun setAuthorAsSelf(discussionEntry: DiscussionEntry) {
        val user = ApiPrefs.user
        if(user != null) {
            val dp = DiscussionParticipant(user.id)
            dp.avatarImageUrl = user.avatarUrl
            dp.displayName = user.name
            dp.htmlUrl = ""
            discussionEntry.author = dp
        }
    }

    fun updateDiscussionTopic(discussionTopic: DiscussionTopic) {
        this.discussionTopic = discussionTopic
        viewCallback?.populateDiscussionTopic(this.discussionTopicHeader, this.discussionTopic)
    }

    fun likeDiscussionPressed(id: Long) {
        if(discussionEntryRatingCallback != null && discussionEntryRatingCallback!!.isCallInProgress) return

        val entry = findEntry(id)
        if(entry != null) {
            //By default users ratings are 0. If they click and no entry rating exits then they have not rated and are 'liking' a post.
            val rating = if(discussionTopic.entryRatings.containsKey(id)) discussionTopic.entryRatings[id] ?: 0 else 0
            val newRating = if(rating == 1) 0 else 1
            discussionEntryRatingCallback = object: StatusCallback<Void>() {
                override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                    if(response.code() in 200..299) {
                        discussionTopic.entryRatings.put(id, newRating)

                        if(newRating == 1) {
                            entry.ratingSum += 1
                            entry.setHasRated(true)
                            viewCallback?.updateDiscussionLiked(entry)
                        } else if(entry.ratingSum > 0) {
                            entry.ratingSum -= 1
                            entry.setHasRated(false)
                            viewCallback?.updateDiscussionUnliked(entry)
                        }
                    }
                }
            }
            DiscussionManager.rateDiscussionEntry(canvasContext, discussionTopicHeader.id, id, newRating, discussionEntryRatingCallback)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun markAsRead(ids: List<Long>) {
        if(mDiscussionMarkAsReadApiCalls != null && mDiscussionMarkAsReadApiCalls!!.isActive) return
        mDiscussionMarkAsReadApiCalls = weave {
            val markedAsReadIds: MutableList<Long> = ArrayList()
            ids.forEach { entryId ->
                val response = awaitApiResponse<Void?>{ DiscussionManager.markDiscussionTopicEntryRead(canvasContext, discussionTopicHeader.id, entryId, it) }
                if(response.isSuccessful) {
                    markedAsReadIds.add(entryId)
                    val entry = findEntry(entryId)
                    entry?.isUnread = false
                    discussionTopic.unreadEntriesMap.remove(entryId)
                    discussionTopic.unreadEntries.remove(entryId)
                    if (discussionTopicHeader.unreadCount > 0) discussionTopicHeader.unreadCount -= 1
                }
            }

            viewCallback?.updateDiscussionsMarkedAsReadCompleted(markedAsReadIds)
            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
        }
    }

    fun deleteDiscussionEntry(entryId: Long) {
        DiscussionManager.deleteDiscussionEntry(canvasContext, discussionTopicHeader.id, entryId, object: StatusCallback<Void>() {
            override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                if(response.code() in 200..299) {
                    val entry = findEntry(entryId)
                    if (entry != null) {
                        entry.isDeleted = true
                        viewCallback?.updateDiscussionAsDeleted(entry)
                        discussionTopicHeader.decrementDiscussionSubentryCount()
                        DiscussionTopicHeaderEvent(discussionTopicHeader).post()
                    }
                }
            }
        })
    }

    /**
     * Generates a skip id unique per fragment. That way when a new item is added things don't get called multiple times.
     * However, fragments already on the stack will still get the event as it won't be skipped. This keeps the DiscussionTopic
     * updated for older fragments on the stack who need to know about the changes.
     */
    fun getSkipId(): String {
        return  discussionSkipId
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mDiscussionMarkAsReadApiCalls?.cancel()
    }
}
