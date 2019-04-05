package cn.tengfeistudio.forum.api.beans;

import java.io.Serializable;

public class ActivityBean implements Serializable{
    private int activityId;
    private Integer userId;
    private String title;
    private String logoImage;
    private Long createTime;
    private String content;
    private String place;
    private String type;
    private String level;
    private String sponsor;
    private String target;
    private String typeNickname;
    private String activityTime;
    private String contentPicture;
    private Integer flag;
    private Long updateTime;
    private String activityName;


    @Override
    public String toString() {
        return "ActivityBean{" +
                "activityId=" + activityId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", logoImage='" + logoImage + '\'' +
                ", createTime=" + createTime +
                ", content='" + content + '\'' +
                ", place='" + place + '\'' +
                ", type='" + type + '\'' +
                ", level='" + level + '\'' +
                ", sponsor='" + sponsor + '\'' +
                ", target='" + target + '\'' +
                ", typeNickname='" + typeNickname + '\'' +
                ", activityTime='" + activityTime + '\'' +
                ", contentPicture='" + contentPicture + '\'' +
                ", flag=" + flag +
                ", updateTime=" + updateTime +
                ", activityName='" + activityName + '\'' +
                '}';
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTypeNickname() {
        return typeNickname;
    }

    public void setTypeNickname(String typeNickname) {
        this.typeNickname = typeNickname;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    public String getContentPicture() {
        return contentPicture;
    }

    public void setContentPicture(String contentPicture) {
        this.contentPicture = contentPicture;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}
