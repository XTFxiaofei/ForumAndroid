package cn.tengfeistudio.forum.utils;

/**
 * @Description: 常量
 * @Author: gre_yu@163.com
 * @Date: Created in 0:51 2018/2/1
 */
public class Constants {
    /**
     * 当前app版本号
     */
    public static final String version_name="1.1.1";

    public static final int version_code=1;
    /**
     * 存储当前登录用户id的字段名
     */
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    /**
     * token有效期（小时）
     */
    public static final int TOKEN_EXPIRES_HOUR = 72;

    /**
     * 存放Authorization的header字段
     */
    public static final String AUTHORIZATION = "authorization";

    /**
     * 默认用户等级
     */
    public static final int DEFAULT_USER_LEVEL=1;

    /**
     * 默认用户状态: :1正常 0封号 2禁言 3注销
     */
    public static final int DEFAULT_USER_STATUS=1;

    /**
     * flag=1 表示正常 flag=0表示逻辑删除
     */
    public static final int DEFAULT_FLAG=1;

    public static final String DEFAULT_USER_ROLE="user";

    public static final int DEFAULT_EACH_PAGE_SIZE=8;
    /** 删除状态位 */
    public static final int DELETE_FLAG=0;
    /** 举报状态位 */
    public static final int REPORT_FLAG=2;

    /** -----------   -------------------- */
    /** token过期返回 */
    public static final int TOKEN_OVERDUE=50011;
    /** return 返回正常并继续 */
    public static final int RETURN_CONTINUE=100;
   /** 默认查找第几页 */
   public static final int DEFAULT_PAGE_NUMBER=0;
    /** 逗号 */
    public static final String COMMA=",";











}
