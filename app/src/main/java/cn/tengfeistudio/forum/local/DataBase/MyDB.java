package cn.tengfeistudio.forum.local.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.tengfeistudio.forum.api.beans.ActivityBean;
import cn.tengfeistudio.forum.api.beans.Course;
import cn.tengfeistudio.forum.api.beans.User;

public class MyDB {

    /**
     * 浏览 课程表 表
     */
    static final String TABLE_READ_SCHEDULE = "edu_schedule_lists";
    /**
     * 用户本人
     */
    static final String TABLE_USER_INFO = "user_info";

    /**
     * 素拓活动
     */
    static final String TABLE_EXTENTSION_ACTIVITY="extension_activity_lists";

    private SQLiteDatabase db = null;

    private Context context;

    public MyDB(Context context) {
        this.context = context;
        this.db = new SQLiteHelper(context).getWritableDatabase();
    }

    private SQLiteDatabase getDb() {
        if (this.db == null || !this.db.isOpen()) {
            this.db = new SQLiteHelper(context).getWritableDatabase();
        }
        return this.db;
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return format.format(curDate);
    }

    // 处理单个点击事件 判断是更新还是插入
    public void handSingleReadSchedule(Course course) {
        if (isCourseExist(course.getCid())) {
            updateSchedule(course);
        } else {
            insertSchedule(course);
        }
    }
    //存在活动则更新，否则插入
    public void handSingleReadActivity(ActivityBean bean){
        if(isActivityExist(bean.getActivityName())){
            updateActivityBean(bean);
        }else{
            insertActivityBean(bean);
        }
    }

    // 处理单个点击事件 判断是更新还是插入
    public void handSingleReadSchedule(User user) {
        if (isUserExist(user.getUserId())) {
            updateUser(user);
        } else {
            insertUser(user);
        }
    }

    public List<ActivityBean> getActivityBean(){
        getDb();
        List<ActivityBean> datas=new ArrayList<>();
        String sql="SELECT * FROM "+TABLE_EXTENTSION_ACTIVITY;
        Cursor result=this.db.rawQuery(sql,null);
        for(result.moveToFirst();!result.isAfterLast();result.moveToFirst()){
            //aid,auserid,atitle,alogoimage,acreatetime,acontent,aplace,atype,alevel,asponsor,
            // atarget,atypenickname,activityTime,acontentpicture,flag,aupdatetime,activityName
            int id=result.getInt(0);
            int userid=result.getInt(1);
            String title=result.getString(2);
            String logoImage=result.getString(3);
            long createTime=result.getLong(4);
            String content=result.getString(5);
            String place=result.getString(6);
            String type=result.getString(7);
            String level=result.getString(8);
            String sponsor=result.getString(9);
            String target=result.getString(10);
            String typeNickName=result.getString(11);
            String activityTime=result.getString(12);
            String contentPicture=result.getString(13);
            int flag=result.getInt(14);
            long updateTime=result.getLong(15);
            String activityName=result.getString(16);
            datas.add(new ActivityBean(id,userid,title,logoImage,createTime,content,place,type,level,sponsor,target,typeNickName,activityTime,contentPicture,flag,updateTime,activityName));
        }
        result.close();
        this.db.close();
        return datas;
    }

