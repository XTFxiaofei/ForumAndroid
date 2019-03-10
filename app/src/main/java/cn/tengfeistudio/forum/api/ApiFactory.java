package cn.tengfeistudio.forum.api;


import cn.tengfeistudio.forum.api.api.PlusClubApi;
import cn.tengfeistudio.forum.api.api.WeatherApi;


public class ApiFactory {
    protected static final Object monitor = new Object();
    static PlusClubApi plusClubApi = null;
    static WeatherApi weatherApi = null;

}
