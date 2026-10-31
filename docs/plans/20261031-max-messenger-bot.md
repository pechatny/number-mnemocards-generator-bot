# Поддержка бота Max и включение ботов через properties

## Overview
- Добавить второй входящий/исходящий адаптер — бот мессенджера **Max** — рядом с существующим Telegram-ботом.
- Включение каждого бота управляется через `application.yaml` (`bot.telegram.enabled`, `bot.max.enabled`).
  - сейчас: Telegram **выключен**, Max **включён**;
  - архитектура должна позволять включить оба одновременно.
- Оплата (ЮКасса) для Max **не подключается**: вместо кнопок с ценами пользователь Max получает сообщение «оплата пока недоступна». Для Telegram оплата работает как раньше.
- Токен Max-бота передаётся через properties (`bot.max.token: ${MAX_BOT_TOKEN}`).
- Бизнес-логика (`application/`, `domain/`) переиспользуется; меняются только сигнатуры там, где сейчас зашито понятие Telegram.

## Context (from discovery)
- Стек: Spring Boot 2.7.18, Java 11, Maven, JPA + Liquibase + PostgreSQL, MapStruct, Lombok, `telegrambots-spring-boot-starter 6.0.1`, Testcontainers.
- Входящий адаптер Telegram: `infrastructure/telegram/MnemocardsGeneratorBot` (long polling, пул на 10 потоков), `handler/CallbackHandler`, `PreCheckoutHandler`, `SuccessfulPaymentHandler`, `mapper/TelegramUpdateMapper(Impl)`.
- Исходящие порты (`application/port/out`) и их единственные Telegram-реализации (`infrastructure/telegram/*Adapter`):
  `SendMessageService`, `SendPhotoService`, `SendButtonMessageService`, `SendButtonsMessageService`, `SendCallbackAnswerService`, `DeleteMessageService`, `SendInvoiceService`, `SendPreCheckoutService`.
- Все Telegram-бины — безусловные `@Component`/`@Service`; `TelegramApiClient` + `BotProperties(bot.name, bot.token)`.
- Привязка к Telegram в ядре:
  - `domain/user/User.telegramId`, `GetOrCreateUserCommand.telegramId`, `UserRepositoryPort.findByTelegramId`, `UserService.findByTelegramId`;
  - `CommandProcessor.process(Long telegramId, Long chatId)`, `CommandHandler.handle(command, telegramId, chatId)`;
  - `domain/Callback.telegramId`, `Callback.messageId: Integer` (в Max id сообщения — строка `mid...`);
  - `DeleteMessageService.delete(Long chatId, Integer messageId)`.
- БД: таблица `"user"` — `telegram_id bigint NOT NULL`, `username varchar NOT NULL UNIQUE` (у пользователя Max username может отсутствовать). Последняя миграция — `006-create-order-table.yaml`.
- Флоу «нет токенов»: `NumericMessageHandlerImpl` → `ShowPricesButtonUseCase` → `ShowPricesCallbackProcessor` → `CreateInvoiceCallbackProcessor` → `SendInvoiceService`.
- Тесты: `BaseIntegrationTest` (Testcontainers Postgres), `UserEntityRepositoryTest`, `OrderServiceTest`, `ImageMergerTest`, `AllNumbersSplitterTest`, `MoneyTest`.

## Development Approach
- **testing approach**: Regular (сначала код, затем тесты в рамках той же задачи)
- complete each task fully before moving to the next
- make small, focused changes
- **CRITICAL: every task MUST include new/updated tests** for code changes in that task
    - write unit tests for new/modified functions/methods
    - add new test cases for new code paths
    - update existing test cases if behavior changes
    - tests cover both success and error scenarios
- **CRITICAL: all tests must pass before starting next task** - no exceptions
- **CRITICAL: update this plan file when scope changes during implementation**
- run tests after each change: `./mvnw test`
- maintain backward compatibility (существующие Telegram-пользователи и их балансы сохраняются)

