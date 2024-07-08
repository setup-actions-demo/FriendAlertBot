## Friend Alert Bot

This repository is a part of FriendAlert project.
Root repository of FriendAlert project: https://github.com/Ivanuil/FriendAlert

### About project

Friend Alert is a Telegram bot, made for School 21 students.
It allows users to subscribe for their friends entering and leaving campus.
It also provides extensive statistics on School 21 campuses and participants.

Project includes two microservices:
1. FriendAlertBot (java) ← **This repository**

   FriendAlertBot gathers data from School 21 platform and sends out messages for users.
   It also logs data for analytics.

2. FriendAlertAnalytics (python)

   FriendAlertAnalytics provides analytics data visualisation

### Stack

- Spring Boot
- Hibernate + Liquibase + PostgreSQL
- ClickHouse + ClickHouseHttpClient
- JUnit + TestContainers

### How to run

1. Add `school21.platform.token` property in `Bearer abc123` format
2. Add `telegram.bot.token` property in `avc:abc` format
3. Run PostgreSQL (`docker compose up` in project root directory)
4. Run `FriendAlertBotApplication.class`
