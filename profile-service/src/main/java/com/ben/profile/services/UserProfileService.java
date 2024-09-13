package com.ben.profile.services;

import com.ben.profile.entities.UserProfile;
import org.springframework.stereotype.Service;

@Service
public interface UserProfileService {

    UserProfile findById(String id);

    UserProfile createUser(UserProfile user);

    void updateAvatar(UserProfile user, String avatarId);

}