package softuniBlog.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    private Integer id;

    private String email;

    private String fullName;

    private String password;

    private String picture;

    private Set<Role> roles;

    private Set<Article> articles;

    private Set<Comment> comments;

    public User(String email, String fullName, String password, String picture) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.picture = picture;

        this.roles = new HashSet<>();
        this.articles = new HashSet<>();
    }

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "email", unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "fullName", nullable = false)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "password", length = 60, nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "picture", unique = true)
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @OneToMany(mappedBy = "author")
    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Transient
    public boolean isAdmin() {
        return this.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    @OneToMany(mappedBy = "user")
    public Set<Comment> getComments() {return comments;}

    public void setComments(Set<Comment> comments){this.comments = comments;}

    @Transient
    public boolean isAuthor(Article article) {
        return Objects.equals(this.getId(),
                article.getAuthor().getId());
    }

    @Transient
    public boolean isAuthorComment(Comment comment) {
        return Objects.equals(this.getId(),
                comment.getUser().getId());
    }
    @Transient
    public boolean isCommentAuthor (Comment comment) {
        return Objects.equals(this.getId(), comment.getUser().getId());
    }
}