## Testing Strategy
- **unit tests**: обязательны для каждой задачи (маршрутизаторы, мапперы Max-апдейтов, Max-адаптеры с замоканным SDK, use case без оплаты).
- **integration tests**: `BaseIntegrationTest` (Testcontainers) — миграция `007`, репозиторий пользователей, загрузка контекста при разных комбинациях флагов.
- **conditional beans**: `ApplicationContextRunner` — проверка, что при `telegram.enabled=false` Telegram-бины не создаются (и наоборот).
- e2e/UI-тестов в проекте нет.

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with `[+]` prefix
- document issues/blockers with `[!]` prefix
- update plan if implementation deviates from original scope
- keep plan in sync with actual work done

## Solution Overview
**Подход: платформа в контексте вызова + маршрутизирующие адаптеры исходящих портов.**

1. В domain появляется `enum BotPlatform { TELEGRAM, MAX }`.
2. Пользователь идентифицируется парой `(platform, externalId)` вместо `telegramId`. Один и тот же человек в TG и Max — два разных пользователя со своими балансами.
3. Все данные, по которым отправляется ответ, несут платформу явно: `Message`, `Callback`, `ButtonMessage`, `ButtonsMessage`, а методы исходящих портов, принимающие `chatId`, получают и `BotPlatform`. (ThreadLocal-контекст не подходит: `StartCommandProcessor` шлёт сообщения из `ScheduledExecutorService`.)
4. Каждый исходящий порт реализован **маршрутизатором** в `infrastructure/messaging/`, который держит `Map<BotPlatform, PlatformAdapter>` и делегирует. Платформенные адаптеры регистрируются только если их бот включён — маршрутизатор при обращении к выключенной платформе бросает понятное исключение.
5. Включение ботов — `@ConditionalOnProperty` через мета-аннотации `@ConditionalOnTelegramBot` / `@ConditionalOnMaxBot`.
6. Доступность оплаты — новый исходящий порт `PaymentAvailabilityPort.isAvailable(BotPlatform)`, реализуемый из properties (`bot.<platform>.payments-enabled`). `ShowPricesButtonUseCaseImpl` при недоступной оплате отправляет текст вместо кнопки.
7. Max-адаптер (`infrastructure/max/`) на официальном Java SDK Max: long polling `GET /updates` в отдельном потоке (`SmartLifecycle`), маппинг апдейтов в те же входящие порты (`NumericMessageHandler`, `CommandHandler`, `CallbackService`).

Почему так: application-слой остаётся независимым от мессенджеров, сохраняется текущий стиль «порт → адаптер», а добавление третьего мессенджера = новый пакет адаптеров + значение enum.

## Technical Details

### Properties (`application.yaml`)
```yaml
bot:
  telegram:
    enabled: ${TELEGRAM_BOT_ENABLED:false}
    name: NumberMnemocardsGeneratorBot
    token: ${BOT_TOKEN:}
    payments-enabled: true
  max:
    enabled: ${MAX_BOT_ENABLED:true}
    token: ${MAX_BOT_TOKEN:}
    payments-enabled: false
    polling:
      timeout-seconds: 30
      limit: 100
payment:
  token: ${PAYMENT_TOKEN:}
```
- `BotProperties` разделяется на `TelegramBotProperties` (`bot.telegram`) и `MaxBotProperties` (`bot.max`), с `@Validated` + `@NotBlank token` — валидация срабатывает только при включённом боте (бины properties тоже под условием либо проверка в `@PostConstruct`).
- Переменные окружения `BOT_TOKEN`, `PAYMENT_TOKEN` сохраняются; добавляются `MAX_BOT_TOKEN`, `TELEGRAM_BOT_ENABLED`, `MAX_BOT_ENABLED`.

