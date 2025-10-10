package com.pechatnikov.numbermnemocardsgeneratorbot.repository;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
public class UserEntityRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindUser() {
        String USERNAME = "motherfucker";
        // Given
        UserEntity userEntity = UserEntity.builder()
                .telegramId(1L)
                .username(USERNAME)
                .firstname("name")
                .lastname("idont tell you")
                .createdAt(Instant.now())
                .build();


        // When
        UserEntity savedUserEntity = springDataUserRepository.save(userEntity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> foundUser = springDataUserRepository.findById(savedUserEntity.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(USERNAME);
    }
}
