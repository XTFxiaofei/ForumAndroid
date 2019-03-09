package cn.tengfeistudio.forum.api.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ActivityApi {
    @POST("get_all_activity")
    Observable<ResponseBody> getAllActivity(@Query("page") int page);
    @GET("get_all_activity_byPlaceAndType")
    Observable<ResponseBody> selectActivityByPlaceAndType(@Query("page") int page, @Query("place") String place, @Query("type") String type);
    @GET("get_activity_byActivityId")
    Observable<ResponseBody> getActivity(@Query("activityId") int activityId);
}
