package com.tts.techtalenttwitter.services;

import com.tts.techtalenttwitter.models.Role;
import com.tts.techtalenttwitter.models.Role.RoleType;
import com.tts.techtalenttwitter.models.User;
import com.tts.techtalenttwitter.repositories.RoleRepository;
import com.tts.techtalenttwitter.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Service
public class UserService {
    private UserRepository        userRepository;
    private RoleRepository        roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public List<User> findAll() {
        return (List<User>) userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User saveNewUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(true);
        Role userRole = roleRepository.findByRole(RoleType.USER);
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        return userRepository.save(user);
    }

    public Optional<User> getLoggedInUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(userName);
    }
}
