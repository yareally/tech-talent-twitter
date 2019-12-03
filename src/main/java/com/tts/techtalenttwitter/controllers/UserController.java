package com.tts.techtalenttwitter.controllers;

import com.tts.techtalenttwitter.models.Tweet;
import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.services.TweetService;
import com.tts.techtalenttwitter.services.UserService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author Wes Lanning
 * @version 2019-12-02
 */
@Controller
public class UserController {
    private UserService userService;

    private TweetService tweetService;

    @Autowired
    public UserController(UserService userService, TweetService tweetService) {
        this.userService = userService;
        this.tweetService = tweetService;
    }

    @GetMapping(value = "/users/{username}")
    public String getUser(@PathVariable(value = "username") String username, Model model) {
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            List<Tweet> tweets = tweetService.findAllByUser(user.get());
            model.addAttribute("tweetList", tweets);
            model.addAttribute("user", user);
        }
        return "user";
    }

    @GetMapping(value = "/users")
    public String getUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        setTweetCounts(users, model);
        return "users";
    }

    private void setTweetCounts(List<User> users, Model model) {
        var tweetCounts = new HashMap<String, Integer>();

        users.forEach(user -> {
            List<Tweet> tweets = tweetService.findAllByUser(user);
            tweetCounts.put(user.getUsername(), tweets.size());
        });
        model.addAttribute("tweetCounts", tweetCounts);
    }
}
