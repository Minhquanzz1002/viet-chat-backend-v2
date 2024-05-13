# Backend Spring Boot cho Việt Chat
## Cài đặt
Tạo S3 Bucket và tạo file `application-secret.yml` với nội dung
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
## Cập nhật
Tính năng ẩn/hiện, ghim/bỏ ghim phòng chat
- Phòng chat có trạng thái `hidden: true` sẽ không được hiển thị trong danh sách phòng chat. Chỉ có thể tìm kiếm và nhập mật khẩu 4 số để truy cập (theo Zalo). Phần mật khẩu chưa xử lý
- Bổ sung `boolean hidden` vào thông tin phòng chat
- Bổ sung `LocalDateTime pinnedAt` vào thông tin phòng chat

Cập nhật API `GET: /v1/users/profile/chats`
- Bổ sung `hidden: boolean` trong kết quả trả về
- Bổ sung `pinnedAt: LocalDateTime` trong kết quả trả về
- Danh sách chat được sắp xếp theo thứ tự:
  - Danh sách phòng chat được ghim (cái được ghim mới nhất sẽ ở trên đầu mảng)
  - Các phòng chat còn lại (sắp xếp theo thời gian gửi tin nhắn cuối)

API mới: `PUT: /v1/users/profile/chats/{chat-id}`. Cập nhật ẩn/hiện, ghim/bỏ ghim phòng chat
- Body: những thông tin không thay đổi không cần bỏ vào
```
{
  hidden: bool;
  pin: bool
}
```
- Error: 
  - 404 - Not Found: `{chat-id}` không tồn tại
```json
{
    "timestamp": "13-05-2024 17:43:19",
    "status": 404,
    "error": "Not Found",
    "detail": "Không tìm thấy phòng chat"
}
```

API mới: `DELETE: /v1/chats/{chat-id}/messages`
- Response:
```text
Xóa lịch sử trò chuyện thành công
```
- Error:
  - 404 - Not Found: `{chat-id}` không tồn tại
```json
{
    "timestamp": "13-05-2024 17:43:19",
    "status": 404,
    "error": "Not Found",
    "detail": "Không tìm thấy phòng chat"
}
```
## API

