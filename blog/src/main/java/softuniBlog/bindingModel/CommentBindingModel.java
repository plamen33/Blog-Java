package softuniBlog.bindingModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CommentBindingModel {

    @NotNull
    @Size(min=1, max=210)
    private String commentString;

    public String getCommentString() {
        return commentString;
    }

    public void setCommentString(String commentString) {
        this.commentString = commentString;
    }
}
