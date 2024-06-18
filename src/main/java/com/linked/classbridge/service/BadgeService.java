package com.linked.classbridge.service;

import static org.yaml.snakeyaml.tokens.Token.ID.Value;

import com.linked.classbridge.domain.Badge;
import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserBadges;
import com.linked.classbridge.domain.UserCategoryStats;
import com.linked.classbridge.dto.badge.BadgeResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.BadgeRepository;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.UserBadgeRepository;
import com.linked.classbridge.repository.UserCategoryStatsRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.CategoryType;
import com.linked.classbridge.type.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BadgeService {

    private final UserCategoryStatsRepository userCategoryStatsRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    public BadgeService(UserCategoryStatsRepository userCategoryStatsRepository, BadgeRepository badgeRepository,
                        UserBadgeRepository userBadgeRepository, UserRepository userRepository,
                        CategoryRepository categoryRepository, S3Service s3Service) {

        this.userCategoryStatsRepository = userCategoryStatsRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.s3Service = s3Service;
    }

    // 테스트 환경에서 개발자가 직접 뱃지를 등록
    public void uploadBadge(String badgeName, MultipartFile badgeImage, Long categoryId, int threshold) {

        String imageUrl = s3Service.uploadImage(badgeImage, "badge/");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CATEGORY_NOT_FOUND));

        Badge badge = Badge.builder()
                .name(badgeName)
                .imageUrl(imageUrl)
                .category(category)
                .threshold(threshold)
                .build();

        badgeRepository.save(badge);
    }

    public void addStamp(String userEmail, String categoryName) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findByName(CategoryType.valueOf(categoryName));
        if (category == null) {
            throw new RestApiException(ErrorCode.CATEGORY_NOT_FOUND);
        }

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

    public List<BadgeResponse> getBadges(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));
        List<UserBadges> userBadges = userBadgeRepository.findByUser(user);

        return userBadges.stream()
                .map(UserBadges::getBadge)
                .map(badge -> BadgeResponse.builder()
                        .name(badge.getName())
                        .imageUrl(badge.getImageUrl())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
