package com.pechatnikov.numbermnemocardsgeneratorbot.repository;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Transactional
public class UserEntityRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindUser() {
        String USERNAME = "TestUserName";
        // Given
        UserEntity userEntity = UserEntity.builder()
                .platform(BotPlatform.TELEGRAM)
                .externalId(1L)
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
        assertThat(foundUser.get().getPlatform()).isEqualTo(BotPlatform.TELEGRAM);
    }

    @Test
    void shouldFindByPlatformAndExternalId() {
        // Given
        springDataUserRepository.save(user(BotPlatform.TELEGRAM, 100L, "telegram_user"));
        springDataUserRepository.save(user(BotPlatform.MAX, 100L, "max_user"));
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<UserEntity> telegramUser = springDataUserRepository.findByPlatformAndExternalId(BotPlatform.TELEGRAM, 100L);
        Optional<UserEntity> maxUser = springDataUserRepository.findByPlatformAndExternalId(BotPlatform.MAX, 100L);
        Optional<UserEntity> missingUser = springDataUserRepository.findByPlatformAndExternalId(BotPlatform.MAX, 200L);

        // Then
        assertThat(telegramUser).map(UserEntity::getUsername).contains("telegram_user");
        assertThat(maxUser).map(UserEntity::getUsername).contains("max_user");
        assertThat(missingUser).isEmpty();
    }

    @Test
    void shouldSaveUserWithoutUsername() {
        // Given
        UserEntity savedUserEntity = springDataUserRepository.save(user(BotPlatform.MAX, 300L, null));
        springDataUserRepository.save(user(BotPlatform.MAX, 301L, null));
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(springDataUserRepository.findById(savedUserEntity.getId()))
            .map(UserEntity::getUsername)
            .isEmpty();
    }

    @Test
    void shouldNotAllowDuplicatePlatformAndExternalId() {
        // Given
        springDataUserRepository.save(user(BotPlatform.MAX, 400L, "first"));
        entityManager.flush();

        // When / Then
        assertThatThrownBy(() -> {
            springDataUserRepository.save(user(BotPlatform.MAX, 400L, "second"));
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    private static UserEntity user(BotPlatform platform, Long externalId, String username) {
        return UserEntity.builder()
            .platform(platform)
            .externalId(externalId)
            .username(username)
            .createdAt(Instant.now())
            .build();
    }
}
