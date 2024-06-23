## Friend Alert Bot

### About

Friend Alert is a Telegram bot, made for School 21 students.
It allows users to subscribe for their friends.
If user is subscribed to someone, they will get a notification when their friend enters or leaves School 21.

### Stack

- Spring Boot
- Hibernate + Liquibase + PostgreSQL
- JUnit + TestContainers

### How to run

1. Add `school21.platform.token` property in `Bearer abc123` format
2. Add `telegram.bot.token` property in `avc:abc` format
3. Run PostgreSQL (`docker compose up` in project root directory)
4. Run `FriendAlertBotApplication.class`
