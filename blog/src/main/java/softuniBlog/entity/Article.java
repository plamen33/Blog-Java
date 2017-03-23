package softuniBlog.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="articles")
public class Article {

    private Integer id;
    private String title;
    private String content;
    private User author;
    private Category category;
    private String picture;
    private String video;
    private String videoLink;
    private Set<Tag> tags;
    private Integer articleLikes;
    private Integer articleDislikes;
    private String likedUsers;
    private String dislikedUsers;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column (nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "categoryId")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Column(name = "picture")
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Column(name = "video")
    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }
    @ManyToMany()
    @JoinColumn(table = "articles_tags")
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    @Column(name = "articleLikes")
    public Integer getArticleLikes() {
        return articleLikes;
    }

    public void setArticleLikes(Integer articleLikes) {
        this.articleLikes = articleLikes;
    }

    @Column(name = "likedUsers")
    public String getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(String likedUsers) {
        this.likedUsers = likedUsers;
    }

    @Column(name = "articleDislikes")
    public Integer getArticleDislikes() {
        return articleDislikes;
    }

    public void setArticleDislikes(Integer articleDislikes) {
        this.articleDislikes = articleDislikes;
    }

    @Column(name = "dislikedUsers")
    public String getDislikedUsers() {
        return dislikedUsers;
    }

    public void setDislikedUsers(String dislikedUsers) {
        this.dislikedUsers = dislikedUsers;
    }



    public Article(String title, String content, User author, Category category, String picture, String video, HashSet<Tag> tags, Integer articleLikes, String likedUsers, Integer articleDislikes, String dislikedUsers){

        this.title=title;
        this.content=content;
        this.author=author;
        this.category=category;
        this.picture = picture;
        this.tags = tags;
        this.articleLikes = articleLikes;
        this.likedUsers = likedUsers;
        this.articleDislikes = articleDislikes;
        this.dislikedUsers = dislikedUsers;
    }
    public Article(){   }

    public Article(String title, String content, org.springframework.security.core.userdetails.User userEntity){   }

    @Transient
    public Integer likeCount(Set<String> likes){
        return likes.size();
    }

    @Transient
    public String getSummary(){
        return this.getContent().substring(0, this.getContent().length() / 2) + "...";
    }

}
