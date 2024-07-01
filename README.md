
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

```text 
  GET /api/v1/chats/{chat-id}
```

#### Mark a chat as read

```text
  PUT /api/v1/chats/{chat-id}
```

#### Get messages

```text
  GET /api/v1/chats/{chat-id}/messages
```

#### Send messages

```text
  POST /api/v1/chats/{chat-id}/messages
```

#### Delete all messages in a chat

```text
  DELETE /api/v1/chats/{chat-id}/messages
```

#### Undo sending a message

```text
  PUT /api/v1/chats/{chat-id}/messages/{message-id}
```

#### Delete a message

```text
  DELETE /api/v1/chats/{chat-id}/messages/{message-id}
```

#### Pin a message

```text
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/pin
```

#### Unpin a message

```text
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/unpin
```

#### Send reaction to a message

```text
  PUT /api/v1/chats/{chat-id}/messages/{message-id}/reaction
```

#### Delete all reactions

```text
  DELETE /api/v1/chats/{chat-id}/messages/{message-id}/reaction
```

### User Controller

#### Get user information

```text
  GET /api/v1/users/profile
```

#### Update user information

```text
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

```text
  GET /api/v1/users/profile/{phone}
```

#### Get all chat rooms

```text
  GET /api/v1/users/profile/chats
```

#### Update chat room

```text
  PUT /api/v1/users/profile/chats/{chat-id}
```

Body
```json5
{
  "status": "DELETED" // DELETED, HIDDEN, PINNED, NORMAL
}
```

#### Get all friends

```text
  GET /api/v1/users/profile/friends?type=friend
```

#### Send friend request

```text
  PUT /api/v1/users/friends/{friend-id}
```

#### Delete friend

```text
  DELETE /api/v1/users/friends/{friend-id}
```

#### Accept friend request

```text
  PUT /api/v1/users/friends/{friend-id}/accept
```

#### Block friend

```text
  PUT /api/v1/users/friends/{friend-id}/block
```

#### Cancel friend request

```text
  PUT /api/v1/users/friends/{friend-id}/cancel
```

#### Decline friend request

```text
  PUT /api/v1/users/friends/{friend-id}/decline
```

#### Get all groups

```text
  GET /api/v1/users/profile/groups
```

#### Leave group

```text
  PUT /api/v1/users/profile/groups/{group-id}/leave
```

#### Get all recent searches

```text
  GET /api/v1/users/search/recent
```


### Phone Verification

#### Send OTP

```text
  POST /api/v1/verification/otp/sms/send 
```

Body
```json5
{
    "phone": "phone",
}
```

#### Validate OTP

```text
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

```text
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

```text
  POST /api/v1/auth/logout   
```

Body
```json5
{
    "token": "token"
}
```

#### Logout all

```text
  POST /api/v1/auth/logout/all   
```

Body
```json5
{
  "token": "token"
}
```

#### Update password

```text
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

```text
  POST /api/v1/auth/password/forgot   
```

Body
```json5
{
  "phone": "phone",
}
```

#### Validate OTP for password reset

```text
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

```text
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

```text
  POST /api/v1/auth/refresh-token   
```

Body
```json5
{
  "token": "token"
}
```

#### Register

```text
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

```text
  POST /api/v1/groups
```

#### Get the group information

```text
  GET /api/v1/groups/{group-id}
```

#### Update the group information

```text
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

```text
  DELETE /api/v1/groups/{group-id}
```

#### Get all the group members

```text
  GET /api/v1/groups/{group-id}/members
```

#### Add the group members

```text
  PUT /api/v1/groups/{group-id}/members
```

Body
```json
["member id 1", "member id 2"]
```

#### Delete a group member

```text
  DELETE /api/v1/groups/{group-id}/members/{member-id}
```

#### Change a group member role

```text
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

```text
  POST /api/v1/files
```


## Environment Variables

To run this project, you will need to add the following environment variables to your **application-secret.yml** file

```yaml
aws:
  access-key: key
  secret-key: key
  endpoint-url: texts://{bucket-name}.s3.ap-southeast-1.amazonaws.com/
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
  git clone texts://github.com/Minhquanzz1002/viet-chat-backend-v2
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

- [@Minhquanzz1002](texts://github.com/Minhquanzz1002)

