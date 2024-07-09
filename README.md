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

### How to run locally

1. In file [application-auth.properties](src/main/resources/application-auth.properties)
specify School 21 platform credentials and Telegram bot token. 
You might want to run the following script to prevent git from commiting this file:
   ```bash
   git update-index --skip-worktree -- src/main/resources/application-auth.properties
   ```
2. Run PostgreSQL and ClickHouse:
   ```bash
   docker compose up postgres clickhouse -d
   ```
3. Run [FriendAlertBotApplication.java](src/main/java/edu/ivanuil/friendalertbot/FriendAlertBotApplication.java)

### How to run as a Docker container

1. Repeat step 1 from **_How to run locally_**
2. Run docker compose:
   ```bash
   docker compose up -d
   ```
