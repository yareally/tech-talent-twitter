package com.tts.techtalenttwitter.repositories;

import com.tts.techtalenttwitter.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}
