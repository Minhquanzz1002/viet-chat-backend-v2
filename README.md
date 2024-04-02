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
    stompClient.subscribe('/user/' + user.username + '/private', onMessageReceived);
}

const onMessageReceived = (payload) => {
    // TODO làm quần què gì đó làm
}

// Gửi tin nhắn
const sendMessage = () => {
    const data = {};
    stompClient.send("/app/message/65e84882726cb907cc6b6040", {}, JSON.stringify(data));
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