package softuniBlog.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import softuniBlog.bindingModel.UserEditBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Comment;
import softuniBlog.entity.Role;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CommentRepository;
import softuniBlog.repository.RoleRepository;
import softuniBlog.repository.UserRepository;
import softuniBlog.service.NotificationService;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    private NotificationService notifyService;

    @GetMapping("/")
    public String listUsers(Model model){
        List<User> users = this.userRepository.findAll();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("view", "admin/user/list");

        return "base-layout";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);
        List<Role> roles = this.roleRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("view", "admin/user/edit");

        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id,
                              UserEditBindingModel userBindingModel){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        // checking if edit user email already exists
        String newUserName= userBindingModel.getEmail();
        List<User> existingUsers = this.userRepository.findAll();
        existingUsers.remove(user);
        for (User u:existingUsers) {
            if(u.getEmail().equals(newUserName)){
                notifyService.addErrorMessage("User already exists in Database - choose appropriate email");
                return "redirect:/admin/users/edit/{id}";
            }
        }

        if(!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())){

            if(userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

        user.setFullName(userBindingModel.getFullName());
        user.setEmail(userBindingModel.getEmail());

        Set<Role> roles = new HashSet<>();

        for (Integer roleId : userBindingModel.getRoles()){
            roles.add(this.roleRepository.findOne(roleId));
        }

        user.setRoles(roles);

        this.userRepository.saveAndFlush(user);

        return "redirect:/admin/users/";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }

        User user = this.userRepository.findOne(id);

        model.addAttribute("user", user);
        model.addAttribute("view", "admin/user/delete");

        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.userRepository.exists(id)){
            return "redirect:/admin/users/";
        }
        User user = this.userRepository.findOne(id);

        /// checking if admin tries to delete himself:

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        User userEntity = this.userRepository.findByEmail(principal.getUsername());
        if(userEntity.getId().equals(user.getId())) {
            notifyService.addErrorMessage("User cannot delete himself");
            notifyService.addInfoMessage("Hacking the Java Blog Project is forbidden and will be persecuted !");
            return "redirect:/admin/users/";
        }


        for(Article article : user.getArticles()){

            // delete comments of the article
            List<Comment> commentsList = this.commentRepository.findByArticle(article);
            for (Comment comment : commentsList) {
                this.commentRepository.delete(comment);
            }

            System.out.println("CHECKING");
            //delete article picture:
            String root = System.getProperty("user.dir");
            String oldPic = article.getPicture();
            if (oldPic != null) {
                File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\articles\\", oldPic);
                try {
                    if (oldPicFile.delete()) {
                        System.out.println(oldPicFile.getName() + " is deleted!");
                    } else {
//                        notifyService.addErrorMessage("Delete operation failed !");
                    }
                } catch (Exception e) {
                    notifyService.addErrorMessage("Massive esception occurred due to file deletion issues !");
                    e.printStackTrace();
                }
            }

            this.articleRepository.delete(article);
        }

        //delete User pic:
        String root = System.getProperty("user.dir");
        String oldPic = user.getPicture();
        if (oldPic != null && !oldPic.equals("javauser.jpg")) {
            File oldPicFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", oldPic);
            try {
                if (oldPicFile.delete()) {
                    System.out.println(oldPicFile.getName() + " is deleted!");
                } else {
                    notifyService.addErrorMessage("Delete process failed");
                }
            } catch (Exception e) {
                notifyService.addErrorMessage("Exception due to failure with delete file process");
                e.printStackTrace();
            }
        }
        ///////

        /// delete user comments in other articles:
        for (Comment comment:user.getComments()) {
            this.commentRepository.delete(comment);
        }

        this.userRepository.delete(user);

        return "redirect:/admin/users/";
    }
}
