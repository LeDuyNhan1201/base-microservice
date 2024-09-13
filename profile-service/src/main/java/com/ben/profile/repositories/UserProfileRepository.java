package com.ben.profile.repositories;

import com.ben.profile.entities.UserProfile;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Observed
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}