package cn.tengfeistudio.forum.api.beans;


import java.util.List;

public class CommentBean {

   /** 评论 */
   Comment comment;
   /** 回复 */
   List<Comment> replyList;

   public Comment getComment() {
      return comment;
   }

   public void setComment(Comment comment) {
      this.comment = comment;
   }

   public List<Comment> getReplyList() {
      return replyList;
   }

   public void setReplyList(List<Comment> replyList) {
      this.replyList = replyList;
   }

   @Override
   public String toString() {
      return "CommentBean{" +
              "comment=" + comment +
              ", replyList=" + replyList +
              '}';
   }
}
