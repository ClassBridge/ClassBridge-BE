package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Badge;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserBadges;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadges, Long> {

    Optional<UserBadges> findByUserAndBadge(User user, Badge badge);
}
