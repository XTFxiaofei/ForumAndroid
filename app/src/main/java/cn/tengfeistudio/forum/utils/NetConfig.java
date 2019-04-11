package cn.tengfeistudio.forum.utils;

public class NetConfig {
    public static final String User_Agent_KEY = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
    // VPN 登陆官网
    public static final String vpnSigninURL = "https://webvpn.lsu.edu.cn/users/sign_in";




    // 教务系统主页基地址 - 外/内网
    public static final String BASE_EDU_HOST_ME = "https://jwgl.webvpn.lsu.edu.cn/xs_main.aspx?xh=";

    // 验证码获取 - 外/内网
    public static final String CHECKIMG_URL_EDU = "https://jwgl.webvpn.lsu.edu.cn/CheckCode.aspx";

    // 验证码获取 - 内网
    public static final String CHECKING_URL_IN = "http://jwgl.lsu.edu.cn/CheckCode.aspx";

    // 二次get请求登出 URL - 外/内网
    public static final String GET_LOGOUT_URL_EDU = "https://jwgl.webvpn.lsu.edu.cn/logout.aspx";

    // 二次get请求登出 URL - 内网
    public static final String GET_LOGOUT_URL_IN = "https://jwgl.lsu.edu.cn/logout.aspx";

    // 查询各种信息基地址 - 外/内网
    public static final String BASE_EDU_GETINFO_EDU = "https://jwgl.webvpn.lsu.edu.cn/xskbcx.aspx";

    // 查询各种信息基地址 - 内网
    public static final String BASE_EDU_GETINFO_IN = "https://jwgl.lsu.edu.cn/xskbcx.aspx";

    // 信息门户登陆主页 - 外/内网
    public static final String BASE_ECARD_PLUS = "https://ca.webvpn.lsu.edu.cn/zfca/login";

    // 信息门户登陆主页 - 内网
    public static final String BASE_ECARD_IN = "http://ca.lsu.edu.cn/zfca/login";

    // 信息门户登陆请求 - 外/内网
    public static final String ECARD_LOGIN_PLUS = "https://ca.webvpn.lsu.edu.cn/zfca/login";

    // 获取天气api
    public static final String GET_WEATHER_URL = "http://www.weather.com.cn/data/cityinfo/";

























    /** ----------- 我的url地址 ------------------ */




    /** github版本更新地址 */
    public static final String GITHUB_GET_RELEASE="https://api.github.com/repos/XTFxiaofei/ForumAndroid/releases/latest";

    public static final String BASE_DOMAIN="http://192.168.191.1:8080/";

    /** 分享web网页的帖子 */
    public static final String SHARE_TOPIC=BASE_DOMAIN+"forum/";

    /** 发帖 **/
    public static final String BASE_TOPIC_INCLUDE_IMAGES=BASE_DOMAIN+"topic/send_topic_image";

    public static final String BASE_USER=BASE_DOMAIN+"user/";

    public static final String BASE_COLLECTION=BASE_DOMAIN+"collection/";

    public static final String BASE_USER_MODIFY_ICON=BASE_USER+"modify";

    /** 获取用户具体信息 */
    public static final String BASE_GET_USER_DETAILS=BASE_USER+"get_user_details";

    /** 获取其他人的具体信息 */
    public static final String BASE_GET_OTHER_DETAILS=BASE_USER+"get_other_details/";

    /** 注册url */
    public static final String BASE_USER_REGISTER=BASE_USER+"register";

    /** 重置密码 */
    public static final String BASE_RESET_PASSWORD=BASE_USER+"reset_password";

    /** 重新获取token */
    public static final String BASE_REFRESH_TOKEN=BASE_USER+"refresh";

    public static final String BASE_TOPIC=BASE_DOMAIN+"topic/";

    /** 通过类型查找帖子 */
    public static final String BASE_ALL_TOPIC=BASE_TOPIC+"get_topics_byTheme";

    /** 评论 */
    public static final String BASE_COMMENT=BASE_DOMAIN+"comment/";

    /** 活动 */
    public static final String BASE_ACTIVITY=BASE_DOMAIN+"activity/";

    public static final String BASE_SCHOOL=BASE_DOMAIN+"schooldata/";

    /** 课表url */
    public static final String BASE_SCHEDULE=BASE_SCHOOL+"get_schedule";

    public static final String BASE_EDU_GDUFE="http://jwxt.gdufe.edu.cn/jsxsd/";

    /** 安装包下载地址 */
    public static final String APK_DOWNLOAD_URL="https://github.com/XTFxiaofei/ForumAndroid/releases/download/"+Constants.version_name+"/app-debug.apk";


    /** 连接websocket */
    public static final String CONNECT_WEBSOCKET_URL="ws://192.168.191.1:8080/cheat/websocket";

    /** 拉取未读回复 */
    public static final String RECEIVE_REPLY=BASE_COMMENT+"pull_unread_reply";

    /** 拉取新的活动 */
    public static final String RECEIVE_ACTIVITY="";
}
