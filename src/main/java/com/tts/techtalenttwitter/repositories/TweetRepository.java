package com.tts.techtalenttwitter.repositories;

import com.tts.techtalenttwitter.models.Tweet;
import com.tts.techtalenttwitter.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {
    List<Tweet> findAllByOrderByCreatedAtDesc();
    List<Tweet> findAllByUserOrderByCreatedAtDesc(User user);
    List<Tweet> findAllByUserInOrderByCreatedAtDesc(List<User> users);
}
