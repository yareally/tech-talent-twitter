package com.tts.techtalenttwitter.controllers;

import com.tts.techtalenttwitter.models.Tweet;
import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.services.TweetService;
import com.tts.techtalenttwitter.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Controller
public class TweetController {
    private UserService userService;
    private TweetService tweetService;

    @Autowired
    public TweetController(UserService userService, TweetService tweetService) {
        this.userService = userService;
        this.tweetService = tweetService;
    }

    @GetMapping(value = {"/tweets", "/"})
    public String getFeed(Model model) {
        List<Tweet> tweets = tweetService.findAll();
        model.addAttribute("tweetList", tweets);
        // return to the feed.html page (must have a matching template called feed)
        return "feed";
    }

    @GetMapping(value = "/tweets/new")
    public String getTweetForm(Model model) {
        model.addAttribute("tweet", new Tweet());
        // return to the newTweet.html page (must have a matching template called newTweet)
        return "newTweet";
    }

    @PostMapping(value = "/tweets")
    public String submitTweetForm(@Valid Tweet tweet, BindingResult bindingResult, Model model) {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isPresent() && !bindingResult.hasErrors()) {
            tweet.setUser(user.get());
            tweetService.save(tweet);
            model.addAttribute("successMessage", "Tweet successfully created!");
            model.addAttribute("tweet", new Tweet());
        }
        // return back to the newTweet template page
        return "newTweet";
    }
}
