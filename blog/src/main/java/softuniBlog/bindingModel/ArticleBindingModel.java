package softuniBlog.bindingModel;


import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;

public class ArticleBindingModel {
    @NotNull
    @Size(min=1, max=30)
    private String title;
    @NotNull
    @Size(min=1, max=2100)
    private String content;
    @NotNull
    private Integer categoryId;
    @NotNull
    @Size(min=1, max=21)
    private String tagString;
    @NotNull
    @Size(min=1, max=100)
    private String video;
    @NotNull
    private MultipartFile picture;





    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }
}
