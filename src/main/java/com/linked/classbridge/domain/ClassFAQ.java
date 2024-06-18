package com.linked.classbridge.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "class_faq")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE class_faq SET deleted_at = NOW() WHERE faq_id = ?")
@ToString
public class ClassFAQ extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faqId;

    private String title;
    private String content;

    @JoinColumn(name = "class_id")
    @ManyToOne
    private OneDayClass oneDayClass;
}
