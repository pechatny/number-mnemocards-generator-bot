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
4. Все исходящие порты сообщений реализует один **`MessengerRouter`** в `infrastructure/messaging/`, который держит `Map<BotPlatform, MessengerGateway>` и делегирует. Каждая платформа реализует один интерфейс `MessengerGateway`. Платформенные адаптеры регистрируются только если их бот включён — маршрутизатор при обращении к выключенной платформе бросает понятное исключение.
5. Включение ботов — `@ConditionalOnProperty` через мета-аннотации `@ConditionalOnTelegramBot` / `@ConditionalOnMaxBot`.
6. Доступность оплаты — новый исходящий порт `PaymentAvailabilityPort.isAvailable(BotPlatform)`, реализуемый из properties (`bot.<platform>.payments-enabled`). `ShowPricesButtonUseCaseImpl` при недоступной оплате отправляет текст вместо кнопки.
7. Max-адаптер (`infrastructure/max/`) на собственном тонком клиенте `RestTemplate` (официального Java SDK на Maven Central нет — см. Task 1): long polling `GET /updates` в отдельном потоке (`SmartLifecycle`), маппинг апдейтов в те же входящие порты (`NumericMessageHandler`, `CommandHandler`, `CallbackService`).

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

- [x] найти актуальные Maven-координаты официального SDK: в организации `max-messenger` Java-клиента нет (только TS/Go/Python + OpenAPI `max-messenger/api-schema`); `max-messenger-client/max-bot-api-client-java` — неофициальный, только `ru.max:max-bot-api:0.0.6-SNAPSHOT`, на Maven Central отсутствует (`ru.max`, `chat.max` → 404); `ru.etsft.max` есть на Central, но требует Java 21
- [x] [!] SDK не подходит → по согласованию с разработчиком: **собственный клиент на `RestTemplate`** по схеме `max-messenger/api-schema` (Task 6), без новых зависимостей
- [x] решение записано в план (Task 6 обновлена)

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

- [x] создать `BotPlatform { TELEGRAM, MAX }`
- [x] написать миграцию `007` (platform с default `TELEGRAM`, rename `telegram_id → external_id`, снять NOT NULL/UNIQUE с `username`, unique `(platform, external_id)`)
- [x] `UserEntity`: `externalId`, `platform` (`@Enumerated(EnumType.STRING)`), `username` nullable
- [x] domain `User` + `GetOrCreateUserCommand`: `platform` + `externalId` вместо `telegramId`
- [x] `UserRepositoryPort.findByPlatformAndExternalId`, `UserService.getOrCreateUser` / `findByPlatformAndExternalId`
- [x] обновить `UserEntityRepositoryTest`: существующий пользователь получает `TELEGRAM` после миграции; два пользователя с одинаковым `externalId` на разных платформах; дубликат `(platform, externalId)` → ошибка; пользователь без username сохраняется
- [x] тест `UserServiceImpl`: находит существующего / создаёт нового с учётом платформы
- [+] вызовы `findByTelegramId` в `BalanceCommandProcessor` / `CreateInvoiceCallbackProcessor` временно переведены на `BotPlatform.TELEGRAM` — обобщено в Task 3
- [+] тестовая инфраструктура: `BaseIntegrationTest` — singleton-контейнер (был остановлен между классами при закэшированном контексте), `NumberMnemocardsGeneratorBotApplicationTests` наследует `BaseIntegrationTest`
- [x] run tests - must pass before next task (47/47, `./mvnw test`)

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

- [x] `Message` получает платформу через `user.getPlatform()` (отдельное поле не понадобилось); `Callback`: `platform`, `externalUserId`, `String messageId`
- [x] `CommandContext(platform, externalUserId, chatId)`; `CommandProcessor.process(CommandContext)`, `CommandHandler.handle(Command, CommandContext)`
- [x] добавить `BotPlatform` в методы исходящих портов (см. таблицу в Technical Details) и протащить через все use case
- [x] Telegram-сторона: мапперы и handler'ы проставляют `BotPlatform.TELEGRAM`; адаптеры принимают новый параметр (пока игнорируют)
- [x] тесты: `NumericMessageHandlerImpl` (успех; нет токенов → `showPricesButton` с платформой пользователя; слишком длинное сообщение), `CommandHandlerImpl` (контекст доходит до процессора), `TelegramUpdateMapperImpl` (платформа TELEGRAM, externalId из `from.id`)
- [+] `ButtonMessage` / `ButtonsMessage` переписаны с delombok-кода на `@Value @Builder` (как `Callback`), API не изменилось
- [+] `SuccessfulPaymentUseCaseImpl`: платформа берётся из `order.getUser()`, `messageId` инвойса передаётся строкой
- [+] тесты: `NumericMessageHandlerImplTest`, `CommandHandlerImplTest`, `BalanceCommandProcessorTest`, `TelegramInboundMappingTest` (маппер + `CallbackHandler`)
- [x] run tests - must pass before next task (56/56)

