package softuniBlog.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softuniBlog.bindingModel.UserBindingModel;
import softuniBlog.entity.Category;
import softuniBlog.entity.Role;
import softuniBlog.entity.User;
import softuniBlog.repository.CategoryRepository;
import softuniBlog.repository.RoleRepository;
import softuniBlog.repository.UserRepository;
import softuniBlog.service.NotificationService;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private NotificationService notifyService;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("view", "user/register");

        return "base-layout";
    }

    @PostMapping("/register")
    public String registerProcess(UserBindingModel userBindingModel){

        if(!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
            return "redirect:/register";
        }

        boolean userAlreadyExists = this.userRepository.findByEmail(userBindingModel.getEmail()) != null;

        if (userAlreadyExists){
            notifyService.addErrorMessage("User already exists in Database !");
            return "redirect:/register";
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        User user = new User(
                userBindingModel.getEmail(),
                userBindingModel.getFullName(),
                bCryptPasswordEncoder.encode(userBindingModel.getPassword()),
                userBindingModel.getPicture().getOriginalFilename()
        );

        Role userRole = this.roleRepository.findByName("ROLE_USER");

        user.addRole(userRole);
        String root = System.getProperty("user.dir");
        MultipartFile file = userBindingModel.getPicture();

        if (file != null && (file.getSize() > 0 && file.getSize() < 77000)) {
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
            if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                /// add new picture
                String originalFileName = user.getFullName() + file.getOriginalFilename();
                File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", originalFileName);

                try {
                    file.transferTo(imageFile);
                    user.setPicture(originalFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            user.setPicture("javauser.jpg");
        }
        notifyService.addInfoMessage("Successful registration.");
        this.userRepository.saveAndFlush(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model){

        model.addAttribute("view", "user/login");

        return "base-layout";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }
    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePost(UserBindingModel userBindingModel, MultipartFile file){
        try {
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            User user = this.userRepository.findByEmail(principal.getUsername());

            String root = System.getProperty("user.dir");
            file = userBindingModel.getPicture();

            if (file != null ) {
                if (file.getSize() > 0 && file.getSize() < 77000) {
                    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length()).toLowerCase();
                    if (fileExtension.equals(".png") || fileExtension.equals(".jpg") || fileExtension.equals(".gif") || fileExtension.equals(".jpeg")) {

                        //delete old pic:

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

                        /// add new picture
                        String originalFileName = user.getFullName() + file.getOriginalFilename();
                        File imageFile = new File(root + "\\src\\main\\resources\\static\\images\\users\\", originalFileName);

                        try {
                            file.transferTo(imageFile);
                            user.setPicture(originalFileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } // image type limit
                    else{
                        notifyService.addErrorMessage("You can upload only images !");
                    }
                } // size limit
                else{
                    notifyService.addWarningMessage("The loaded file was skipped due to size resrictions - 77 kB upper limit size");
                }
            }
            else {
                notifyService.addErrorMessage("Invalid file");
            }

            this.userRepository.saveAndFlush(user);

            return "redirect:/profile";
        }// end of try
        catch(Exception e){
            e.printStackTrace();
            return "redirect:/profile";
        }
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public String profileOfUser(@PathVariable Integer id , Model model){
        List<Category> categories = categoryRepository.findAll();
        User user = this.userRepository.findOne(id);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User userEntity = this.userRepository.findByEmail(principal.getUsername());

        if(userEntity.getId().equals(user.getId())) {
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            model.addAttribute("view", "user/profile");
            return "base-layout";
        }

        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profileOfUser");

        return "base-layout";
    }

}

