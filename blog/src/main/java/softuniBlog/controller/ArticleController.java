package softuniBlog.controller;

import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.bindingModel.UserBindingModel;
import softuniBlog.entity.*;
import softuniBlog.entity.User;
import softuniBlog.repository.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CommentRepository commentRepository;

    private HashSet<Tag> findTagsFromString(String tagString){
        HashSet<Tag> tags = new HashSet<>();
        String[] tagNames = tagString.split(",\\s*");

        for (String tagName : tagNames){
            Tag currentTag = this.tagRepository.findByName(tagName);
            if (currentTag==null){
                currentTag=new Tag(tagName);
                this.tagRepository.saveAndFlush(currentTag);
            }
            tags.add(currentTag);
        }
        return tags;
    }

    @GetMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model, ArticleBindingModel articleBindingModel){
        List<Category> categories = this.categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("view", "article/create");
        return "base-layout";
    }

    //    @PostMapping("/article/create")
    @RequestMapping(value = "/article/create", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public String createProcess(@Valid ArticleBindingModel articleBindingModel, BindingResult bindingResult,  RedirectAttributes redirectAttributes, UserBindingModel userBindingModel){
        if (bindingResult.hasErrors()) {
            if(articleBindingModel.getTitle().length()>30){
                redirectAttributes.addFlashAttribute("error", "Article title should not exceed 30 symbols");
                return "redirect:/article/create";
            }
            if(articleBindingModel.getTagString().length()>21){

                redirectAttributes.addFlashAttribute("error", "Tag should not exceed 21 symbols");
                articleBindingModel.setTagString(articleBindingModel.getTagString().substring(0, 21));
                return "redirect:/article/create";
            }
            if(articleBindingModel.getContent().length()>2100){
                redirectAttributes.addFlashAttribute("error", "Content should not exceed 2100 symbols");
                articleBindingModel.setContent(articleBindingModel.getContent().substring(0, 2099));
            }
            if(articleBindingModel.getVideo().length()>100){
                redirectAttributes.addFlashAttribute("error", "Video link should not exceed 100 symbols");
                articleBindingModel.setVideo(articleBindingModel.getVideo().substring(0, 100));
            }


        }
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Article articleEntity = new Article(
                articleBindingModel.getTitle(),
                articleBindingModel.getContent(),
                userEntity,
                category,
                articleBindingModel.getPicture().getOriginalFilename(),
                articleBindingModel.getVideo(),
                tags
        );
        /// add image:
        String root = System.getProperty("user.dir");
        MultipartFile file = userBindingModel.getPicture();

        if (file != null && (file.getSize() > 0 && file.getSize() < 77000)) {
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
            if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                /// add new picture
                String originalFileName = articleEntity.getTitle()+ "-" + file.getOriginalFilename();
                File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\articles\\", originalFileName);

                try {
                    file.transferTo(imageFile);
                    articleEntity.setPicture(originalFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {

            System.out.println("Too Big");
        }

        // add video
        String regex = "^(?:https?\\:\\/\\/)?(?:www\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v\\=))([\\w-]{10,12})(?:[\\&\\?\\#].*?)*?(?:[\\&\\?\\#]t=([\\d]+))?$";
        Pattern pattern = Pattern.compile(regex);
        String videoLink = articleBindingModel.getVideo();

        if (videoLink!=null){
            Matcher matcher = pattern.matcher(videoLink);
            while(matcher.find()){
                String video = matcher.group(1);
                //System.out.println(video);
                String extOptions = matcher.group(2);
                ///System.out.println(extOptions);
                if (extOptions == null || extOptions.equals("")) {
                    articleEntity.setVideo(video);
                    articleEntity.setVideoLink("");
                    //String videoLink1 = articleEntity.getVideo();
                    //if (videoLink1.length() > 100) { articleEntity.setVideoLink(null); }
                }
                else {
                    // articleEntity.setVideo(video + "?start=" + extOptions);
                    articleEntity.setVideo(video);
                    articleEntity.setVideoLink("?start=" + extOptions);
                    //String videoLink2 = articleEntity.getVideo();
                    //if (videoLink2.length() > 100) { articleEntity.setVideoLink(null); }
                }
            }
        }
        this.articleRepository.saveAndFlush(articleEntity);
        return "redirect:/";
    }
    @GetMapping("/article/{id}")
    public String details(Model model, @PathVariable Integer id){

        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }
        if(!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            User entityUser = this.userRepository.findByEmail(principal.getUsername());
            model.addAttribute("user", entityUser);
        }
        Article article = this.articleRepository.findOne(id);
        List<Comment> comments = this.commentRepository.findByArticle(article);
        comments.stream()
                .sorted((object1, object2) -> object1.getId().compareTo(object2.getId()));
        Collections.reverse(comments);

        model.addAttribute("comments", comments);
        model.addAttribute("article", article);
        model.addAttribute("view", "article/details");
        return "base-layout";

    }

    @GetMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);
        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        List<Category> categories = this.categoryRepository.findAll();

        String tagString = article.getTags()
                .stream().map(Tag::getName)
                .collect(Collectors.joining(", "));
        model.addAttribute("tags", tagString);
        model.addAttribute("article", article);
        model.addAttribute("view", "article/edit");
        model.addAttribute("categories", categories);
        return "base-layout";
    }


    @PostMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, ArticleBindingModel articleBindingModel, MultipartFile file){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);
        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());

        article.setTags(tags);
        article.setCategory(category);
        article.setContent(articleBindingModel.getContent());
        article.setTitle(articleBindingModel.getTitle());

        String root = System.getProperty("user.dir");
        file = articleBindingModel.getPicture();

        if (file != null ) {
            if (file.getSize() > 0 && file.getSize() < 77000) {
                String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                    //delete old pic:
                    String oldPic = article.getPicture();
                    if (oldPic != null) {
                        File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\articles\\", oldPic);
                        try {
                            if (oldPicFile.delete()) {
                                System.out.println(oldPicFile.getName() + " is deleted!");
                            } else {
                                System.out.println("Delete operation failed.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ///////

                    /// add new picture
                    String originalFileName = article.getTitle()+ "-" + file.getOriginalFilename();
                    File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\articles\\", originalFileName);

                    try {
                        file.transferTo(imageFile);
                        article.setPicture(originalFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // image type limit
                else{
                    System.out.println("You can upload only images !");
                }
            } // size limit
            else{
                System.out.println("file too big");
            }
        }
        else {
            System.out.println("Invalid file");
        }
        // edit video
        String regex = "^(?:https?\\:\\/\\/)?(?:www\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v\\=))([\\w-]{10,12})(?:[\\&\\?\\#].*?)*?(?:[\\&\\?\\#]t=([\\d]+))?$";
        Pattern pattern = Pattern.compile(regex);
        String videoLink = articleBindingModel.getVideo();

        if (videoLink!=null){
            Matcher matcher = pattern.matcher(videoLink);
            while(matcher.find()){
                String video = matcher.group(1);
                String extOptions = matcher.group(2);
                if (extOptions == null || extOptions.equals("")) {
                    //if (videoLink.length() > 100) { article.setVideoLink(null); }
                    //else{
                    article.setVideo(video);
                    article.setVideoLink("");
                    //}
                }
                else {
                    //if (videoLink.length() > 100) { article.setVideoLink(null); }
                    // else{
                    //article.setVideo(video + "?start=" + extOptions);
                    article.setVideo(video);
                    article.setVideoLink("?start=" + extOptions);
                    //article.setVideoLink("https://www.youtube.com/embed/"+ video + "?start=" + extOptions);
                    //https://www.youtube.com/embed/s39mNwFuQDQ?start=212
                    //}
                }
            }
            if(!matcher.matches()){
                if (videoLink.equals("clear video")) {
                    article.setVideo(null);
                    article.setVideoLink("");
                    //article.setVideoLink(null);
                }
//                else {
//                    if (article.getVideo()==null ||article.getVideo().equals("") ) { article.setVideoLink(null); }
//                    else { article.setVideoLink("https://www.youtube.com/watch?v=" + article.getVideo()); }
//                }

            }
        }


        this.articleRepository.saveAndFlush(article);
        return "redirect:/article/" + article.getId();
    }


    @GetMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);
        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        model.addAttribute("article", article);
        model.addAttribute("view", "article/delete");

        return "base-layout";
    }

    private boolean isUserAuthorOrAdmin(Article article){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin() || userEntity.isAuthor(article);
    }

    @PostMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id){

        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }
        // delete comments
        List<Comment> commentsList = this.commentRepository.findByArticle(article);
        for (Comment comment : commentsList) {
            this.commentRepository.delete(comment);
        }

        //delete article picture:
        String root = System.getProperty("user.dir");
        String oldPic = article.getPicture();
        if (oldPic != null) {
            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\articles\\", oldPic);
            try {
                if (oldPicFile.delete()) {
                    System.out.println(oldPicFile.getName() + " is deleted!");
                } else {
                    System.out.println("Delete operation failed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.articleRepository.delete(article);
        return "redirect:/";
    }


}
