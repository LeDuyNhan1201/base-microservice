package com.ben.profile.services.impls;

import com.ben.profile.entities.UserProfile;
import com.ben.profile.exceptions.AppException;
import com.ben.profile.repositories.UserProfileRepository;
import com.ben.profile.services.UserProfileService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ben.profile.exceptions.AppErrorCode.PROFILE_NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    UserProfileRepository userProfileRepository;

    @Override
    public UserProfile findById(String id) {
        return userProfileRepository.findById(id).orElseThrow(() ->
                new AppException(PROFILE_NOT_FOUND, NOT_FOUND, "Profile not found with id: " + id));
    }

    @Transactional
    @Override
    public UserProfile createUser(UserProfile user) {
        return userProfileRepository.save(user);
    }

    @Transactional
    @Override
    public void updateAvatar(UserProfile user, String avatarId) {
        user.setAvatarId(avatarId);
        userProfileRepository.save(user);
    }

}