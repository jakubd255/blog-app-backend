# BlogAppBackend
Backend for blog app, written in Spring Boot.
Application provides functionalities for user authentication, profile management, and content creation. 
Posts can be added by redactors or admin. Users can like and comment on posts and reply to comments.

The API supports pagination to efficiently manage large volumes of data. By default, pagination is enabled with the following parameters:
- page: The page number (0-based index). Defaults to 0 if not specified.
- size: The number of items per page. Defaults to 20 if not specified.


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
- Adding comments
- Liking posts and comments

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

### GET: /api/posts/:id/likes
- Get users who liked the post

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

### PUT: /api/posts/:id/like
- Like or dislike the post
- Requires authentication

### DELETE: /api/posts/:id
- Delete chosen post by id
- Requires authentication
- Only author or admin can delete



### POST: /api/comments/post/:id
```json
{
  "text": "Comment text"
}
```
- Add comment to the post
- Requires authentication

### POST: /api/comments/parent/:id
```json
{
  "text": "Comment text"
}
```
- Add reply to the comment
- Requires authentication

### GET: /api/comments/post/:id
- Get comments of the post

### GET: /api/comments/parent/:id
- Get replies of the comment

### GET: /api/comments/:id/likes
- Get users who liked the comment

### PUT: /api/comments/:id
```json
{
  "text": "New text"
}
```
- Requires authentication
- Only post or comment author and admin can update

### PUT: /api/comments/:id/like
- Like or dislike the comment
- Requires authentication

### DELETE: /api/comments/:id
- Delete comment by id
- Requires authentication
- Only post or comment author and admin can delete



### GET: /api/files/download/:name
- Download chosen file by its name