### Task 4: Единый `MessengerGateway` и маршрутизатор исходящих портов

> Решение разработчика: один интерфейс шлюза на платформу + один маршрутизатор вместо отдельного роутера на каждый порт.

**Files:**
- Create: `src/main/java/.../infrastructure/messaging/MessengerGateway.java` (`platform()` + 6 методов отправки)
- Create: `src/main/java/.../infrastructure/messaging/MessengerRouter.java` (реализует `SendMessageService`, `SendPhotoService`, `SendButtonMessageService`, `SendButtonsMessageService`, `SendCallbackAnswerService`, `DeleteMessageService`)
- Create: `src/main/java/.../infrastructure/messaging/MessengerPlatformNotEnabledException.java`
- Create: `src/main/java/.../infrastructure/telegram/TelegramGateway.java` (делегирует существующим Telegram-адаптерам)
- Modify: `src/main/java/.../infrastructure/telegram/{SendMessage,SendPhoto,SendButtonMessage,SendButtonsMessage,SendCallbackAnswer,DeleteMessage}Adapter.java` — больше не реализуют порты, сигнатуры без `BotPlatform`
- Create: `src/test/java/.../infrastructure/messaging/MessengerRouterTest.java`

- [x] `MessengerGateway`: `platform()`, `sendMessage`, `sendPhoto`, `sendButtonMessage`, `sendButtonsMessage`, `sendCallbackAnswer`, `deleteMessage`
- [x] `MessengerRouter`: собирает `List<MessengerGateway>` в `EnumMap`; дубликат платформы → ошибка старта; выключенная платформа → `MessengerPlatformNotEnabledException`
- [x] `TelegramGateway` + перевод Telegram-адаптеров на внутренние компоненты (не реализуют порты)
- [x] тесты маршрутизатора: каждый из 6 портов делегирует нужному шлюзу; выключенная платформа → исключение; дубликат → ошибка
- [x] run tests - must pass before next task (64/64, контекст стартует с `MessengerRouter` как единственной реализацией портов)

### Task 5: Включение ботов через properties и условные бины Telegram

**Files:**
- Create: `src/main/java/.../infrastructure/configuration/TelegramBotProperties.java`
- Create: `src/main/java/.../infrastructure/configuration/MaxBotProperties.java`
- Create: `src/main/java/.../infrastructure/configuration/ConditionalOnTelegramBot.java`, `ConditionalOnMaxBot.java`
- Delete: `src/main/java/.../infrastructure/configuration/BotProperties.java`
- Modify: `src/main/resources/application.yaml`, `src/test/resources/application-test.yaml`
- Modify: все классы в `infrastructure/telegram/**` — `@ConditionalOnTelegramBot`
- Modify: `src/main/java/.../infrastructure/configuration/PaymentProperties.java` (не требовать токен при выключенном Telegram)
- Create: `src/main/java/.../infrastructure/configuration/TelegramPaymentsDisabledConfiguration.java`
- Create: `src/test/java/.../infrastructure/configuration/BotEnablementTest.java`, `BotsDisabledContextTest.java`, `TelegramEnabledContextTest.java`

