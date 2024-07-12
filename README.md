# BlogAppBackend
Backend for blog app, written in Spring Boot. 
This application provides functionalities for user authentication, profile management, and content creation.


## Tech Stack
- Spring Boot
- Spring Security
- JWT
- BCrypt
- Hibernate


## User roles
#### User
- Standard role
- Profile management, password and email updates
- Access to published posts
#### Redactor
- Has permissions to manage the blog content
- Adding new posts
- Editing and deleting own posts
- Access to own drafts
- All permissions of a User

#### Admin
- Has full access to all functions
- Edit and delete all users and posts
- Access to all drafts
- All permissions of a Redactor


## Entities
### User
- id (Integer): Unique identifier for the user
- name (String): User's name
- email (String): User's email address
- password (String): User's hashed password
- profileImage (String): profile image's name
- bio (String): Short biography or description of the user
- role (String): User's role (ROLE_USER, ROLE_REDACTOR, ROLE_ADMIN)

### Post
- id (Integer): Unique identifier for the post
- title (String): Title of the post
- body (String): Content of the post
- status (String): Status of the post (DRAFT, PUBLISHED)
- user (User): Author of the post - reference to the User entity
- date (Date): Date when the post was created or updated to PUBLISHED from DRAFT



## Endpoints
### POST: /api/auth/register
```json
{
  "name": "Example Name",
  "email": "example@gmail.com",
  "password": "12345678"
}
```

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

### PUT: /api/auth/email
```json
{
  "email": "new.email@gmail.com"
}
```
- Requires authentication

### PUT: /api/auth/password
```json
{
  "currentPassword": "12345678",
  "newPassword": "new-password"
}
```
- Requires authentication



### GET: /api/users/:id
- Get chosen user by id

### GET: /api/users
- Get all users

### PUT: /api/users/:id
```json
{
  "name": "New Username",
  "bio": "New bio"
}
```
- Update user information.
- Requires authentication
- User can edit only own
- Admin can edit any

### PUT: /api/users/:id/role
- Change user's role
- Requires authentication
- Only admin can edit

### PUT: /api/users/:id/profile-image
- `multipart/form-data`, `image` key
- Edit chosen user's profile image
- Requires authentication
- User can edit only own
- Admin can edit any

### DELETE: /api/users/:id
- Requires authentication
- User can delete only his own account
- Admin can delete any account

### DELETE: /api/users/:id/profile-image
- Delete user's profile image
- User can delete only his own
- Admin can delete any



### POST: /api/posts
```json
{
  "title": "Example title",
  "body": "Example long text",
  "status": "DRAFT or PUBLISHED"
}
```
- Requires authentication
- Admin or redactor can add

### GET: /api/posts
- Get all published posts

### GET: /api/posts/all
- Get all posts
- Requires authentication
- Requires admin role

### GET: /api/posts/:id
- Get chosen post by id
- If it's draft, only admin or author can get

### GET: /api/posts/user/:id
- Get chosen user's published posts

### GET: /api/posts/user/:id/all
- Get chosen user's all posts
- Requires authentication
- Only admin or author can get

### PUT: /api/posts/:id
```json
{
  "title": "New title",
  "body": "New text",
  "status": "DRAFT or PUBLISHED"
}
```
- Requires authentication
- Only author or admin can update

### DELETE: /api/posts/:id
- Delete chosen post by id
- Requires authentication
- Only author or admin can delete



### GET: /api/files/download/:name
- Download chosen file by its name


## Integration tests
![](./screenshots/integration-tests.png)  