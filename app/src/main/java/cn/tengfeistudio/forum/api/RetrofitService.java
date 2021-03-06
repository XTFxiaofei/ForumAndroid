package cn.tengfeistudio.forum.api;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import cn.tengfeistudio.forum.App;
import cn.tengfeistudio.forum.api.api.ActivityApi;
import cn.tengfeistudio.forum.api.api.CollectionApi;
import cn.tengfeistudio.forum.api.api.CommentApi;
import cn.tengfeistudio.forum.api.api.GithubApi;
import cn.tengfeistudio.forum.api.api.TopicApi;
import cn.tengfeistudio.forum.api.api.UserApi;
import cn.tengfeistudio.forum.api.api.WeatherApi;
import cn.tengfeistudio.forum.api.bean.Store;
import cn.tengfeistudio.forum.api.bean.Weather;
import cn.tengfeistudio.forum.utils.Constants;
import cn.tengfeistudio.forum.utils.NetConfig;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * '整个网络通信服务的启动控制，必须先调用初始化函数才能正常使用网络通信接口
 */
public class RetrofitService {


    //天气api
    private static WeatherApi weatherApi;
    //github api
    private static GithubApi githubApi;
    //用户api
    private static UserApi userApi;
    //帖子api
    private static TopicApi topicApi;
    //评论/回复api
    private static CommentApi commentApi;
    //活动api
    private static ActivityApi activityApi;
    //收藏
    private static CollectionApi collectionApi;

    private RetrofitService() {
        throw new AssertionError();
    }

    /**
     * 初始化网络通信服务
     */
    public static void init() {
        Cache cache = new Cache(new File(App.getContext().getCacheDir(), "HttpCache"),
                1024 * 1024 * 100);
        OkHttpClient client = new OkHttpClient.Builder().cache(cache)
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();



        /** 用户base_url */
        Retrofit retrofit=new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.BASE_USER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        userApi=retrofit.create(UserApi.class);

        /** 收藏base_user */
        retrofit=new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.BASE_COLLECTION)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        collectionApi=retrofit.create(CollectionApi.class);

