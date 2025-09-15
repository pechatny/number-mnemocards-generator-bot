package com.pechatnikov.numbermnemocardsgeneratorbot.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @NotNull
    @Lob
    @Column(name = "username", nullable = false)
    private String username;

    @Lob
    @Column(name = "firstname")
    private String firstname;

    @Lob
    @Column(name = "lastname")
    private String lastname;

    @Column(name = "created_at")
    private Instant createdAt;

}