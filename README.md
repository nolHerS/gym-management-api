# gym-management-api
Gym management application for trainers to create, assign and manage personalized workout routines and nutrition plans for their clients.

## API documentation and authentication

With the application running locally, Swagger UI is available at
`http://localhost:8080/swagger-ui.html` and the OpenAPI document at
`http://localhost:8080/v3/api-docs`.

Create a user with `POST /api/users`, then obtain a JWT with:

```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "your-password"
}
```

Use the returned token in Swagger's **Authorize** button as
`Bearer <token>`. The JWT signing key can be configured through `JWT_SECRET`;
production also disables Swagger with the `prod` profile.

## Docker

The API can use the existing `gym-mysql` container through its
`mysql_default` Docker network. Copy `.env.example` to `.env` and set the
same MySQL password used by that container, then run:

```bash
docker compose up --build -d
```

Swagger will be available at `http://localhost:8080/swagger-ui.html`.
Stop the API container with `docker compose down`; the existing MySQL
container is not removed.