    // 获取课程表的课程信息
    public List<Course> getSchedule() {
        getDb();
        List<Course> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_READ_SCHEDULE;
        Cursor result = this.db.rawQuery(sql, null);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            //cid,cstartsection,cweekday,csumsection,courseName,sectionNumber,teacher,place,weekNumber,create_time
            int cid = result.getInt(0);
            int startsection = result.getInt(1);
            int weekday = result.getInt(2);
            int sumsection=result.getInt(3);
            String courseName = result.getString(4);
            String sectionNumber = result.getString(5);
            String teacher = result.getString(6);
            String place = result.getString(7);
            String weekNumber=result.getString(8);
            String create_time = result.getString(9);
            datas.add(new Course(cid,startsection,weekday,sumsection,courseName,sectionNumber,teacher,place,weekNumber,create_time));
        }
        result.close();
        this.db.close();
        return datas;
    }

    // 获取用户本地缓存头像
    public String getUserAvatarPath(long uid) {
        getDb();
        User user = new User();
        String avatarPath = "";
        String sql = "SELECT * FROM " + TABLE_USER_INFO;
        Cursor result = this.db.rawQuery(sql, null);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            if (result.getLong(0) == uid) {
                avatarPath = result.getString(10);
                break;
            }
        }
        result.close();
        this.db.close();
        return avatarPath;
    }

    // 获取用户信息
    public User getUser(int uid) {
        getDb();
        User user = new User();
        String sql = "SELECT * FROM " + TABLE_USER_INFO;
        Cursor result = this.db.rawQuery(sql, null);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            if (result.getInt(0) == uid) {
                user.setNickname(result.getString(1));
                user.setEmail(result.getString(2));
                user.setStudentCard(result.getString(3));
                user.setAccount(result.getString(4));
                user.setPhone(result.getLong(5));
                user.setCreateTime(result.getLong(6));
                user.setUpdateTime(result.getLong(7));
                user.setRole(result.getString(8));
                user.setIcon(result.getString(9));
                user.setSex(result.getInt(10));
                break;
            }
        }
        result.close();
        this.db.close();
        return user;
    }


    // 插入操作
    private void insertSchedule(Course course) {
        getDb();
        String sql = "INSERT INTO " + TABLE_READ_SCHEDULE + " (cid,cstartsection,cweekday,csumsection,courseName,sectionNumber,teacher,place,weekNumber,create_time)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?)";
        String create_time = getTime();
        Object args[] = new Object[]{course.getCid(), course.getStartSection(), course.getWeekDay(),course.getSumSection(),
                course.getCourseName(), course.getSectionNumber(), course.getTeacher(),
                course.getPlace(), course.getWeekNumber(), create_time};
        this.db.execSQL(sql, args);
        this.db.close();
    }


    //插入活动
    private void insertActivityBean(ActivityBean bean){
        getDb();
        String sql="INSERT INTO "+TABLE_EXTENTSION_ACTIVITY+"(aid,auserid,atitle,alogoimage,acreatetime,acontent,aplace,atype,alevel,asponsor," +
                "atarget,atypenickname,activityTime,acontentpicture,aflag,aupdatetime,activityname)"
                +"  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object args[]=new Object[]{bean.getActivityId(),bean.getUserId(),bean.getTitle(),bean.getLogoImage(),bean.getCreateTime(),bean.getContent(),
                bean.getPlace(),bean.getType(),bean.getLevel(),bean.getSponsor(),bean.getTarget(),bean.getTypeNickname(),bean.getActivityTime(),
                bean.getContentPicture(),bean.getFlag(),bean.getUpdateTime(),bean.getActivityName()};
        this.db.execSQL(sql,args);
        this.db.close();
    }

    /** 插入用户信息到数据库 */
    private void insertUser(User user) {
        getDb();
        String sql = "INSERT INTO " + TABLE_USER_INFO + " (uid,uname,uemail,ustudentid,uaccount,uphone,ucreated,uupdated,urole,uavatarsrc,usex)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        Object args[] = new Object[]{user.getUserId(), user.getNickname(), user.getEmail(), user.getStudentCard(),
                user.getAccount(), user.getPhone(), user.getCreateTime(), user.getUpdateTime(),
                user.getIcon(), user.getSex()};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    // 更新操作
    private void updateSchedule(Course course) {
        getDb();
        String create_time = getTime();
        //cid,cstartsection,cweekday,csumsection,courseName,sectionNumber,teacher,place,weekNumber,create_time
        String sql = "UPDATE " + TABLE_READ_SCHEDULE + " SET cstartsection=?,cweekday=?,csumsection=?,courseName=?,sectionNumber=?,teacher=?,place=?,weekNumber=?,create_time=? WHERE cid=?";
        Object args[] = new Object[]{course.getStartSection(), course.getWeekDay(),course.getSumSection(), course.getCourseName(),
                course.getSectionNumber(), course.getTeacher(), course.getPlace(),
                course.getWeekNumber(), create_time, course.getCid()};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    //更新素拓活动
    private void updateActivityBean(ActivityBean bean){
        getDb();
        String sql="UPDATE "+TABLE_EXTENTSION_ACTIVITY+" SET aid=?,auserid=?,atitle=?,alogoimage=?,acreatetime=?,acontent=?,aplace=?,atype=?,alevel=?,asponsor=?,"
                + "atarget=?,atypenickname=?,activitytime=?,acontentpicture=?,aflag=?,aupdatetime=?,activityname=? WHERE activityname=?";
        Object args[]=new Object[]{bean.getActivityId(),bean.getUserId(),bean.getTitle(),bean.getLogoImage(),bean.getCreateTime(),bean.getContent(),
                bean.getPlace(),bean.getType(),bean.getLevel(),bean.getSponsor(),bean.getTarget(),bean.getTypeNickname(),bean.getActivityTime(),
                bean.getContentPicture(),bean.getFlag(),bean.getUpdateTime(),bean.getActivityName()};
        this.db.execSQL(sql,args);
        this.db.close();
    }
    //更新用户信息
    private void updateUser(User user) {
        getDb();
        String sql = "UPDATE " + TABLE_USER_INFO + " SET uid=?,uname=?,uemail=?,ustudentid=?,uaccount=?,uphone=?,ucreated=?,uupdated=?,urole=?,uavatarsrc=?,usex=? WHERE uid=?";
        Object args[] = new Object[]{user.getUserId(), user.getNickname(), user.getEmail(), user.getStudentCard(),
                user.getAccount(), user.getPhone(), user.getCreateTime(), user.getUpdateTime(),
                user.getIcon(), user.getSex()};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    private boolean isCourseExist(int cid) {
        getDb();
        String sql = "SELECT cid FROM " + TABLE_READ_SCHEDULE + " where cid = ?";
        String args[] = new String[]{String.valueOf(cid)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.close();
        this.db.close();
        return count != 0;
    }
    //判断活动是否存在
    private boolean isActivityExist(String activityName){
        getDb();
        String sql="SELECT activityname FROM "+TABLE_EXTENTSION_ACTIVITY+" WHERE activityname=?";
        String args[]=new String[]{activityName};
        Cursor result=db.rawQuery(sql,args);
        int count=result.getCount();
        result.close();;
        this.db.close();
        return  count!=0;
    }

    public boolean isUserExist(int uid) {
        getDb();
        String sql = "SELECT uid FROM " + TABLE_USER_INFO + " where uid = ?";
        String args[] = new String[]{String.valueOf(uid)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.close();
        this.db.close();
        return count != 0;
    }

    public boolean isScheduleExist() {
        getDb();
        String sql = "SELECT cid FROM " + TABLE_READ_SCHEDULE;
        Cursor result = db.rawQuery(sql, null);
        int count = result.getCount();
        Log.e("DATABASE", "count == " + count);
        this.db.close();
        return count != 0;
    }
    //判断是否存在活动列表
    public boolean isActivityListExist(){
        getDb();
        String sql="SELECT activityname FROM "+TABLE_EXTENTSION_ACTIVITY;
        Cursor result=db.rawQuery(sql,null);
        int count=result.getCount();
        this.db.close();
        return count!=0;
    }

    public void clearSchedule() {
        getDb();
        String sql = "DELETE FROM " + TABLE_READ_SCHEDULE;
        this.db.execSQL(sql);
        this.db.close();
    }

    public void clearActivityList(){
        getDb();
        String sql="DELETE FROM "+TABLE_EXTENTSION_ACTIVITY;
        this.db.execSQL(sql);
        this.db.close();
    }

}
