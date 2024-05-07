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
## API

