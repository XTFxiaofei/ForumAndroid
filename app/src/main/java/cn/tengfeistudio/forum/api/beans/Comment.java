package cn.tengfeistudio.forum.api.beans;


import android.support.annotation.NonNull;

public class Comment implements Comparable<Comment>{
    private int commentId;
    private Integer fromId;
    private String fromNickname;
    private Integer toId;
    private String toNickename;
    private String commentType;
    private Integer targetId;
    private String commentContent;
    private Long createTime;
    private String commentPicture;
    private String targetContent;
    private User userByFromId;

    @Override
    public int compareTo(@NonNull Comment comment) {
        if(this.commentId>comment.getCommentId()){
            return 1;
        }
        if(this.commentId<comment.getCommentId()){
            return -1;
        }
        return 0;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public String getToNickename() {
        return toNickename;
    }

    public void setToNickename(String toNickename) {
        this.toNickename = toNickename;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getCommentPicture() {
        return commentPicture;
    }

    public void setCommentPicture(String commentPicture) {
        this.commentPicture = commentPicture;
    }

    public String getTargetContent() {
        return targetContent;
    }

    public void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
    }

    public User getUserByFromId() {
        return userByFromId;
    }

    public void setUserByFromId(User userByFromId) {
        this.userByFromId = userByFromId;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", fromId=" + fromId +
                ", fromNickname='" + fromNickname + '\'' +
                ", toId=" + toId +
                ", toNickename='" + toNickename + '\'' +
                ", commentType='" + commentType + '\'' +
                ", targetId=" + targetId +
                ", commentContent='" + commentContent + '\'' +
                ", createTime=" + createTime +
                ", commentPicture='" + commentPicture + '\'' +
                ", targetContent='" + targetContent + '\'' +
                ", userByFromId=" + userByFromId +
                '}';
    }
}
