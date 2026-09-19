# StreamSwitch

**Pay-TV subscription discovery and comparison platform**

Browse packages from providers like DStv, Canal+, and StarTimes. View prices, channels, and compare packages — then head to the provider's official site to subscribe.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Bean Validation**

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Postman (for API testing)

## Database Setup

1. Open PostgreSQL and run:

```sql
CREATE DATABASE streamswitch_db;
```

2. The `application.properties` is configured with:
   - **Database:** `streamswitch_db`
   - **Username:** `postgres` (set via `SPRING_DATASOURCE_USERNAME` env var)
   - **Password:** set via `SPRING_DATASOURCE_PASSWORD` env var
   - **Port:** `5432`

3. Hibernate will auto-create/update tables on startup (`ddl-auto=update`).

## How to Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The app starts at `http://localhost:8080`

## API Endpoints

### TV Providers (`/api/providers`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/providers` | Create a new provider |
| GET | `/api/providers` | Get all providers |
| GET | `/api/providers/{id}` | Get provider by ID |
| PUT | `/api/providers/{id}` | Update a provider |
| DELETE | `/api/providers/{id}` | Delete a provider |

### Subscription Packages (`/api/packages`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/packages/provider/{providerId}` | Create package under a provider |
| GET | `/api/packages` | Get all packages |
| GET | `/api/packages/{id}` | Get package by ID |
| GET | `/api/packages/provider/{providerId}` | Get packages by provider |
| PUT | `/api/packages/{id}` | Update a package |
| DELETE | `/api/packages/{id}` | Delete a package |
| POST | `/api/packages/{id}/channels/{channelId}` | Add channel to package |
| DELETE | `/api/packages/{id}/channels/{channelId}` | Remove channel from package |

### Channels (`/api/channels`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/channels` | Create a new channel |
| GET | `/api/channels` | Get all channels |
| GET | `/api/channels/{id}` | Get channel by ID |
| GET | `/api/channels/genre/{genre}` | Get channels by genre |
| PUT | `/api/channels/{id}` | Update a channel |
| DELETE | `/api/channels/{id}` | Delete a channel |

## Entity Relationships

```
TVProvider (1) ──── (*) SubscriptionPackage
SubscriptionPackage (*) ──── (*) Channel (Many-to-Many)
```

## Postman Collection

Import `postman/StreamSwitch_API_Collection.json` into Postman to get pre-configured requests for all endpoints.

## Sample Request (Create Provider)

```json
POST http://localhost:8080/api/providers
Content-Type: application/json

{
  "name": "DStv",
  "description": "MultiChoice satellite television service",
  "websiteUrl": "https://www.dstv.com",
  "country": "Nigeria"
}
```

## Project Structure

```
src/main/java/com/streamswitch/
├── StreamSwitchApplication.java
├── controller/
│   ├── TVProviderController.java
│   ├── SubscriptionPackageController.java
│   └── ChannelController.java
├── entity/
│   ├── TVProvider.java
│   ├── SubscriptionPackage.java
│   └── Channel.java
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── TVProviderRepository.java
│   ├── SubscriptionPackageRepository.java
│   └── ChannelRepository.java
└── service/
    ├── TVProviderService.java
    ├── SubscriptionPackageService.java
    └── ChannelService.java
```
