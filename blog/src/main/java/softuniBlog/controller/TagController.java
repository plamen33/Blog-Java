package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import softuniBlog.entity.Article;
import softuniBlog.entity.Category;
import softuniBlog.entity.Tag;
import softuniBlog.entity.User;
import softuniBlog.repository.TagRepository;
import softuniBlog.repository.UserRepository;

import java.util.Set;

@Controller
public class TagController {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/tag/{name}")
    public String articlesWithTag(Model model, @PathVariable String name){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal().equals("anonymousUser")){
            Tag tag = tagRepository.findByName(name);
            if(tag==null){
                return "redirect:/";
            }

            model.addAttribute("view", "tag/articles");
            model.addAttribute("tag", tag);

            return "base-layout";
        }
        else{
            Tag tag = tagRepository.findByName(name);
            if(tag==null){
                return "redirect:/";
            }
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", user);
            model.addAttribute("view", "tag/articles");
            model.addAttribute("tag", tag);

            return "base-layout";
        }
    }
}
