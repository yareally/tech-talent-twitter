package com.tts.techtalenttwitter.controllers;

import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Optional;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Controller
public class AuthController {
    private UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    // https://twitter.com/signup
    @GetMapping(value = "/signup")
    public String registration(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "registration";
    }


    @PostMapping(value = "/signup")
    public String createNewUser(@Valid User user, BindingResult bindingResult, Model model) {
       userService.findByUsername(user.getUsername())
           .ifPresentOrElse(
               foundUser -> {},
               () -> bindingResult.rejectValue("username", "error.user", "Username is already taken")
           );

        if (!bindingResult.hasErrors()) {
            userService.saveNewUser(user);
            model.addAttribute("success", "Sign up successful!");
            model.addAttribute("user", new User());
        }
        return "registration";
    }
}