- [x] properties-классы `bot.telegram.*` / `bot.max.*` (`enabled`, `token`, `name`, `payments-enabled`, `polling.*`), обновить `application.yaml` (TG выключен, Max включён по умолчанию)
- [x] мета-аннотации `@ConditionalOnProperty(prefix="bot.telegram", name="enabled", havingValue="true")` и аналог для Max
- [x] пометить все Telegram-бины (15 классов в `infrastructure/telegram/**`); автоконфигурация стартера отключается через `telegrambots.enabled: ${bot.telegram.enabled}`
- [x] `CreateInvoiceCallbackProcessor`/`PreCheckoutServiceImpl` не падают на старте без Telegram: `TelegramPaymentsDisabledConfiguration` даёт заглушки `SendInvoiceService`/`SendPreCheckoutService` (бросают `UnsupportedOperationException`), application-слой не тронут
- [x] тест через `ApplicationContextRunner`/`@SpringBootTest(properties=...)`: TG off → нет `MnemocardsGeneratorBot`; TG on → есть; оба on → у маршрутизаторов две платформы
- [x] обновить `NumberMnemocardsGeneratorBotApplicationTests` под новые properties
- [+] токен проверяется в `@PostConstruct` properties-классов только при `enabled=true`; неиспользуемый `bot.chat-id` удалён
- [+] `MessengerRouter.enabledPlatforms()` — для логов и тестов
- [x] run tests - must pass before next task (79/79)

### Task 6: Max API клиент (RestTemplate) и properties

**Files:**
- Create: `src/main/java/.../infrastructure/max/MaxClientConfiguration.java`
- Create: `src/main/java/.../infrastructure/max/MaxClient.java` (RestTemplate, `https://platform-api.max.ru`, заголовок `Authorization: <token>`)
- Create: `src/main/java/.../infrastructure/max/dto/*` (только нужные DTO: Update, Message, Recipient, User, Callback, NewMessageBody, Attachment, InlineKeyboard, UploadEndpoint, ...)
- Create: `src/main/java/.../infrastructure/max/MaxApiException.java`
- Create: `src/test/java/.../infrastructure/max/MaxClientTest.java`, `MaxClientConfigurationTest.java`

- [x] бин `RestTemplate` для Max с токеном из `MaxBotProperties` (`@ConditionalOnMaxBot`), read-timeout больше long-polling timeout; понятная ошибка старта при пустом токене
- [x] DTO по схеме `max-messenger/api-schema` (Jackson, `@JsonIgnoreProperties(ignoreUnknown = true)`, snake_case)
- [x] `MaxClient`: `getUpdates(marker, timeout, limit)`, `sendText(chatId, text)`, `sendButtons(chatId, text, buttons)`, `sendImage(chatId, File)` (`POST /uploads?type=image` → multipart на выданный URL → token → `POST /messages`, retry на `attachment.not.ready`), `answerCallback(callbackId, text)`, `deleteMessage(messageId)`
- [x] тесты через `MockRestServiceServer`: формат запросов (URL, заголовок, тело), разбор ответов, retry при `attachment.not.ready`, HTTP-ошибка → `MaxApiException`
- [+] два RestTemplate: API (`Authorization: <token>`, без `Bearer`) и upload — без токена, т.к. URL загрузки на стороннем хосте
- [+] URI: строгое кодирование переменных (`TEMPLATE_AND_VALUES`) — `+` в `callback_id` иначе читается как пробел; URL загрузки передаётся как `java.net.URI`, без повторного кодирования
- [+] `bot.max.upload.retry-attempts` / `retry-delay-millis` для `attachment.not.ready`; повтор только для этого кода ошибки
- [+] `MaxButton.callback` проверяет лимит payload 1024 символа
- [!] документация Max: long polling «не подходит для production», рекомендуют webhook — оставляем long polling (как в плане), webhook — возможная отдельная задача
- [x] run tests - must pass before next task (93/93)

### Task 7: Max — исходящие адаптеры

**Files:**
- Create: `src/main/java/.../infrastructure/max/MaxGateway.java`
- Create: `src/main/java/.../infrastructure/max/MaxMarkdown.java`
- Create: `src/test/java/.../infrastructure/max/MaxGatewayTest.java`, `MaxMarkdownTest.java`
- Create: `src/test/java/.../infrastructure/configuration/MaxEnabledContextTest.java`, `BothBotsEnabledContextTest.java`

