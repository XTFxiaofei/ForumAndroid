package cn.tengfeistudio.forum.local.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "plusclub.db";

    // 数据库版本，更新版本数据库重建
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //cid,cstartsection,cweekday,csumsection,courseName,sectionNumber,teacher,place,weekNumber,create_time
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + MyDB.TABLE_READ_SCHEDULE + "("
                + "cid INT primary key,"
                + "cstartsection INT NOT NULL,"
                + "cweekday INT NOT NULL,"
                + "csumsection INT NOT NULL,"
                + "courseName VARCHAR(30) NOT NULL,"
                + "sectionNumber VARCHAR(30) NOT NULL,"
                + "teacher VARCHAR(30) NOT NULL,"
                + "place VARCHAR(30) NOT NULL,"
                + "weekNumber VARCHAR(30) NOT NULL,"
                + "create_time DATETIME NOT NULL"
                + ")";
        db.execSQL(sql);
        Log.e("DATABASE", "TABLE_READ_SCHEDULE数据表创建成功");

        //uid,uname,uemail,ustudentid,uaccount,uphone,ucreated,uupdated,urole,uavatarsrc,usex
        String sql2 = "CREATE TABLE " + MyDB.TABLE_USER_INFO + "("
                + "uid LONG primary key,"
                + "uname VARCHAR(30) NOT NULL,"
                + "uemail VARCHAR(50) NOT NULL,"
                + "ustudentid VARCHAR(30),"
                + "uaccount VARCHAR(30),"
                + "uphone VARCHAR(30),"
                + "ucreated VARCHAR(50),"
                + "uupdated VARCHAR(50),"
                + "urole VARCHAR(30),"
                + "uavatarsrc VARCHAR(50),"
                + "uavatarpath VARCHAR(50)"
                + ")";
        db.execSQL(sql2);
        Log.e("DATABASE", "TABLE_USER_INFO数据表创建成功");

        //aid,auserid,atitle,alogoimage,acreatetime,acontent,aplace,atype,alevel,asponsor,
        // atarget,atypenickname,activityTime,acontentpicture,flag,aupdatetime,activityname
        String sql3="CREATE TABLE "+MyDB.TABLE_EXTENTSION_ACTIVITY+"("
                +"aid INT primary key,"
                +"auserid INT,"
                +"atitle VARCHAR(100) NOT NULL,"
                +"alogoimage VARCHAR(255),"
                +"acreatetime LONG,"
                +"acontent VARCHAR,"
                +"aplace VARCHAR(100) NOT NULL,"
                +"atype VARCAHR(100) NOT NULL,"
                +"alevel VARCHAR(100),"
                +"asponsor VARCHAR(100),"
                +"atarget VARCHAR(100),"
                +"atypenickname VARCHAR(100),"
                +"activitytime VARCHAR(100),"
                +"acontentpicture VARCHAR(255),"
                +"aflag INT,"
                +"aupdatetime LONG,"
                +"activityname VARCHAR(100) NOT NULL"
                + ")";
        db.execSQL(sql3);
        Log.e("DATABASE", "TABLE_EXTENTSION_ACTIVITY数据表创建成功");
    }

    /**
     * 数据库更新函数，当数据库更新时会执行此函数
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + MyDB.TABLE_READ_SCHEDULE;
        db.execSQL(sql);

        String sql2 = "DROP TABLE IF EXISTS " + MyDB.TABLE_USER_INFO;
        db.execSQL(sql2);

        String sql3="DROP TABLE IF EXISTS "+MyDB.TABLE_EXTENTSION_ACTIVITY;
        db.execSQL(sql3);

        this.onCreate(db);
        Log.e("DATABASE", "数据库已更新");
    }
}
