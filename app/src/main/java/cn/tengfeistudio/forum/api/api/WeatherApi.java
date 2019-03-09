package cn.tengfeistudio.forum.api.api;



import cn.tengfeistudio.forum.api.bean.Weather;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WeatherApi {
    // 获取天气信息
    @GET("{cityCode}.html")
    Observable<Weather> getWeather(@Path("cityCode") String cityCode);
}
