package com.ben.identity.services.impls;

import com.ben.identity.entities.User;
import com.ben.identity.exceptions.AppException;
import com.ben.identity.repositories.UserRepository;
import com.ben.identity.services.UserService;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.ben.identity.exceptions.AppErrorCode.USER_NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    @PostConstruct
    public void generateAndSaveFakeUsers() {
        Faker faker = new Faker();
        List<User> users = new ArrayList<>();

        User myUser = User.builder()
                .email("benlun99999@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .isActivated(true)
                .build();
        users.add(myUser);

        for (int i = 0; i < 20; i++) { // Generate 10 fake users
            String email = faker.internet().emailAddress();
            String password = passwordEncoder.encode(email);
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setActivated(true);
            users.add(user);

        }
        userRepository.saveAll(users);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AppException(USER_NOT_FOUND, NOT_FOUND, "User not found with email: " + email));
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(() ->
                new AppException(USER_NOT_FOUND, NOT_FOUND, "User not found with id: " + id));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(User user, String password) {
        user.setPassword(password);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(User user) {
        user.setActivated(true);
        userRepository.save(user);
    }

}