# WhatsApp Clone Backend

A modern real-time messaging application backend built with Spring Boot, featuring direct messaging, group chats, file sharing, and user blocking functionality.

## Features

### Core Messaging
- **Real-time messaging** with WebSocket support
- **Message states** (Sent, Delivered, Seen)
- **File sharing** with support for images, audio, and video
- **Message history** and chat persistence

### User Management
- **OAuth2 authentication** with Keycloak integration
- **User synchronization** with identity provider
- **Online status** tracking
- **User blocking/unblocking** functionality / Experimental

### Chat Features
- **Direct messaging** between users
- **Group chats** with member management
- **Admin permissions** for group management
- **Chat filtering** (exclude blocked users) / Experimental

### Technical Features
- **RESTful API** with OpenAPI documentation
- **Real-time notifications** via WebSocket
- **CORS configuration** for frontend integration
- **Multi-part file uploads** with organized storage

## Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Security**: Spring Security + OAuth2 Resource Server
- **Authentication**: Keycloak
- **Database**: PostgreSQL with JPA/Hibernate
- **Real-time**: WebSocket with STOMP protocol
- **Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Prerequisites

- Java 17 or higher
- Docker & Docker Compose
- Maven

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ./path_to_folder
```

### 2. Start Infrastructure Services

```yaml
services:
  postgres:
    container_name: postgres-sql-wac
    image: postgres
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
      PGDATA: /var/lib/postgresql/data
      POSTGRES_DB: whatsapp_clone
    volumes:
      - postgres:/data/postgres
    ports:
      - 5432:5432
    networks:
      - whatsapp-clone
    restart: unless-stopped
  keycloak:
    container_name: keycloak-wac
    image: quay.io/keycloak/keycloak:26.0.0
    ports:
      - 9090:8080
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    networks:
      - whatsapp-clone
    command:
      - "start-dev"

networks:
  whatsapp-clone:
    driver: bridge

volumes:
  postgres:
    driver: local
```

```bash
docker-compose up
```

This will start:
- PostgreSQL database on port `5432`
- Keycloak identity server on port `9090`

### 3. Configure Keycloak

1. Access Keycloak admin console: http://localhost:9090
2. Login with `admin/admin`
3. Create a new realm named `whatsapp-clone`
4. Configure OAuth2 clients as needed

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Architecture Overview

### Package Structure

```
com.whatsappclone/
├── configs/           # Configuration classes
├── controllers/       # REST endpoints
├── dtos/             # Data Transfer Objects
│   ├── requests/     # Request DTOs
│   └── responses/    # Response DTOs
├── entities/         # JPA entities
├── events/           # Event objects for notifications
├── mappers/          # Entity-DTO mappers
├── repositories/     # JPA repositories
├── services/         # Business logic
└── utils/            # Utility classes
```

## 🔧 Configuration

### Application Properties

Key configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/whatsapp_clone
    username: admin
    password: admin123
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/whatsapp-clone

application:
  file:
    uploads:
      media-output-path: ./uploads
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/whatsapp_clone` |
| `DATABASE_USERNAME` | Database username | `admin` |
| `DATABASE_PASSWORD` | Database password | `admin123` |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URI | `http://localhost:9090/realms/whatsapp-clone` |
| `FILE_UPLOAD_PATH` | File upload directory | `./uploads` |

## 📡 API Endpoints

### Authentication
All endpoints require valid JWT token except WebSocket and documentation endpoints.

### Users
- `GET /api/v1/users` - Get all users (excluding self and blocked)
- `GET /api/v1/users/all` - Get all users without filters

### Chats
- `POST /api/v1/chats` - Create new chat
- `GET /api/v1/chats` - Get user's chats

### Messages
- `POST /api/v1/messages` - Send text message
- `POST /api/v1/messages/upload-media` - Send media message
- `GET /api/v1/messages/chat/{chatId}` - Get chat messages
- `PATCH /api/v1/messages` - Mark messages as seen

### Groups / Experimental
- `POST /api/v1/groups` - Create group
- `GET /api/v1/groups` - Get user's groups
- `POST /api/v1/groups/{groupId}/members` - Add member to group

### Blocked Users / Experimental
- `POST /api/v1/blocked-users/block` - Block user
- `DELETE /api/v1/blocked-users/unblock/{userId}` - Unblock user
- `GET /api/v1/blocked-users` - Get blocked users list
- `GET /api/v1/blocked-users/is-blocked/{userId}` - Check if user is blocked

## WebSocket Integration

### Connection
Connect to WebSocket endpoint: `ws://localhost:8080/ws`

## Database Schema

### Core Tables
- `users` - User information and status
- `chats` - Chat conversations (direct and group)
- `messages` - Message content and metadata
- `groups` - Group information and settings
- `blocked_users` - User blocking relationships

### Relationships
- Users can have multiple chats (1:N)
- Chats contain multiple messages (1:N)
- Groups have multiple members (N:M)
- Users can block multiple users (1:N)

## Security Features

- **OAuth2 Resource Server** with JWT validation
- **CORS Configuration** for cross-origin requests
- **User blocking** prevents message exchange
- **Group admin permissions** for member management
- **File upload validation** and organized storage

## Database Migration
The application uses Hibernate's `ddl-auto: update`.

---

**Note**: This is a demonstration project. For production use, ensure proper security configurations, monitoring, and testing procedures are in place.
