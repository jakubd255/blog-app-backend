# BlogAppBackend
Backend for personal blog app, written in Spring Boot. Currently in development.


## Tech Stack
- Spring Boot
- Spring Security
- JWT
- BCrypt
- Hibernate


## Endpoints
### POST: /api/auth/log-in
```json
{
  "email": "example@gmail.com",
  "password": "12345678"
}
```

### GET: /api/auth/log-out
- Log out by removing HTTP-only cookies
- Requires authentication

### GET: /api/auth
- Get logged-in user data
- Requires authentication

### POST: /api/posts
```json
{
  "title": "Example title",
  "body": "Example long text"
}
```
- Requires authentication

### GET: /api/posts
- Get all posts

### GET: /api/posts/:id
- Get chosen post by id

### PUT: /api/posts/:id
```json
{
  "title": "New title",
  "body": "New text"
}
```
- Requires authentication
- Only author or admin can update

### DELETE: /api/posts/:id
- Delete chosen post by id
- Requires authentication
- Only author or admin can delete