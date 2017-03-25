package softuniBlog.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import softuniBlog.bindingModel.CategoryBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Category;
import softuniBlog.entity.Comment;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CategoryRepository;
import softuniBlog.repository.CommentRepository;
import softuniBlog.repository.UserRepository;
import softuniBlog.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    private NotificationService notifyService;

    @GetMapping("/")
    public String list(Model model){
        model.addAttribute("view", "admin/category/list");
        List<Category> categories = this.categoryRepository.findAll();
        categories = categories.stream()
                .sorted(Comparator.comparingInt(Category::getId))
                .collect(Collectors.toList());
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        return "base-layout";
    }

    @GetMapping("/create")
    public String create (Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("view", "admin/category/create");
        return "base-layout";
    }

    @PostMapping("/create")
    public String createProcess(CategoryBindingModel categoryBindingModel, RedirectAttributes redirectAttributes, MultipartFile file){

        if(StringUtils.isEmpty(categoryBindingModel.getName())){
            redirectAttributes.addFlashAttribute("error", "Category should not be empty");
            return "redirect:/admin/categories/create";
        }

        Category category = new Category(categoryBindingModel.getName());

        /// adding category Image
        String root = System.getProperty("user.dir");
        file = categoryBindingModel.getPicture();

        if (file != null ) {
            if (file.getSize() > 0 && file.getSize() < 77000) {
                String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                    /// add new picture
                    String originalFileName = category.getName()+ "-" + file.getOriginalFilename();
                    File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", originalFileName);

                    try {
                        file.transferTo(imageFile);
                        category.setPicture(originalFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // image type limit
                else{
                    notifyService.addWarningMessage("You can upload only images !");
                }
            } // size limit
            else{

            }
        }
        else {
            notifyService.addWarningMessage("Invalid file");
        }

        this.categoryRepository.saveAndFlush(category);
        return "redirect:/admin/categories/";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("view", "admin/category/edit");
        Category category = this.categoryRepository.findOne(id);
        model.addAttribute("category", category);
        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id, CategoryBindingModel categoryBindingModel, MultipartFile file){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        Category category=this.categoryRepository.findOne(id);

        /// changing category Image
        String root = System.getProperty("user.dir");
        file = categoryBindingModel.getPicture();

        if (file != null ) {
            if (file.getSize() > 0 && file.getSize() < 77000) {
                String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                    //delete old pic:
                    String oldPic = category.getPicture();
                    System.out.println(oldPic);
                    if (oldPic != null) {
                        File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", oldPic);
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
                    String originalFileName = category.getName()+ "-" + file.getOriginalFilename();
                    File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", originalFileName);

                    try {
                        file.transferTo(imageFile);
                        category.setPicture(originalFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // image type limit
                else{
                    notifyService.addWarningMessage("You can upload only images !");
                }
            } // size limit
            else{

            }
        }
        else {
            //notifyService.addWarningMessage("Invalid file");
        }



        category.setName(categoryBindingModel.getName());
        this.categoryRepository.saveAndFlush(category);
        return "redirect:/admin/categories/";
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        Category category = this.categoryRepository.findOne(id);
        model.addAttribute("category", category);
        model.addAttribute("view", "admin/category/delete");
        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/admin/categories/";
        }
        Category category = this.categoryRepository.findOne(id);

        for (Article article:category.getArticles()){

            // delete comments of the article
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
                        notifyService.addErrorMessage("Delete operation failed !");
                    }
                } catch (Exception e) {
                    notifyService.addErrorMessage("Massive esception occurred due to file deletion issues !");
                    e.printStackTrace();
                }
            }


            this.articleRepository.delete(article);
        }

        //delete category pic:
        String root = System.getProperty("user.dir");
        String oldPic = category.getPicture();
        System.out.println(oldPic);
        if (oldPic != null) {
            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\categories\\", oldPic);
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
        this.categoryRepository.delete(category);
        return "redirect:/admin/categories/";
    }




}
