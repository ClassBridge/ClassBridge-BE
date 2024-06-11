package com.linked.classbridge.service;

import com.linked.classbridge.domain.Badge;
import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserBadges;
import com.linked.classbridge.domain.UserCategoryStats;
import com.linked.classbridge.repository.BadgeRepository;
import com.linked.classbridge.repository.UserBadgeRepository;
import com.linked.classbridge.repository.UserCategoryStatsRepository;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {

    private final UserCategoryStatsRepository userCategoryStatsRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(UserCategoryStatsRepository userCategoryStatsRepository, BadgeRepository badgeRepository,
                        UserBadgeRepository userBadgeRepository) {
        this.userCategoryStatsRepository = userCategoryStatsRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public void addStamp(User user, Category category) {
        UserCategoryStats stats = userCategoryStatsRepository.findByUserAndCategory(user, category)
                .orElseGet(() -> UserCategoryStats.builder()
                        .user(user)
                        .category(category)
                        .stampCount(0)
                        .build()
                );
        stats.setStampCount(stats.getStampCount() + 1);
        userCategoryStatsRepository.save(stats);

        Badge badge = badgeRepository.findByCategoryAndThreshold(category, stats.getStampCount())
                .orElse(null);
        if (badge != null && userBadgeRepository.findByUserAndBadge(user, badge).isEmpty()) {
            UserBadges userBadge = UserBadges.builder()
                    .user(user)
                    .badge(badge)
                    .build();
            userBadgeRepository.save(userBadge);
        }
    }
}
