package cn.tengfeistudio.forum.api.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
       /** 登录 */
    @POST("login")
    Observable<ResponseBody> doLogin(@Query("email") String email, @Query("password") String password);
    /** 获取用户信息 */
    @GET("get_user_details")
    Observable<ResponseBody> getUserDetails(@Header("authorization") String authorization);
    /** 重置密码 */
    @POST("reset_password")
    Observable<ResponseBody> resetPassword(@Header("authorization") String authorization, @Query("oldPassword") String oldPassword, @Query("newPassword") String newPassword);
    /** 注册 */
    @POST("register")
    Observable<ResponseBody> doRegister(@Query("email") String email, @Query("nickName") String nickName, @Query("studentCard") String studentCard, @Query("phone") long phone, @Query("password") String password);


}