### Миграция `007-multi-platform-user.yaml`
- `addColumn "user".platform varchar(32) NOT NULL DEFAULT 'TELEGRAM'`
- `renameColumn telegram_id → external_id`
- `dropUniqueConstraint` на `username` (имя ограничения проверить в БД, ожидается `user_username_key`), `dropNotNullConstraint username`
- `addUniqueConstraint (platform, external_id)` — `uq_user_platform_external_id`

### Изменения сигнатур в ядре
| Было | Стало |
|------|-------|
| `User.telegramId` | `User.platform`, `User.externalId` |
| `GetOrCreateUserCommand.telegramId` | `platform`, `externalId` |
| `UserRepositoryPort.findByTelegramId(Long)` | `findByPlatformAndExternalId(BotPlatform, Long)` |
| `CommandProcessor.process(Long telegramId, Long chatId)` | `process(CommandContext ctx)` — `platform`, `externalUserId`, `chatId` |
| `Callback.telegramId`, `Integer messageId` | `platform`, `externalUserId`, `String messageId` |
| `SendMessageService.sendMessage(Long chatId, String)` | `sendMessage(BotPlatform, Long chatId, String)` |
| `SendPhotoService.sendPhoto(Long, File)` | `sendPhoto(BotPlatform, Long, File)` |
| `DeleteMessageService.delete(Long, Integer)` | `delete(BotPlatform, Long, String messageId)` |
| `SendCallbackAnswerService.sendCallbackAnswer(String, String)` | `sendCallbackAnswer(BotPlatform, String, String)` |
| `ButtonMessage` / `ButtonsMessage` | + поле `platform` |
| `ShowPricesButtonUseCase.showPricesButton(Long chatId)` | `showPricesButton(BotPlatform, Long chatId)` |

`SendInvoiceService`, `SendPreCheckoutService` остаются Telegram-only (оплата есть только там), но тоже работают только при `bot.telegram.enabled=true`.