- [x] реализовать `MaxGateway implements MessengerGateway` поверх `MaxClient` (`@ConditionalOnMaxBot`)
- [x] кнопки → inline keyboard Max с `type: callback`, `payload` = JSON `CallbackData` (проверить лимит длины payload)
- [x] markdown: тексты бота — Telegram Markdown (`*жирный*`), в Max `*x*` — курсив; `MaxMarkdown` переводит одиночные `*` в `**`, двойные и код не трогает. Тексты в application не менялись
- [x] тесты `MaxGateway` (успех + ошибка клиента)
- [+] обработка ошибок как в Telegram-адаптерах: текст/кнопки/удаление — лог; картинка → `SendPhotoException`; ответ на callback — пробрасывается
- [+] контекстные тесты с `@MockBean MaxClient`: только Max (по умолчанию) и оба бота одновременно
- [!] `MaxGateway` использует `infrastructure.telegram.dto.CallbackData` (как и application-слой) — кандидат на перенос в общий пакет, вне scope
- [x] run tests - must pass before next task (113/113)

### Task 8: Max — приём апдейтов (long polling) и маппинг во входящие порты

**Files:**
- Create: `src/main/java/.../infrastructure/max/MaxBotPoller.java` (`SmartLifecycle`)
- Create: `src/main/java/.../infrastructure/max/MaxUpdateDispatcher.java`
- Create: `src/main/java/.../infrastructure/max/mapper/MaxUpdateMapper.java`
- Create: `src/test/java/.../infrastructure/max/MaxUpdateDispatcherTest.java`, `mapper/MaxUpdateMapperTest.java`, `MaxBotPollerTest.java`, `MaxBotFlowIntegrationTest.java`, `MaxTestData.java`

- [x] `MaxBotPoller`: цикл `getUpdates` с `marker`, обработка в пуле потоков (как в Telegram-боте), backoff при ошибках сети, корректная остановка при shutdown
- [x] `MaxUpdateMapper`: `message_created` → `GetOrCreateUserCommand(platform=MAX, externalId=sender.user_id, ...)` + `Message(chatId=recipient.chat_id)`; `message_callback` → `Callback(platform=MAX, messageId=mid, callbackQueryId=callback_id, ...)`
- [x] `MaxUpdateDispatcher`: команда / число / `bot_started` → `START` / callback / прочее (таблица в Technical Details), обработка ошибок с ответом пользователю
- [x] тесты маппера (все типы апдейтов, отсутствующие username/last_name) и диспетчера (маршрутизация, неизвестная команда, исключение в обработчике → сообщение об ошибке)
- [+] callback без `message` (сообщение с кнопкой удалено) → ответ «Кнопка устарела», без падения
- [+] `bot.max.polling.error-backoff-millis` / `max-error-backoff-millis` / `worker-threads`; пустой ответ `/updates` тоже уходит в паузу
- [+] сквозной тест на реальном контексте и БД: `/updates` → пользователь `MAX` в БД → `sendImage` в Max
- [x] run tests - must pass before next task (138/138)

### Task 9: Отключение оплаты для Max

**Files:**
- Create: `src/main/java/.../application/port/out/PaymentAvailabilityPort.java`
- Create: `src/main/java/.../infrastructure/configuration/PropertiesPaymentAvailabilityAdapter.java`
- Modify: `src/main/java/.../application/service/ShowPricesButtonUseCaseImpl.java`
- Modify: `src/main/java/.../application/service/callback/ShowPricesCallbackProcessor.java`, `CreateInvoiceCallbackProcessor.java` (защита: не создавать заказ/инвойс, если оплата недоступна)
- Create: `src/test/java/.../application/service/ShowPricesButtonUseCaseImplTest.java`
- Create: `src/test/java/.../application/service/callback/PaymentCallbackProcessorsTest.java`
- Create: `src/test/java/.../infrastructure/configuration/PropertiesPaymentAvailabilityAdapterTest.java`

- [x] порт `isAvailable(BotPlatform)` + адаптер из `bot.<platform>.payments-enabled`
- [x] `ShowPricesButtonUseCaseImpl`: оплата недоступна → `SendMessageService` с текстом «Пополнение баланса в этом мессенджере пока недоступно» вместо кнопки (касается и `/balance_refill`, и нехватки токенов)
- [x] callback-процессоры оплаты: при недоступной платформе — ответ на callback и выход, без создания `Order`
- [x] тесты: TG + оплата доступна → кнопка; MAX → текст; callback `CREATE_INVOICE` от MAX → заказ не создаётся
- [+] `bot.max.payments-enabled=true` → ошибка старта: `SendInvoiceService` есть только у Telegram, флаг отправил бы инвойс Max-чата в Telegram
- [+] сквозной тест: `/balance_refill` и кнопка с ценой в Max → «недоступно», заказ в БД не создаётся
- [x] run tests - must pass before next task (151/151)

