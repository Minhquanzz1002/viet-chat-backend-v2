# viet-chat-backend

Backend Spring Boot cho Việt Chat

## Bắt buộc
Tự tạo S3 Bucket và tạo file `application-secret.yml` với nội dung
```yaml
aws:
  access-key: key
  secret-key: key
  endpoint-url: https://{bucket-name}.s3.ap-southeast-1.amazonaws.com/
  bucket:
    name: {bucket-name}

twilio:
  accountSID: AC8cb6385a9de41d503b7c432bcd095c69
  authToken: 9db70f2d921fa3110a98f25c63653b9b
  phoneNumberTrial: +16266465296
```

## Cách dùng Websocket phía Client

### 1. Import thư viện vào `package.json`

``` json
"sockjs-client": "^1.6.1",
"stompjs": "^2.3.3",
```

và thư viện `"net": "^1.0.2"` nếu gặp lỗi liên quan đến net

### 2. Kết nối

```javascript
import {over} from 'stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;

const connect = () => {
    let Sock = new SockJS('http://localhost:8080/api/ws');
    stompClient = over(Sock);
    stompClient.connect({}, onConnected, (error) => console.log(error));
}

const onConnected = () => {
    // Dùng để nhận các thông báo có người kết bạn, có người đồng ý kết bạn
    stompClient.subscribe('/user/' + user.username + '/private', onMessageReceived);
    // Dùng để nhận tin nhắn mới từ phòng chat
    stompClient.subscribe('/chatroom/{id của phòng chat}', onPrivateMessageReceived);
}

const onMessageReceived = (payload) => {
    consolog.log(payload.body)
    // TODO làm quần què gì đó làm
}

// Gửi tin nhắn
const sendMessage = () => {
    const data = {
        content: "Hello mấy ní",
        sender: "id của thằng gửi",
        replyMessageId: "id của tin nhắn muốn rep",
        attachments: [
            {
                type: "IMAGE",
                url: "/abc/abc.png",
                filename: "abc.png"
            }
            
        ],
    };
    stompClient.send("/app/chat/{id của phòng chat}", {}, JSON.stringify(data));
}
```

### 3. Một số sự kiện cần lưu ý

- Gửi lời mời kết bạn

```json
{
  "message": "Quân đã gửi lời mời kết bạn",
  "type": "FRIEND_REQUEST",
  "sender": "6602209387c1ef000f268f77",
  "timestamp": "2024-03-20 00:00:00"
}
```

- Chấp nhận lời mời kết bạn

```json
{
  "message": "Quân đã chấp nhận lời mời kết bạn",
  "type": "ACCEPT_FRIEND_REQUEST",
  "sender": "6602209387c1ef000f268f77",
  "timestamp": "2024-03-20 00:00:00"
}
```

Hệ thống gửi thông báo về qua `/user/{user-id}/private`
### 4. Cuối cùng
Cảm ơn vì đã đọc. Lũ Wibu ảo tưởng