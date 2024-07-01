
# BACKEND SPRING BOOT FOR VIET CHAT

Viet Chat is a real-time chat application inspired by Zalo.


## Demo

link


## Tech Stack

**Language:** Java

**Framework:** Spring boot

**Database:** MongoDB

**Dependencies:** Twilio, Modal Mapper, Spring Security, Spring Validation, Spring Websocket, JJWT, AWS SDK

## Features

- Register/Login
- View profiles
- User search
- Send messages
- Recall messages
- Delete messages
- Delete conversations
- Send reactions
- View messages
- Create and manage groups (add members, remove members, ...)


## API Reference

View details API: `/api/swagger-ui/index.html`

![App Screenshot](/demo/swagger.png)

### Chat Controller

#### Get chat room information

```http
  GET /api/v1/chats/{chat-id}
```

#### Mark a chat as read

```http
  PUT /api/v1/chats/{chat-id}
```

#### Get messages

```http
  GET /api/v1/chats/{chat-id}/messages
```

#### Send messages

```http
  POST /api/v1/chats/{chat-id}/messages
```

#### Delete all messages in a chat

```http
  DELETE /api/v1/chats/{chat-id}/messages
```

#### Undo sending a message

```http
  PUT /api/v1/chats/{chat-id}/messages/{message-id}
```

#### Delete a message

```http
  DELETE /api/v1/chats/{chat-id}/messages/{message-id}
```

#### Pin a message

```http
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/pin
```

#### Unpin a message

```http
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/unpin
```

#### Send reaction to a message

```http
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/reaction
```

#### Delete all reactions

```http
  DELETE /api/v1/chats/{chat-id}/messages/{message-id}/reaction
```

### User Controller

#### Get user information

```http
  GET /api/v1/users/profile
```

#### Update user information

```http
  PUT /api/v1/users/profile
```

Body
```json5
{
  "firstName": "string",
  "lastName": "string",
  "bio": "string",
  "thumbnailAvatar": "string",
  "coverImage": "string",
  "gender": true,
  "birthday": "2024-07-01"
}
```

#### Get user information by phone

```http
  GET /api/v1/users/profile/{phone}
```

#### Get all chat rooms

```http
  GET /api/v1/users/profile/chats
```

#### Update chat room

```http
  PUT /api/v1/users/profile/chats/{chat-id}
```

Body
```json5
{
  "status": "DELETED" // DELETED, HIDDEN, PINNED, NORMAL
}
```

#### Get all friends

```http
  GET /api/v1/users/profile/friends?type=friend
```

#### Send friend request

```http
  PUT /api/v1/users/friends/{friend-id}
```

#### Delete friend

```http
  DELETE /api/v1/users/friends/{friend-id}
```

#### Accept friend request

```http
  PUT /api/v1/users/friends/{friend-id}/accept
```

#### Block friend

```http
  PUT /api/v1/users/friends/{friend-id}/block
```

#### Cancel friend request

```http
  PUT /api/v1/users/friends/{friend-id}/cancel
```

#### Decline friend request

```http
  PUT /api/v1/users/friends/{friend-id}/decline
```

#### Get all groups

```http
  GET /api/v1/users/profile/groups
```

#### Leave group

```http
  PUT /api/v1/users/profile/groups/{group-id}/leave
```

#### Get all recent searches

```http
  GET /api/v1/users/search/recent
```


### Phone Verification

#### Send OTP

```http
  POST /api/v1/verification/otp/sms/send 
```

Body
```json5
{
    "phone": "phone",
}
```

#### Validate OTP

```http
  POST /api/v1/verification/otp/sms/validate
```

Body
```json5
{
  "phone": "0703290094",
  "otp": "611487"
}
```

### Authentication

#### Login

```http
  POST /api/v1/auth/login   
```

Body
```json5
{
    "phone": "phone",
    "password": "pwd"
}
```

#### Logout

```http
  POST /api/v1/auth/logout   
```

Body
```json5
{
    "token": "token"
}
```

#### Logout all

```http
  POST /api/v1/auth/logout/all   
```

Body
```json5
{
  "token": "token"
}
```

#### Update password

```http
  POST /api/v1/auth/password/change   
```

Body
```json5
{
  "oldPassword": "pwd",
  "newPassword": "pwd",
}
```

#### Forgot password

```http
  POST /api/v1/auth/password/forgot   
```

Body
```json5
{
  "phone": "phone",
}
```

#### Validate OTP for password reset

```http
  POST /api/v1/auth/password/reset/validate  
```

Body
```json5
{
  "phone": "XXXX",
  "otp": "XXXXXX"
}
```

#### Update password after validating OTP

```http
  POST /api/v1/auth/password/reset 
```

Body
```json5
{
  "token": "token",
  "password": "pwd"
}
```

#### Get new access token

```http
  POST /api/v1/auth/refresh-token   
```

Body
```json5
{
  "token": "token"
}
```

#### Register

```http
  POST /api/v1/auth/register   
```

Body
```json5
{
  "firstName": "fn",
  "lastName": "ln",
  "gender": true,
  "birthday": "2024-07-01",
  "password": "pwd"
}
```

### Group Controller

#### Create a group

```http
  POST /api/v1/groups
```

#### Get the group information

```http
  GET /api/v1/groups/{group-id}
```

#### Update the group information

```http
  PUT /api/v1/groups/{group-id}
```

Body
```json
{
    "name": "new name",
    "thumbnailAvatar": "url"
}
```

#### Disband the group

```http
  DELETE /api/v1/groups/{group-id}
```

#### Get all the group members

```http
  GET /api/v1/groups/{group-id}/members
```

#### Add the group members

```http
  PUT /api/v1/groups/{group-id}/members
```

Body
```json
["member id 1", "member id 2"]
```

#### Delete a group member

```http
  DELETE /api/v1/groups/{group-id}/members/{member-id}
```

#### Change a group member role

```http
  PUT /api/v1/groups/{group-id}/members/{member-id}
```

Body
```json5
{
  "role": "GROUP_LEADER" // Roles: GROUP_LEADER, DEPUTY_GROUP_LEADER, MEMBER
}
```

### File Controller

#### Get Pre-signed URL to upload file to S3

```http
  POST /api/v1/files
```


## Environment Variables

To run this project, you will need to add the following environment variables to your **application-secret.yml** file

```yaml
aws:
  access-key: key
  secret-key: key
  endpoint-url: https://{bucket-name}.s3.ap-southeast-1.amazonaws.com/
  bucket:
    name: {bucket-name}

twilio:
  accountSID: key
  authToken: key
  phoneNumberTrial: +XXXXXX
```


## Run Locally

Clone the project

```bash
  git clone https://github.com/Minhquanzz1002/viet-chat-backend-v2
```

Go to the project directory

```bash
  cd my-project
```

Install dependencies

```bash
  ./mvnw install
```

Start the server

```bash
  ./mvnw spring-boot:run
```


## Authors

- [@Minhquanzz1002](https://github.com/Minhquanzz1002)

