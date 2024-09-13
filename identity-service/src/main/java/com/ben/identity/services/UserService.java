package com.ben.identity.services;

import com.ben.identity.entities.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    User findByEmail(String email);

    User findById(String id);

    boolean existsByEmail(String email);

    User createUser(User user);

    void updatePassword(User user, String password);

    void activateUser(User user);

}