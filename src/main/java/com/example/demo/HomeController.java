package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserService userService;
    @GetMapping ("/register")
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping ("/register")
    public String processRegistrationPrage(@Valid
                                           @ModelAttribute("user") User user,
                                           BindingResult result,
                                           Model model){
        model.addAttribute("user", user);
        if (result.hasErrors())
        {
            return "registration";
        }
        else
        {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Created");
        }
        return "index";
    }

    //home with list of messages

    @RequestMapping("/")
    public String index(Model model){
        model.addAttribute("messages", messageRepository.findAll());
        return "index";
    }

    //login stuff
    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    //login process
    @RequestMapping("/secure")
    public String secure(Principal principal, Model model){
        User myuser = ((CustomUserDetails)
                ((UsernamePasswordAuthenticationToken) principal)
                .getPrincipal()).getUser();
                model.addAttribute("myuser", myuser);
                return "index";
    }

    //message posting

    @GetMapping("/add")
    public String newMessage(Model model){
        model.addAttribute("message", new Message());
        return "messageform";
    }

    @PostMapping("/add")
    public String processMessage(@ModelAttribute Message message,
                                 @RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            messageRepository.save(message);
            return "redirect:/";
        }
        try{
            Map uploadResult = cloudc.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            message.setPic(uploadResult.get("url").toString());
            messageRepository.save(message);
        }catch (IOException e){
            e.printStackTrace();
            return "redirect:/add";
        }
        return  "redirect:/";
    }

    //detail add and delete

    @RequestMapping("/detail/{id}")
    public String showCourse(@PathVariable("id") long id, Model model)
    {
        model.addAttribute("message", messageRepository.findById(id).get());
        return "show";
    }

    @RequestMapping("/update/{id}")
    public String updateCourse(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findById(id).get());
        return "messageform";
    }

    @RequestMapping("/delete/{id}")
    public String delCourse(@PathVariable("id") long id){
        messageRepository.deleteById(id);
        return "redirect:/";
    }



}
