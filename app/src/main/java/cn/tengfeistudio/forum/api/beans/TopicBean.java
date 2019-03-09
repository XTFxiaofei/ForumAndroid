package cn.tengfeistudio.forum.api.beans;

public class TopicBean {
    /** 帖子id */
    private int topicId;
    /** 帖子标题 */
    private String title;
    /** 帖子内容 */
    private String content;
    /** 发布时间 */
    private long createTime;
    /** 分类 */
    private String theme;
    /** 发布者 */
    private User userByUserId;
    /** 评论人数 */
    private int commentNumber;
    /** 浏览人数 */
    private int viewNumber;
    /** 是否置顶 */
    private Integer sticky;
    /** 点赞 */
    private String praiseAccountJson;
    /** 内容图片 */
    private String contentPictureJson;

    @Override
    public String toString() {
        return "TopicBean{" +
                "topicId=" + topicId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", theme='" + theme + '\'' +
                ", userByUserId=" + userByUserId +
                ", commentNumber=" + commentNumber +
                ", viewNumber=" + viewNumber +
                ", sticky=" + sticky +
                ", praiseAccountJson='" + praiseAccountJson + '\'' +
                ", contentPictureJson='" + contentPictureJson + '\'' +
                '}';
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public User getUserByUserId() {
        return userByUserId;
    }

    public void setUserByUserId(User userByUserId) {
        this.userByUserId = userByUserId;
    }

    public int getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(int commentNumber) {
        this.commentNumber = commentNumber;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public void setViewNumber(int viewNumber) {
        this.viewNumber = viewNumber;
    }

    public Integer getSticky() {
        return sticky;
    }

    public void setSticky(Integer sticky) {
        this.sticky = sticky;
    }

    public String getPraiseAccountJson() {
        return praiseAccountJson;
    }

    public void setPraiseAccountJson(String praiseAccountJson) {
        this.praiseAccountJson = praiseAccountJson;
    }

    public String getContentPictureJson() {
        return contentPictureJson;
    }

    public void setContentPictureJson(String contentPictureJson) {
        this.contentPictureJson = contentPictureJson;
    }
}
