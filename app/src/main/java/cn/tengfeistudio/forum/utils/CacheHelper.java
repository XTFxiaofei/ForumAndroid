package cn.tengfeistudio.forum.utils;


import cn.tengfeistudio.forum.App;

public class CacheHelper {
    // 缓存基地址
    public static final String CACHE_BASE_PATH = App.getContext().getCacheDir().getPath();

    // 头像缓存地址
    public static final String CACHE_AVATAR = CACHE_BASE_PATH + "/avatar";
}
