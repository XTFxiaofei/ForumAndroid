package cn.tengfeistudio.forum.api.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TopicApi {
    /**
     * 发贴
     */
    @POST("send_topic")
    Observable<ResponseBody> sendTopic(@Header("authorization") String authorization, @Query("title") String title, @Query("content") String content, @Query("theme") String theme);

    /**
     * 通过topicId查询帖子
     */
    @GET("get_topic_byTopicId")
    Observable<ResponseBody> getTopicByTopicId(@Query("topicId") int topicId);

    /**
     * 通过commentId查询帖子
     */
    @GET("get_topic_byCommentId")
    Observable<ResponseBody> getTopicByCommentId(@Query("commentId") int commentId);

    /**
     * 通过用户id查询该用户的帖子
     */
    @GET("get_topics_byUserId")
    Observable<ResponseBody> getTopicsByUserId(@Header("authorization") String authorization, @Query("page") int page);

    /**
     * 通过theme查询贴子
     */
    @GET("get_topics_byTheme")
    Observable<ResponseBody> getTopicsByTheme(@Query("theme") String theme, @Query("page") int page);

    @POST("delete_topic_byTopicId")
    Observable<ResponseBody> deleteTopicByTopicId(@Header("authorization") String authorization,@Query("topicId")int topicId,@Query("flag")int flag);

}
