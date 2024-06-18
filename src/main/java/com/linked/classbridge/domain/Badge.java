package com.linked.classbridge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Badge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name; // 뱃지 이름

    private String imageUrl; // 뱃지 이미지 URL

    private int threshold; // 뱃지 획득 조건

    @Override
    @Transient
    public LocalDateTime getDeletedAt() {
        return super.getDeletedAt();
    }

    @Override
    public void setDeletedAt(LocalDateTime deletedAt) {
        // Do nothing
    }
}
