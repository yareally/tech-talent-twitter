package com.tts.techtalenttwitter.controllers;

import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * @author Wes Lanning
 * @version 2019-12-02
 */
@Controller
public class FollowController {
    private UserService userService;

@Autowired
    public FollowController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/follow/{username}")
    public String follow(@PathVariable(value = "username") String username, HttpServletRequest request) {
        Optional<User> loggedInUser = userService.getLoggedInUser();
        Optional<User> userToFollow = userService.findByUsername(username);

        if (loggedInUser.isPresent() && userToFollow.isPresent()) {
            List<User> followers = userToFollow.get().getFollowers();
            userToFollow.get().setFollowers(followers);
            userService.save(userToFollow.get());
        }
        return String.format("redirect:%s", request.getHeader("Referer"));
    }

    @PostMapping(value = "/unfollow/{username}")
    public String unfollow(@PathVariable(value = "username") String username, HttpServletRequest request) {
        Optional<User> loggedInUser = userService.getLoggedInUser();
        Optional<User> userToUnfollow = userService.findByUsername(username);

        if (loggedInUser.isPresent() && userToUnfollow.isPresent()) {
            List<User> followers = userToUnfollow.get().getFollowers();
            followers.remove(loggedInUser.get());
            userToUnfollow.get().setFollowers(followers);
            userService.save(userToUnfollow.get());
        }
        return String.format("redirect:%s", request.getHeader("Referer"));
    }
}
