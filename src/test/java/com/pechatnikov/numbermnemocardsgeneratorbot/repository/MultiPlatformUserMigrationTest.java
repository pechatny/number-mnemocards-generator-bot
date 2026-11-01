package com.pechatnikov.numbermnemocardsgeneratorbot.repository;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Миграция 007 на базе с данными в состоянии до неё: существующие Telegram-пользователи и их балансы сохраняются.
 * Отдельный контейнер — нужна чистая база, на которую миграции накатываются по частям.
 */
class MultiPlatformUserMigrationTest {
    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";
    // 001–006: по одному changeSet в файле, в 006 — два
    private static final int CHANGESETS_BEFORE_007 = 7;

    @Test
    void shouldKeepExistingTelegramUsersAndBalances() throws Exception {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")) {
            postgres.start();

            try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
                Liquibase liquibase = liquibase(connection);
                liquibase.update(CHANGESETS_BEFORE_007, new Contexts(), new LabelExpression());

                try (Statement statement = connection.createStatement()) {
                    statement.execute("INSERT INTO \"user\" (id, telegram_id, username, firstname) VALUES (1, 111, 'old_user', 'Old')");
                    statement.execute("INSERT INTO \"user\" (id, telegram_id, username, firstname) VALUES (2, 222, 'second', 'Second')");
                    statement.execute("INSERT INTO token_balance (id, user_id, balance) VALUES (1, 1, 37)");
                }
                // Liquibase выключает autocommit и делает rollback перед захватом блокировки
                connection.commit();

                liquibase.update(new Contexts(), new LabelExpression());

                try (Statement statement = connection.createStatement();
                     ResultSet users = statement.executeQuery(
                         "SELECT u.id, u.platform, u.external_id, u.username, b.balance " +
                             "FROM \"user\" u LEFT JOIN token_balance b ON b.user_id = u.id ORDER BY u.id")) {
                    assertThat(users.next()).isTrue();
                    assertThat(users.getString("platform")).isEqualTo("TELEGRAM");
                    assertThat(users.getLong("external_id")).isEqualTo(111L);
                    assertThat(users.getString("username")).isEqualTo("old_user");
                    assertThat(users.getLong("balance")).isEqualTo(37L);

                    assertThat(users.next()).isTrue();
                    assertThat(users.getString("platform")).isEqualTo("TELEGRAM");
                    assertThat(users.getLong("external_id")).isEqualTo(222L);

                    assertThat(users.next()).isFalse();
                }

                // После миграции: пользователь Max с тем же внешним ID и без username
                try (Statement statement = connection.createStatement()) {
                    statement.execute("INSERT INTO \"user\" (id, platform, external_id, username) VALUES (3, 'MAX', 111, NULL)");
                }
            }
        }
    }

    private static Liquibase liquibase(Connection connection) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        return new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database);
    }
}
