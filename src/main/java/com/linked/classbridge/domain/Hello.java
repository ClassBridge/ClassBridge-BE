package com.linked.classbridge.domain;

import com.linked.classbridge.dto.HelloDto.HelloRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE hello SET deleted_at = now() WHERE hello_id = ?")
@SQLRestriction("deleted_at is null")
public class Hello extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long helloId;

    private String name;

    private int age;

    public void update(HelloRequest request) {
        this.name = request.name();
        this.age = request.age();
    }
}
