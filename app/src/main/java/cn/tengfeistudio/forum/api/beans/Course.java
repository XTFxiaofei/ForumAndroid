package cn.tengfeistudio.forum.api.beans;


public class Course {

    /**
     * 数据库primary key 在初始化的时候根据顺序分配 从0开始
     */
    private int cid;
    /**
     * weekDay星期几
     */
    private int weekDay;
    /**
     * startSection开始节数
     */
    private int startSection;

    /**
     * 每次多少节
     */
    private int sumSection=2;
    /**
     * courseName课程名
     */
    private String courseName;
    /**
     * teacher老师
     */
    private String teacher;
    /**
     * weekNumber第几周
     */
    private String weekNumber;
    /**
     * sectionNumber节数
     */
    private String sectionNumber;
    /**
     * 地点
   */
    private String place;
    /**
     * course 存进数据库的时间
     */
    private String create_time;

    public Course(){
        super();
    }

    public Course(int cid, int startSection, int weekDay, int sumSection, String courseName,String sectionNumber, String teacher,  String place, String weekNumber, String create_time) {
        this.cid = cid;
        this.weekDay = weekDay;
        this.startSection = startSection;
        this.sumSection = sumSection;
        this.courseName = courseName;
        this.teacher = teacher;
        this.weekNumber = weekNumber;
        this.sectionNumber = sectionNumber;
        this.place = place;
        this.create_time = create_time;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public int getStartSection() {
        return startSection;
    }

    public void setStartSection(int startSection) {
        this.startSection = startSection;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(String weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public int getSumSection() {
        return sumSection;
    }

    public void setSumSection(int sumSection) {
        this.sumSection = sumSection;
    }
}
