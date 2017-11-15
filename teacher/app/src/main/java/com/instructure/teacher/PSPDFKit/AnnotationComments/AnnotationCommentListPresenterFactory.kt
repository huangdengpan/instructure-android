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
package com.instructure.teacher.PSPDFKit.AnnotationComments

import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation
import instructure.androidblueprint.PresenterFactory

class AnnotationCommentListPresenterFactory(
        val annotations: ArrayList<CanvaDocAnnotation>,
        val canvaDocId: String,
        val sessionId: String,
        val canvaDocDomain: String,
        val assigneeId: Long): PresenterFactory<AnnotationCommentListPresenter> {
    override fun create() = AnnotationCommentListPresenter(annotations, canvaDocId, sessionId, canvaDocDomain, assigneeId)
}