### Task 10: Verify acceptance criteria
- [x] Max включён, Telegram выключен по умолчанию — приложение стартует без `BOT_TOKEN`/`PAYMENT_TOKEN`: реальный запуск jar (профиль `dev`, отдельный Postgres в Docker, фейковый `MAX_BOT_TOKEN`) — `Включённые мессенджеры: [MAX]`, Telegram-бины не создаются; Max API отвечает `401 verify.token`, поллер уходит в backoff 1→2→4 с; SIGTERM — остановка мгновенная
- [x] оба бота включены — контекст стартует, ответы уходят в правильный мессенджер (`BothBotsEnabledContextTest`, `MessengerRouterTest`)
- [x] существующие Telegram-пользователи и балансы сохранились после миграции (`MultiPlatformUserMigrationTest`: 001–006 → данные → 007)
- [x] в Max: `/start` (кнопка «Начать»), число → картинка, `/balance`, `/balance_refill` и кнопка оплаты → «недоступно» (`MaxBotFlowIntegrationTest`); нехватка токенов → `NumericMessageHandlerImplTest` + `ShowPricesButtonUseCaseImplTest`
- [!] найдено при реальном запуске: поток поллера был daemon → без Telegram-бота JVM завершалась сразу после старта. Исправлено (`setDaemon(false)`), тест `shouldKeepJvmAliveWithNonDaemonPollingThread`
- [!] найдено сквозным тестом: `/balance` от пользователя, ещё не присылавшего число, падал на `Optional.get()` (было и в Telegram). Исправлено: показывается стартовый баланс `NEW_USER_INIT_TOKENS`
- [+] `MaxBotFlowIntegrationTest`: мок `MaxClient` не перестраивается во время теста (стабинг Mockito не потокобезопасен при работающем поллере) — события идут через очередь
- [x] run full test suite: `./mvnw test` (156/156, три прогона подряд стабильны)
- [x] verify test coverage meets project standard — инструмента покрытия (JaCoCo) и порога в проекте нет; новые классы покрыты unit- и интеграционными тестами

### Task 11: [Final] Update documentation
- [x] `.env.example`: `TELEGRAM_BOT_ENABLED`, `MAX_BOT_ENABLED`, `MAX_BOT_TOKEN` (README в проекте нет; `docker-compose.yml` содержит только Postgres — менять нечего; `.env` / `.env.docker` — локальные файлы разработчика, не трогались)
- [x] CLAUDE.md в проекте нет — не создавался; паттерны (маршрутизация по `BotPlatform`, `@ConditionalOn*Bot`) описаны в Solution Overview этого плана
- [x] move this plan to `docs/plans/completed/`

## Post-Completion
*Items requiring manual intervention or external systems - no checkboxes, informational only*

**Manual verification:**
- создать бота в Max (business.max.ru → платформа для разработчиков), получить токен, положить в `MAX_BOT_TOKEN`
- убедиться, что у бота нет активного webhook-подписчика (`GET /subscriptions`), иначе long polling не получит апдейты
- вручную пройти сценарии: `/start` (включая кнопку «Начать» → `bot_started`), отправка номера телефона, `/balance`, `/balance_refill`, исчерпание бесплатных токенов
- проверить отображение markdown приветственных сообщений в клиенте Max (мобильный + веб)

**External system updates:**
- в окружении прод-сервера добавить `MAX_BOT_TOKEN`, `MAX_BOT_ENABLED=true`, `TELEGRAM_BOT_ENABLED=false`
- перед деплоем прогнать миграцию `007` на копии продовой БД (имя ограничения `user_username_key` подтверждено на Postgres 15 в тестах)
- при деплое с выключенным Telegram процесс держит живым поток `max-poller`; проверить, что после старта в логе есть `Включённые мессенджеры: [MAX]` и `Max bot polling started`
- документация Max называет long polling неподходящим для production и рекомендует webhook — при проблемах с доставкой событий вынести переход на webhook в отдельную задачу
- подключение оплаты для Max (ЮКасса / платёжный API Max) — отдельная задача в будущем: включить `bot.max.payments-enabled` и реализовать `SendInvoiceService` для Max
