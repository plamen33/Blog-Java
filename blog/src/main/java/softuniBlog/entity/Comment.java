package softuniBlog.entity;

import javax.persistence.*;

@Entity
@Table(name="comments")
public class Comment {

    private Integer id;
    private String date;
    private String text;
    private Article article;
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    @Column(nullable = false)
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    @Column(nullable = false)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    @ManyToOne
    @JoinColumn(name="articleId")
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
    @ManyToOne
    @JoinColumn(name="userId")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public Comment(){

    }
    public Comment(String date, String text, Article article, User user ){
        this.date=date;
        this.text = text;
        this.article=article;
        this.user=user;

    }

}
