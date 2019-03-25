package cn.tengfeistudio.forum.api.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CommentApi {
    @POST("add_comment")
    Observable<ResponseBody> addComment(@Header("authorization") String authorization, @Query("targetId") int targetId, @Query("content") String content, @Query("topicId") int topicId);
    @GET("get_topic_comment")
    Observable<ResponseBody> getCommentsByTopicId(@Query("topicId") int page);
    @POST("delete_comment_byCommentId")
    Observable<ResponseBody> deleteCommentbyCommentId(@Header("authorization") String authorization,@Query("commentId")int commentId);
    @GET("pull_unread_reply")
    Observable<ResponseBody> getAllUnreadReply(@Header("authorization") String authorization);
    @GET("get_comment_byToUserId")
    Observable<ResponseBody> getCommentsByToUserId(@Header("authorization") String authorization, @Query("page") int page);
    @GET("get_comment_byUserId")
    Observable<ResponseBody> getCommentsByUserId(@Header("authorization") String authorization, @Query("page") int page);
}