### Max: обработка апдейтов
| Max update | Действие |
|------------|----------|
| `message_created`, текст начинается с `/` | `CommandHandler` (`Command.fromValue`) |
| `message_created`, текст содержит цифры | `NumericMessageHandler` |
| `bot_started` | `CommandHandler` с `Command.START` |
| `message_callback` | `CallbackService` (payload = JSON `CallbackData`, как в Telegram) |
| прочее | `log.warn` |
- chatId — `message.recipient.chat_id`; пользователь — `message.sender` (`user_id`, `first_name`, `last_name`, `username` — nullable).
- Ошибки обработки → сообщение «❌ Произошла непредвиденная ошибка» (как в Telegram-боте).
- Отправка картинки: `POST /uploads?type=image` → загрузка файла на выданный URL → `token` → `POST /messages?chat_id=` с attachment `image`. Учесть `attachment.not.ready` — повтор с небольшой задержкой.
- Текст отправляется с `format: markdown`; проверить совместимость разметки приветственных сообщений `StartCommandProcessor` (`*bold*`, ```` ``` ````) с markdown Max.

## What Goes Where
- **Implementation Steps** (`[ ]` checkboxes): изменения в коде, миграции, тесты, документация.
- **Post-Completion** (no checkboxes): создание бота в Max, получение токена, деплой, ручная проверка.

## Implementation Steps

### Task 1: Проверка официального Java SDK Max (spike)

**Files:**
- Modify: `pom.xml`

- [ ] найти актуальные Maven-координаты официального SDK (`ru.max:max-bot-api` из `max-messenger/max-bot-api-client-java`): есть ли релиз (не SNAPSHOT) на Maven Central, совместимость с Java 11 и Jackson из Spring Boot 2.7
- [ ] проверить наличие в SDK нужных методов: getUpdates (long polling), sendMessage с inline-клавиатурой и markdown, upload image, answerOnCallback, deleteMessage
- [ ] добавить зависимость в `pom.xml`, `./mvnw -q compile`
- [ ] [!] если SDK не подходит (только SNAPSHOT/нет нужных методов/конфликт зависимостей) — остановиться и согласовать fallback: собственный `MaxApiClient` на `RestTemplate` за тем же интерфейсом `MaxClient` (Task 6); обновить план
- [ ] записать решение и версию в раздел Context этого плана

### Task 2: Миграция и модель пользователя `(platform, externalId)`

> **JPA skill required (spring-data-jpa).** Before writing any entity code: activate the skill and verify the implementation follows its rules. For any deviation — ask the developer before continuing.

**Files:**
- Create: `src/main/java/.../domain/BotPlatform.java`
- Create: `src/main/resources/db/changelog/changes/007-multi-platform-user.yaml`
- Modify: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Modify: `src/main/java/.../domain/user/User.java`
- Modify: `src/main/java/.../infrastructure/persistence/entity/UserEntity.java`
- Modify: `src/main/java/.../infrastructure/persistence/jpa/SpringDataUserRepository.java`
- Modify: `src/main/java/.../infrastructure/persistence/mapper/UserPersistenceMapper(Impl).java`
- Modify: `src/main/java/.../infrastructure/persistence/adapter/*UserRepository*` (реализация `UserRepositoryPort`)
- Modify: `src/main/java/.../application/port/out/UserRepositoryPort.java`
- Modify: `src/main/java/.../application/port/in/GetOrCreateUserCommand.java`, `UserService.java`
- Modify: `src/main/java/.../application/service/UserServiceImpl.java`
- Modify: `src/main/java/.../application/mapper/GetOrCreateUserCommandMapper*.java`
- Modify: `src/test/java/.../repository/UserEntityRepositoryTest.java`

- [ ] создать `BotPlatform { TELEGRAM, MAX }`
- [ ] написать миграцию `007` (platform с default `TELEGRAM`, rename `telegram_id → external_id`, снять NOT NULL/UNIQUE с `username`, unique `(platform, external_id)`)
- [ ] `UserEntity`: `externalId`, `platform` (`@Enumerated(EnumType.STRING)`), `username` nullable
- [ ] domain `User` + `GetOrCreateUserCommand`: `platform` + `externalId` вместо `telegramId`
- [ ] `UserRepositoryPort.findByPlatformAndExternalId`, `UserService.getOrCreateUser` / `findByPlatformAndExternalId`
- [ ] обновить `UserEntityRepositoryTest`: существующий пользователь получает `TELEGRAM` после миграции; два пользователя с одинаковым `externalId` на разных платформах; дубликат `(platform, externalId)` → ошибка; пользователь без username сохраняется
- [ ] тест `UserServiceImpl`: находит существующего / создаёт нового с учётом платформы
- [ ] run tests - must pass before next task

### Task 3: Платформа в domain-объектах и сигнатурах портов

**Files:**
- Create: `src/main/java/.../domain/command/CommandContext.java`
- Modify: `src/main/java/.../domain/Message.java`, `Callback.java`, `ButtonMessage.java`, `ButtonsMessage.java`
- Modify: `src/main/java/.../application/port/out/SendMessageService.java`, `SendPhotoService.java`, `SendButtonMessageService.java`, `SendButtonsMessageService.java`, `SendCallbackAnswerService.java`, `DeleteMessageService.java`
- Modify: `src/main/java/.../application/port/in/CommandProcessor.java`, `CommandHandler.java`, `ShowPricesButtonUseCase.java`
- Modify: `src/main/java/.../application/service/NumericMessageHandlerImpl.java`, `ShowPricesButtonUseCaseImpl.java`, `SuccessfulPaymentUseCaseImpl.java`, `PreCheckoutServiceImpl.java`
- Modify: `src/main/java/.../application/service/command/*CommandProcessor.java`, `CommandHandlerImpl.java`
- Modify: `src/main/java/.../application/service/callback/ShowPricesCallbackProcessor.java`, `CreateInvoiceCallbackProcessor.java`
- Modify: все `infrastructure/telegram/*Adapter.java`, `TelegramUpdateMapperImpl.java`, `handler/CallbackHandler.java`, `MnemocardsGeneratorBot.java`

- [ ] `Message` получает платформу через `user.getPlatform()` (отдельное поле не нужно — проверить, что `chatId` + `user` достаточно); `Callback`: `platform`, `externalUserId`, `String messageId`
- [ ] `CommandContext(platform, externalUserId, chatId)`; `CommandProcessor.process(CommandContext)`, `CommandHandler.handle(Command, CommandContext)`
- [ ] добавить `BotPlatform` в методы исходящих портов (см. таблицу в Technical Details) и протащить через все use case
- [ ] Telegram-сторона: мапперы и handler'ы проставляют `BotPlatform.TELEGRAM`; адаптеры принимают новый параметр (пока игнорируют)
- [ ] тесты: `NumericMessageHandlerImpl` (успех; нет токенов → `showPricesButton` с платформой пользователя; слишком длинное сообщение), `CommandHandlerImpl` (контекст доходит до процессора), `TelegramUpdateMapperImpl` (платформа TELEGRAM, externalId из `from.id`)
- [ ] run tests - must pass before next task

### Task 4: Маршрутизаторы исходящих портов

**Files:**
- Create: `src/main/java/.../infrastructure/messaging/PlatformAware.java` (`BotPlatform platform()`)
- Create: `src/main/java/.../infrastructure/messaging/MessengerPlatformNotEnabledException.java`
- Create: `src/main/java/.../infrastructure/messaging/Routing{SendMessage,SendPhoto,SendButtonMessage,SendButtonsMessage,SendCallbackAnswer,DeleteMessage}Service.java`
- Create: `src/main/java/.../infrastructure/messaging/platform/` — платформенные интерфейсы `Platform*Sender` (`extends PlatformAware`)
- Modify: `src/main/java/.../infrastructure/telegram/*Adapter.java` — реализуют `Platform*Sender` вместо портов
- Create: `src/test/java/.../infrastructure/messaging/Routing*Test.java`

- [ ] платформенные интерфейсы-«отправители» для каждого порта, Telegram-адаптеры переводятся на них (`platform() = TELEGRAM`)
- [ ] маршрутизатор на каждый порт: собирает `List<Platform*Sender>` в `EnumMap`, делегирует по `BotPlatform`; при отсутствии адаптера — `MessengerPlatformNotEnabledException`; при дубликате платформы — ошибка старта
- [ ] обсудить с разработчиком: один generic-маршрутизатор vs. отдельный класс на порт (дублирование vs. абстракция) — выбрать до реализации
- [ ] тесты маршрутизаторов: делегирование нужному адаптеру; выключенная платформа → исключение; дубликат → ошибка
- [ ] run tests - must pass before next task

### Task 5: Включение ботов через properties и условные бины Telegram

**Files:**
- Create: `src/main/java/.../infrastructure/configuration/TelegramBotProperties.java`
- Create: `src/main/java/.../infrastructure/configuration/MaxBotProperties.java`
- Create: `src/main/java/.../infrastructure/configuration/ConditionalOnTelegramBot.java`, `ConditionalOnMaxBot.java`
- Delete: `src/main/java/.../infrastructure/configuration/BotProperties.java`
- Modify: `src/main/resources/application.yaml`, `src/test/resources/application-test.yaml`
- Modify: все классы в `infrastructure/telegram/**` — `@ConditionalOnTelegramBot`
- Modify: `src/main/java/.../infrastructure/configuration/PaymentProperties.java` (не требовать токен при выключенном Telegram)
- Create: `src/test/java/.../infrastructure/configuration/BotEnablementTest.java`

- [ ] properties-классы `bot.telegram.*` / `bot.max.*` (`enabled`, `token`, `name`, `payments-enabled`, `polling.*`), обновить `application.yaml` (TG выключен, Max включён по умолчанию)
- [ ] мета-аннотации `@ConditionalOnProperty(prefix="bot.telegram", name="enabled", havingValue="true")` и аналог для Max
- [ ] пометить все Telegram-бины (бот, `TelegramApiClient`, адаптеры, handler'ы, `SendInvoiceAdapter`, `SendPreCheckoutAdapter`); проверить, что `telegrambots-spring-boot-starter` стартует без зарегистрированных ботов (при необходимости исключить его автоконфигурацию, когда TG выключен)
- [ ] `CreateInvoiceCallbackProcessor`/`PreCheckoutServiceImpl` не должны падать на старте без Telegram-бинов (`SendInvoiceService`/`SendPreCheckoutService` — через `ObjectProvider` или условие)
- [ ] тест через `ApplicationContextRunner`/`@SpringBootTest(properties=...)`: TG off → нет `MnemocardsGeneratorBot`; TG on → есть; оба on → у маршрутизаторов две платформы
- [ ] обновить `NumberMnemocardsGeneratorBotApplicationTests` под новые properties
- [ ] run tests - must pass before next task

### Task 6: Max API клиент и properties

**Files:**
- Create: `src/main/java/.../infrastructure/max/MaxClientConfiguration.java`
- Create: `src/main/java/.../infrastructure/max/MaxClient.java` (тонкая обёртка над SDK — для мокирования в тестах)
- Create: `src/test/java/.../infrastructure/max/MaxClientTest.java`

- [ ] бин клиента SDK с токеном из `MaxBotProperties` (`@ConditionalOnMaxBot`), понятная ошибка старта при пустом токене
- [ ] `MaxClient`: `getUpdates(marker, timeout, limit)`, `sendText(chatId, text)`, `sendButtons(chatId, text, buttons)`, `sendImage(chatId, File)` (upload + отправка, retry на `attachment.not.ready`), `answerCallback(callbackId, text)`, `deleteMessage(messageId)`
- [ ] тесты: маппинг вызовов на SDK (мок), retry при `attachment.not.ready`, ошибка SDK → `SendPhotoException` для фото / лог для текста
- [ ] run tests - must pass before next task

### Task 7: Max — исходящие адаптеры

**Files:**
- Create: `src/main/java/.../infrastructure/max/Max{SendMessage,SendPhoto,SendButtonMessage,SendButtonsMessage,SendCallbackAnswer,DeleteMessage}Adapter.java`
- Create: `src/test/java/.../infrastructure/max/Max*AdapterTest.java`

- [ ] реализовать `Platform*Sender` для `MAX` поверх `MaxClient` (`@ConditionalOnMaxBot`)
- [ ] кнопки → inline keyboard Max с `type: callback`, `payload` = JSON `CallbackData` (проверить лимит длины payload)
- [ ] markdown: адаптировать/проверить приветственные тексты под разметку Max
- [ ] тесты адаптеров (успех + ошибка клиента)
- [ ] run tests - must pass before next task

### Task 8: Max — приём апдейтов (long polling) и маппинг во входящие порты

**Files:**
- Create: `src/main/java/.../infrastructure/max/MaxBotPoller.java` (`SmartLifecycle`)
- Create: `src/main/java/.../infrastructure/max/MaxUpdateDispatcher.java`
- Create: `src/main/java/.../infrastructure/max/mapper/MaxUpdateMapper.java`
- Create: `src/test/java/.../infrastructure/max/MaxUpdateDispatcherTest.java`, `mapper/MaxUpdateMapperTest.java`

- [ ] `MaxBotPoller`: цикл `getUpdates` с `marker`, обработка в пуле потоков (как в Telegram-боте), backoff при ошибках сети, корректная остановка при shutdown
- [ ] `MaxUpdateMapper`: `message_created` → `GetOrCreateUserCommand(platform=MAX, externalId=sender.user_id, ...)` + `Message(chatId=recipient.chat_id)`; `message_callback` → `Callback(platform=MAX, messageId=mid, callbackQueryId=callback_id, ...)`
- [ ] `MaxUpdateDispatcher`: команда / число / `bot_started` → `START` / callback / прочее (таблица в Technical Details), обработка ошибок с ответом пользователю
- [ ] тесты маппера (все типы апдейтов, отсутствующие username/last_name) и диспетчера (маршрутизация, неизвестная команда, исключение в обработчике → сообщение об ошибке)
- [ ] run tests - must pass before next task

### Task 9: Отключение оплаты для Max

**Files:**
- Create: `src/main/java/.../application/port/out/PaymentAvailabilityPort.java`
- Create: `src/main/java/.../infrastructure/configuration/PropertiesPaymentAvailabilityAdapter.java`
- Modify: `src/main/java/.../application/service/ShowPricesButtonUseCaseImpl.java`
- Modify: `src/main/java/.../application/service/callback/ShowPricesCallbackProcessor.java`, `CreateInvoiceCallbackProcessor.java` (защита: не создавать заказ/инвойс, если оплата недоступна)
- Create: `src/test/java/.../application/service/ShowPricesButtonUseCaseImplTest.java`

- [ ] порт `isAvailable(BotPlatform)` + адаптер из `bot.<platform>.payments-enabled`
- [ ] `ShowPricesButtonUseCaseImpl`: оплата недоступна → `SendMessageService` с текстом «Пополнение баланса в этом мессенджере пока недоступно» вместо кнопки (касается и `/balance_refill`, и нехватки токенов)
- [ ] callback-процессоры оплаты: при недоступной платформе — ответ на callback и выход, без создания `Order`
- [ ] тесты: TG + оплата доступна → кнопка; MAX → текст; callback `CREATE_INVOICE` от MAX → заказ не создаётся
- [ ] run tests - must pass before next task

### Task 10: Verify acceptance criteria
- [ ] Max включён, Telegram выключен по умолчанию — приложение стартует без `BOT_TOKEN`/`PAYMENT_TOKEN`
- [ ] оба бота включены — контекст стартует, ответы уходят в правильный мессенджер
- [ ] существующие Telegram-пользователи и балансы сохранились после миграции
- [ ] в Max: `/start`, число → картинка, `/balance`, нехватка токенов → сообщение без оплаты
- [ ] run full test suite: `./mvnw test`
- [ ] verify test coverage meets project standard

### Task 11: [Final] Update documentation
- [ ] обновить README.md (если появится) / `docker-compose.yml` / `.env`-пример: `MAX_BOT_TOKEN`, `TELEGRAM_BOT_ENABLED`, `MAX_BOT_ENABLED`
- [ ] update CLAUDE.md if new patterns discovered (маршрутизация по `BotPlatform`, `@ConditionalOn*Bot`)
- [ ] move this plan to `docs/plans/completed/`

## Post-Completion
*Items requiring manual intervention or external systems - no checkboxes, informational only*

**Manual verification:**
- создать бота в Max (business.max.ru → платформа для разработчиков), получить токен, положить в `MAX_BOT_TOKEN`
- убедиться, что у бота нет активного webhook-подписчика (`GET /subscriptions`), иначе long polling не получит апдейты
- вручную пройти сценарии: `/start` (включая кнопку «Начать» → `bot_started`), отправка номера телефона, `/balance`, `/balance_refill`, исчерпание бесплатных токенов
- проверить отображение markdown приветственных сообщений в клиенте Max (мобильный + веб)

**External system updates:**
- в окружении прод-сервера добавить `MAX_BOT_TOKEN`, `MAX_BOT_ENABLED=true`, `TELEGRAM_BOT_ENABLED=false`
- подключение оплаты для Max (ЮКасса / платёжный API Max) — отдельная задача в будущем: включить `bot.max.payments-enabled` и реализовать `SendInvoiceService` для Max
