package com.tts.techtalenttwitter.services;

import com.tts.techtalenttwitter.models.Tweet;
import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.repositories.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Service
public class TweetService {

    private TweetRepository tweetRepository;

    @Autowired
    public TweetService(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    /**
     * Find all tweets regardless of user
     * @return
     */
    public List<Tweet> findAll() {
        List<Tweet> tweets = tweetRepository.findAllByOrderByCreatedAtDesc();
        return tweets;
    }

    /**
     * Given a user, find all their tweets and return them
     * @param user
     * @return
     */
    public List<Tweet> findAllByUser(User user) {
        List<Tweet> tweets = tweetRepository.findAllByUserOrderByCreatedAtDesc(user);
        return tweets;
    }

    /**
     * Given a list of users, find all their tweets and return them
     * @param users
     * @return
     */
    public List<Tweet> findAllByUsers(List<User> users) {
        List<Tweet> tweets = tweetRepository.findAllByUserInOrderByCreatedAtDesc(users);
        return tweets;
    }

    /**
     * Add a new tweet
     * @param tweet
     */
    public void save(Tweet tweet) {
        tweetRepository.save(tweet);
    }

}
