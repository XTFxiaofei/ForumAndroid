package cn.tengfeistudio.forum.api.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CollectionApi {
       /** 收藏活动 */
    @POST("collect_activityId")
    Observable<ResponseBody> collectActivity(@Header("authorization") String authorization, @Query("activityId") int  activityId);
    /** 获取已经收藏的集合 */
    @GET("get_collection_activityId")
    Observable<ResponseBody> getCollectActivity(@Header("authorization") String authorization);
    /** 取消收藏 */
    @POST("cancel_collect_activityId")
    Observable<ResponseBody> cancelCollectActivity(@Header("authorization") String authorization, @Query("activityId") int  activityId);
    /** 用户收藏的活动列表 */
    @POST("get_collect_activitylist")
    Observable<ResponseBody> collectActivityList(@Header("authorization") String authorization, @Query("page") int  page);

}
