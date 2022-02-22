package com.microservice.authservice.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "user_table")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserModel implements Serializable {
    @Id
    private String uuid=UUID.randomUUID().toString();

    @Column(nullable = false,unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String createdAt;
    @Column(nullable = false)
    private String updatedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserModel userModel = (UserModel) o;
        return uuid != null && Objects.equals(uuid, userModel.uuid);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
