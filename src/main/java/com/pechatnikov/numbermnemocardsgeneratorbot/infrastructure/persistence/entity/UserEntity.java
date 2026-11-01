package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "\"user\"")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private BotPlatform platform;

    @NotNull
    @Column(name = "external_id", nullable = false)
    private Long externalId;

    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "state")
    private String state;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

}