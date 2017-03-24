package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import softuniBlog.bindingModel.CommentBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Comment;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CommentRepository;
import softuniBlog.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CommentController {
    @Autowired
     ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;


    @RequestMapping(value="/comment/list", method = RequestMethod.GET)
    public String list(Model model){
        model.addAttribute("view", "comment/list");
        List<Comment> comments = this.commentRepository.findAll();
        comments = comments.stream().sorted(Comparator.comparing(Comment::getId)).collect(Collectors.toList());
        Collections.reverse(comments);  // we sort ascending and do a reverse
        //        ZonedDateTime d = ZonedDateTime.parse(b, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User entityUser = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", entityUser);
        }

        model.addAttribute("comments", comments);
        return "base-layout";
    }

    @GetMapping("/comment/create/{id}")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model, @PathVariable Integer id){    // public String create(@PathVariable Integer id, Model model){
        Article article = this.articleRepository.findOne(id);
        User author = article.getAuthor();
        //model.addAttribute("id", id);
        model.addAttribute("view", "comment/create");
        model.addAttribute("article", article);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        return "base-layout";
    }
    @PostMapping("/comment/create/{id}")
    public String createProcess(CommentBindingModel commentBindingModel, @PathVariable Integer id){
        Article article = this.articleRepository.findOne(id);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());
        String date = (ZonedDateTime.now()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)).toString();
        Comment comment=new Comment(
                date,
                commentBindingModel.getCommentString(),
                article,
                user
        );
        this.commentRepository.saveAndFlush(comment);
        return "redirect:/article/"+id;
    }

    @GetMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.commentRepository.exists(id)){
            return "redirect:/";
        }
        Comment comment = this.commentRepository.findOne(id);
        if(!this.isUserAdmin(comment)){  // only admin or author can edit certain comments - avoid hacking the edit functionality
            return "redirect:/comment/list";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("view", "comment/edit");
        model.addAttribute("comment", comment);

        return "base-layout";
    }
    @PostMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, CommentBindingModel commentBindingModel){
      if(!this.commentRepository.exists(id)){
          return "redirect:/";
      }
        Comment comment = this.commentRepository.findOne(id);
        Article article = this.articleRepository.findOne(comment.getArticle().getId());
        if(!this.isUserAdmin(comment)){  // only admin or author can edit certain comments - avoid hacking the edit functionality
            return "redirect:/comment/list";
        }
        comment.setText(commentBindingModel.getCommentString());
//        comment.setArticle(article);
//        comment.setDate("");
//        comment.setArticle(article);
//        comment.setUser(article.getAuthor());
//        comment.setId(comment.getId());

        this.commentRepository.saveAndFlush(comment);
        return "redirect:/comment/list";
    }

    @GetMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/";
        }
        Comment comment = this.commentRepository.findOne(id);
        if (!isUserAdmin(comment)) {
            return "redirect:/comment/list";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("comment", comment);
        model.addAttribute("view", "comment/delete");
        return "base-layout";
    }

    @PostMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, CommentBindingModel commentBindingModel) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/";
        }
        Comment comment = this.commentRepository.findOne(id);
        if (!isUserAdmin(comment)) {
            return "redirect:/comment/list";
        }
        this.commentRepository.delete(comment);
        return "redirect:/comment/list";
    }

    @Transient
    public boolean isUserAdmin(Comment comment) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isAdmin() || userEntity.isCommentAuthor(comment);
    }
}
