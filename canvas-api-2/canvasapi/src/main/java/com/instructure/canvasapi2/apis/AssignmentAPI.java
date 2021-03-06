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
 *
 */

package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.AssignmentGroup;
import com.instructure.canvasapi2.models.GradeableStudent;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.models.post_models.AssignmentPostBodyWrapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class AssignmentAPI {

    interface AssignmentInterface {
        @GET("courses/{courseId}/assignments/{assignmentId}?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides")
        Call<Assignment> getAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET("courses/{courseId}/assignment_groups/{assignmentGroupId}")
        Call<AssignmentGroup> getAssignmentGroup(
                @Path("courseId") long courseId,
                @Path("assignmentGroupId") long assignmentId);

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        Call<List<AssignmentGroup>> getFirstPageAssignmentGroupListWithAssignments(@Path("courseId") long courseId);

        @GET
        Call<List<AssignmentGroup>> getNextPageAssignmentGroupListWithAssignments(@Url String nextUrl);

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        Call<List<AssignmentGroup>> getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Path("courseId") long courseId, @Query("grading_period_id") long gradingPeriodId, @Query("scope_assignments_to_student") boolean scopeToStudent);

        @GET
        Call<List<AssignmentGroup>> getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(@Url String nextUrl);

        @GET
        Call<List<AssignmentGroup>> getNextPage(@Url String nextUrl);


        @PUT("courses/{courseId}/assignments/{assignmentId}")
        Call<Assignment> editAssignment(
                @Path("courseId") long courseId,
                @Path("assignmentId") long assignmentId,
                @Body AssignmentPostBodyWrapper body);

        @DELETE("courses/{courseId}/assignments/{assignmentId}")
        Call<Assignment> deleteAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET("courses/{courseId}/assignments/{assignmentId}/gradeable_students")
        Call<List<GradeableStudent>> getFirstPageGradeableStudentsForAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET
        Call<List<GradeableStudent>> getNextPageGradeableStudents(@Url String nextUrl);

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions?include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group")
        Call<List<Submission>> getFirstPageSubmissionsForAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET
        Call<List<Submission>> getNextPageSubmissions(@Url String nextUrl);

        @GET("courses/{courseId}/assignments?include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&include[]=all_dates&include[]=overrides")
        Call<List<Assignment>> getAssignments(@Path("courseId") long courseId);

        @GET
        Call<List<Assignment>> getNextPageAssignments(@Url String nextUrl);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/courses/{courseId}/assignments/{assignmentId}?include[]=submission")
        Call<Assignment> getAssignmentAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("courseId") String courseId, @Path("assignmentId") String assignmentId);


        //endregion
    }

    public static void getAssignment(long courseId, long assignmentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<Assignment> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignment(courseId, assignmentId)).enqueue(callback);
    }

    public static void getAssignmentGroup(long courseId, long assignmentGroupId, RestBuilder adapter, StatusCallback<AssignmentGroup> callback, RestParams params) {
        callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignmentGroup(courseId, assignmentGroupId)).enqueue(callback);
    }

    public static void getFirstPageAssignmentGroupsWithAssignments(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<AssignmentGroup>> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageAssignmentGroupListWithAssignments(courseId)).enqueue(callback);
    }

    public static void getNextPageAssignmentGroupsWithAssignments(boolean forceNetwork, @NonNull String nextUrl, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<AssignmentGroup>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageAssignmentGroupListWithAssignments(nextUrl)).enqueue(callback);
    }

    public static void getFirstPageAssignmentGroupsWithAssignmentsForGradingPeriod(long courseId, long gradingPeriodId, boolean scopeToStudent, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<AssignmentGroup>> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(courseId, gradingPeriodId, scopeToStudent)).enqueue(callback);
    }

    public static void getNextPageAssignmentGroupsWithAssignmentsForGradingPeriod(boolean forceNetwork, @NonNull String nextUrl, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<AssignmentGroup>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageAssignmentGroupListWithAssignmentsForGradingPeriod(nextUrl)).enqueue(callback);
    }

    public static void editAssignment(long courseId, long assignmentId, AssignmentPostBodyWrapper body, RestBuilder adapter, final StatusCallback<Assignment> callback, RestParams params, boolean serializeNulls){
        if (serializeNulls) {
            callback.addCall(adapter.buildSerializeNulls(AssignmentInterface.class, params).editAssignment(courseId, assignmentId, body)).enqueue(callback);
        } else {
            callback.addCall(adapter.build(AssignmentInterface.class, params).editAssignment(courseId, assignmentId, body)).enqueue(callback);
        }
    }

    public static void editAssignmentAllowNullValues(long courseId, long assignmentId, AssignmentPostBodyWrapper body, RestBuilder adapter, final StatusCallback<Assignment> callback, RestParams params){
        callback.addCall(adapter.build(AssignmentInterface.class, params).editAssignment(courseId, assignmentId, body)).enqueue(callback);
    }

    public static void deleteAssignment(long courseId, long assignmentId, RestBuilder adapter, final StatusCallback<Assignment> callback, RestParams params){
        callback.addCall(adapter.build(AssignmentInterface.class, params).deleteAssignment(courseId, assignmentId)).enqueue(callback);

    }

    public static void getFirstPageGradeableStudentsForAssignment(long courseId, long assignmentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<GradeableStudent>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageGradeableStudentsForAssignment(courseId, assignmentId)).enqueue(callback);
    }

    public static void getNextPageGradeableStudents(boolean forceNetwork, @NonNull String nextUrl, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<GradeableStudent>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageGradeableStudents(nextUrl)).enqueue(callback);
    }

    public static void getFirstPageSubmissionsForAssignment(long courseId, long assignmentId,  boolean forceNetwork, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Submission>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageSubmissionsForAssignment(courseId, assignmentId)).enqueue(callback);
    }

    public static void getNextPageSubmissions(@NonNull String nextUrl, @NonNull RestBuilder adapter, boolean forceNetwork, @NonNull StatusCallback<List<Submission>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageSubmissions(nextUrl)).enqueue(callback);
    }

    public static void getFirstPageAssignments(long courseId, boolean forceNetwork, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Assignment>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignments(courseId)).enqueue(callback);
    }

    public static void getNextPageAssignments(@NonNull String nextUrl, @NonNull RestBuilder adapter, boolean forceNetwork, @NonNull StatusCallback<List<Assignment>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .withForceReadFromNetwork(forceNetwork)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageAssignments(nextUrl)).enqueue(callback);
    }

    public static void getAssignmentAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String courseId,
            @NonNull String assignmentId,
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<Assignment> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignmentAirwolf(parentId, studentId, courseId, assignmentId)).enqueue(callback);
    }
}