        /** 帖子base_url */
        retrofit=new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.BASE_TOPIC)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        topicApi=retrofit.create(TopicApi.class);

        /** 评论/回复base_url */
        retrofit=new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.BASE_COMMENT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        commentApi=retrofit.create(CommentApi.class);

        /** 活动 */
        retrofit=new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.BASE_ACTIVITY)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        activityApi=retrofit.create(ActivityApi.class);

        /** 天气 */
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.GET_WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);

        /** 版本 */
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(NetConfig.GITHUB_GET_RELEASE + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        githubApi = retrofit.create(GithubApi.class);

    }


    /**************************************             API             **************************************/

    /**
     * 获取天气状况
     */
    public static Observable<Weather> getWeather(String cityCode) {
        return weatherApi.getWeather(cityCode)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取新的Token
     */
    private static final String ERROR_TOKEN = "error_token";
    private static final String ERROR_RETRY = "error_retry";
    public static Observable<String> getNewToken() {
        return Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
                String requestBody = "";
                Request request = new Request.Builder()
                        //.url(NetConfig.BASE_GETNEWTOKEN_PLUS)
                        .url(NetConfig.BASE_REFRESH_TOKEN)
                        .header(Constants.AUTHORIZATION, Store.getInstance().getToken())
                        .post(RequestBody.create(mediaType, requestBody))
                        .build();
                Log.e("print","发起Token请求");
                return Observable.just(client.newCall(request).execute().body().string());
            }
        })
                // Token判断
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) {
                                JSONObject obj = JSON.parseObject(s);
                                if (obj.getInteger("code") != Constants.RETURN_CONTINUE) {
                                    emitter.onError(new Throwable(ERROR_RETRY));
                                } else {
                                    //String token = obj.getString("result");
                                    String token=obj.getJSONObject("data").getString("token");
                                    Store.getInstance().setToken(token);
                                    emitter.onNext(token);
                                }
                            }
                        });
                    }
                })
                // flatMap若onError进入retrywhen，否则onNext()
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    private int mRetryCount = 0;

                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) {
                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                if (mRetryCount++ < 3 && throwable.getMessage().equals(ERROR_TOKEN))
                                    return Observable.error(new Throwable(ERROR_RETRY));
                                mRetryCount = 0;
                                return Observable.error(throwable);
                            }
                        });
                    }
                });
    }

    /**
     * 登陆操作
     * @param email
     * @param pwd
     * @return
     */
    public static Observable<ResponseBody> doLogin(String email, String pwd){
         return   userApi.doLogin(email,pwd)
           .subscribeOn(Schedulers.io())
           .unsubscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 获取用户个人信息
     */
   public static Observable<ResponseBody> getUserDetails(){
       return userApi.getUserDetails(Store.getInstance().getToken())
               .subscribeOn(Schedulers.io())
               .unsubscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread());
   }

    /**
     * 修改用户信息
     * @param infoType
     * @param info
     * @return
     */
    public static Observable<ResponseBody> modifyUserInfo(String infoType,String info){
        return userApi.modifyUserInfo(Store.getInstance().getToken(),infoType,info)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 已经收藏的活动
     * @return
     */
    public static Observable<ResponseBody> getCollectionActivity(){
        return collectionApi.getCollectActivity(Store.getInstance().getToken())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 收藏活动
     * @param activiytId
     * @return
     */
    public static Observable<ResponseBody> collectActivity(int activiytId){
        return collectionApi.collectActivity(Store.getInstance().getToken(),activiytId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 取消收藏活动
     * @param activiytId
     * @return
     */
    public static Observable<ResponseBody> cancelCollectActivity(int activiytId){
        return collectionApi.cancelCollectActivity(Store.getInstance().getToken(),activiytId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 用户收藏的活动列表
     * @param page
     * @return
     */
    public static Observable<ResponseBody> collectActivityList(int page){
        return collectionApi.collectActivityList(Store.getInstance().getToken(),page)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 获取app版本信息
     */
    public static Observable<ResponseBody> getRelease() {
        return githubApi.getVersion(NetConfig.GITHUB_GET_RELEASE)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 发送帖子
     */
      public static Observable<ResponseBody> sendTopic(String title,String content,String theme){
          return topicApi.sendTopic(Store.getInstance().getToken(),title,content,theme)
                  .subscribeOn(Schedulers.io())
                  .unsubscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread());
      }


    /**
     * 删除帖子/举报帖子
     * @param topicId
     * @return
     */
    public static Observable<ResponseBody> deleteTopic(int topicId,int flag){
        return topicApi.deleteTopicByTopicId(Store.getInstance().getToken(),topicId,flag)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 点赞
     * @param topicId
     * @return
     */
    public static Observable<ResponseBody> praiseTopic(int topicId){
        return topicApi.praiseTopic(Store.getInstance().getToken(),topicId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 获取帖子
     */
    public static Observable<ResponseBody> getPost(int id){
        return topicApi.getTopicByTopicId(id)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<ResponseBody> getTopicByCommentId(int commentId){
        return topicApi.getTopicByCommentId(commentId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取帖子详情列表，根据activity获取的POSTID从服务器获取[回复对象含User对象]详情
     */
    public static Observable<ResponseBody> getCommentListData(int id){
        return commentApi.getCommentsByTopicId(id)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 发表评论/回复
     * @param targetId
     * @param content 内容
     * @param topicId 帖子id
     * @return
     */
    public static Observable<ResponseBody> addComment(int targetId,String content,int topicId){
        return commentApi.addComment(Store.getInstance().getToken(),targetId,content,topicId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取未读的回复
     * @return
     */
    public static Observable<ResponseBody> getUnreadReply() {
        return commentApi.getAllUnreadReply(Store.getInstance().getToken())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    /**
     * 删除评论
     * @param commentId
     * @return
     */
    public static Observable<ResponseBody> deleteComment(int commentId,int flag){
        return commentApi.deleteCommentbyCommentId(Store.getInstance().getToken(),commentId,flag)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 分页获取所有活动
     * @param page
     * @return
     */
    public static Observable<ResponseBody> getAllActivity(int page){
        return activityApi.getAllActivity(page)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 根据地点，类型获取所有活动
     * @param page
     * @param place
     * @param type
     * @return
     */
    public static Observable<ResponseBody> selectActivityByPlaceAndType(int page,String place,String type){
        return activityApi.selectActivityByPlaceAndType(page,place,type)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 根据活动Id获取单一活动
     * @param activityId
     * @return
     */
    public static Observable<ResponseBody> getActivity(int activityId){
        return activityApi.getActivity(activityId)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 获取回复帖子
     */
//    public static Observable<ResponseBody> getReplyPostList(long id, int page) {
//        return plusClubApi.getReplyPostList(id, page)
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());
//    }
}
