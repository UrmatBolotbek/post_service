# Post Service

**Post Service** is a core microservice in our social network platform that manages user posts and powers the dynamic News Feed feature. It enables users to create, manage, and share content—keeping their followers up-to-date with real-time updates and interactions. This service is a key component in our ecosystem, working alongside other microservices such as user_service, project_service, and notification_service to deliver a seamless social experience.

---

## Overview

- **Content Management:**  
  The service manages posts created by users. Each post contains rich text content, author and project associations, publication flags and timestamps, as well as metadata such as hashtags, likes, comments, and views.

- **News Feed Feature:**  
  The News Feed is a central feature that aggregates posts from the users a person follows, displaying them in reverse chronological order. To deliver a responsive experience:
  - **Caching with Redis:**  
    Posts (or their IDs) are cached in Redis to serve the feed quickly without overloading the primary database. A dedicated Redis collection holds the list of post IDs for each user's feed, while a separate cache stores the detailed JSON representation of each post.
  - **Asynchronous Updates:**  
    When a new post is created, it is saved in the database and then asynchronously published to Kafka. A consumer process then updates the feed cache for all followers of the post's author. This decouples the heavy work of fan-out from the critical path of post creation.
  - **Scalability Considerations:**  
    The design takes into account the high frequency of feed retrievals and aims to minimize duplicate data storage while ensuring that the feed remains responsive even under high load.

- **Additional Interactions:**  
  Beyond basic post management, the service handles interactions like likes, comments, and the integration of advertisements (ads) associated with posts. These interactions are also reflected in the feed, with separate asynchronous updates and caching strategies for performance and scalability.

---

## Data Model

The core `Post` entity includes:
- **Content:** A textual field (up to 4096 characters) containing the post content.
- **Associations:** References to the author, associated project, and related entities such as comments, likes, and resources.
- **Publication Flags:** Indicators for whether a post is published or deleted, along with publication and scheduling timestamps.
- **Metadata:** A list of hashtags (stored as JSON), view counts, and verification status with timestamps.

---

### Technologies Used

- [Spring Boot](https://spring.io/projects/spring-boot) – Main framework for building the application.
- [PostgreSQL](https://www.postgresql.org/) – Primary relational database.
- [Redis](https://redis.io/) – Used as a cache and for pub/sub messaging.
- [Testcontainers](https://testcontainers.com/) – For isolated testing with a real database.
- [Liquibase](https://www.liquibase.org/) – For managing database schema migrations.
- [Gradle](https://gradle.org/) – Build system.

### Database & Infrastructure

- **Database:**  
  PostgreSQL is managed in a separate service ([infra](../infra)). Liquibase automatically applies necessary migrations at startup.
- **Redis:**  
  Redis is deployed as a single instance in the [infra](../infra) service, and is used for caching posts, user data, and feed lists.
- **Testing:**  
  Integration tests use Testcontainers to spin up isolated instances of PostgreSQL and Redis. The code demonstrates data access via both JdbcTemplate and JPA (Hibernate).

---

### Conclusion

Post Service is integral to our social network, powering the creation, management, and distribution of user content. With its robust News Feed implementation and efficient caching strategies, it ensures a responsive and scalable user experience.
