package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.MessageRequestDTO;
import vn.edu.iuh.dto.ReactionMessageDTO;
import vn.edu.iuh.models.Chat;
import vn.edu.iuh.models.Message;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.ChatService;

@RestController
@RequestMapping("/v1/chats")
@Tag(name = "Chats Controller", description = "Quản lý chat")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @Operation(
            summary = "Lấy tin nhắn của phòng chat",
            description = """
                    Lấy tin nhắn của phòng chat theo ID (có phân trang và kết hợp tìm kiếm tương đối). Trả về danh sách tin nhắn và thông tin người gửi phục vụ cho render tin nhắn.
                                        
                    Đối với các tin nhắn bị thu hồi thì phần nội dung sẽ chuyển sang `Tin nhắn đã bị thu hồi` và phần tệp đánh kèm cũng sẽ chuyển sang `null`
                                        
                    Đối với tin nhắn bị xóa sẽ không được trả về.
                                        
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                        
                    <strong>Forbidden: </strong>
                    - Bạn không phải là thành viên của phòng chat này
                                        
                    <strong>Not Found: </strong>
                    - Không tìm thấy ID phòng chat
                    """
    )
    @GetMapping("/{chat-id}/messages")
    public Page<Message> getAllChat(@PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "20") int size , @RequestParam(required = false) String content) {
        Pageable pageable = PageRequest.of(page, size);
        return chatService.getAllMessages(chatId, userPrincipal, pageable, content);
    }

    @Operation(
            summary = "Thả cảm xúc tin nhắn",
            description = """
                     
                     Cảm xúc gồm: LIKE, LOVE, CRY, ANGER, WOW. Có thể cho deploy sau 1s mới bắt đầu request để giảm tải cho hệ thống
                                       
                     Response của API này sẽ được gửi đến người dùng thuộc phòng chat đó. Hãy bắt nó bằng cách đăng ký lắng nghe socket `/chatroom/{chatId}`
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     """
    )
    @PutMapping("/{chat-id}/messages/{message-id}/reaction")
    public Message reactionMessage(@PathVariable("message-id") String messageId, @PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ReactionMessageDTO reactionMessageDTO) {
        return chatService.reactionMessage(messageId, chatId, userPrincipal, reactionMessageDTO);
    }

    @Operation(
            summary = "Ghim tin nhắn",
            description = """
                    Ghim tin nhắn và trả về một Message dạng event qua cả response và socket cho người còn trong phòng chat.
                    
                    Nội dung message event dạng: `{id người ghim} đã ghim một tin nhắn`. Hãy chuyển ID thành tên người dùng. Trường hợp không tìm thấy thì ẩn event
                        
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     - Không tìm thấy tin nhắn
                     
                     <strong>Bad Request: </strong>
                     - Tin nhắn đã được ghim trước đó
                     - Ghim tối đa 3 tin nhắn
                     """
    )
    @PutMapping("/{chat-id}/messages/{message-id}/pin")
    public Message pinMessage(@PathVariable("message-id") String messageId, @PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.pinMessage(userPrincipal, chatId, messageId);
    }

    @Operation(
            summary = "Bỏ ghim tin nhắn",
            description = """
                    Bỏ ghim tin nhắn và trả về một Message dạng event qua cả response và socket cho người còn trong phòng chat
                    
                    Nội dung message event dạng: `{id người bỏ ghim} đã bỏ ghim một tin nhắn`. Hãy chuyển ID thành tên người dùng. Trường hợp không tìm thấy thì ẩn event
                    
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     - Không tìm thấy tin nhắn
                     
                     <strong>Bad Request: </strong>
                     - Tin nhắn không được ghim trước đó
                     """
    )
    @PutMapping("/{chat-id}/messages/{message-id}/unpin")
    public Message unpinMessage(@PathVariable("message-id") String messageId, @PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.unpinMessage(userPrincipal, chatId, messageId);
    }

    @Operation(
            summary = "Xóa toàn bộ cảm xúc tin nhắn",
            description = """
                     
                     Xóa toàn bộ cảm xúc bạn đã thả cho tin nhắn
                                       
                     Response của API này sẽ được gửi đến người dùng thuộc phòng chat đó. Hãy bắt nó bằng cách đăng ký lắng nghe socket `/chatroom/{chatId}`
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     """
    )
    @DeleteMapping("/{chat-id}/messages/{message-id}/reaction")
    public Message deleteAllReactionsForMessage(@PathVariable("message-id") String messageId, @PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.deleteReactionsMessage(messageId, chatId, userPrincipal);
    }

    @Operation(
            summary = "Gửi tin nhắn",
            description = """
                     Gửi tin nhắn. Tin nhắn sẽ được thêm vào `messages` và phần `lastMessage` cũng sẽ được cập nhật
                     
                     Nếu là tin nhắn văn bản thì nội dung `lastMessage.content: "nội dung tin nhắn"` ngược lại nếu là ảnh hoặc video thì `lastMessage.content: "[FILE]"`
                                       
                     Response của API này sẽ được gửi đến người dùng thuộc phòng chat đó. Hãy bắt nó bằng cách đăng ký lắng nghe socket `/chatroom/{chatId}`
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     """
    )
    @PostMapping("/{chat-id}/messages")
    public Message sendMessage(@RequestBody MessageRequestDTO messageRequestDTO, @PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.saveMessage(messageRequestDTO, chatId, userPrincipal);
    }

    @Operation(
            summary = "Xem tin nhắn",
            description = """
                     Cập nhật trạng thái đã xem cho tin nhắn.
                     
                     Cập nhật `lastSeenMessageId: "id"` thành message id cuối cùng
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     """
    )
    @PutMapping("/{chat-id}")
    public String seenMessage(@PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.seenMessage(chatId, userPrincipal);
    }

    @Operation(
            summary = "Xóa tin nhắn",
            description = """
                    Xóa tin nhắn. Đây là hành động xóa tin nhắn ở từng ứng dụng của người dùng. Đối phương vẫn sẽ nhìn thấy tin nhắn đó bình thường.
                                       
                    User Info ID sẽ thêm vào `deleteBy: ['id']`
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     - Không tìm thấy tin nhắn
                     """
    )
    @DeleteMapping("/{chat-id}/messages/{message-id}")
    public String deleteMessage(@PathVariable("chat-id") String chatId, @PathVariable("message-id") String messageId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.deleteMessage(messageId, chatId, userPrincipal);
    }

    @Operation(
            summary = "Thu hồi tin nhắn",
            description = """
                    Thu hồi tin nhắn đã gửi. Nhớ kiểm tra `createdAt` so với hiện tại có quá 24 giờ không trước khi thực hiện thu hồi.
                    
                    Chỉ có `status` của message sẽ được chuyển sang `UNSEND` các nội dung khác sẽ được giữ nguyên
                    
                    Response của API này sẽ được gửi đến người dùng thuộc phòng chat đó. Hãy bắt nó bằng cách đăng ký lắng nghe socket `/chatroom/{chatId}`
                    
                    <strong>Bad Request:</strong>
                    - Bạn chỉ có thể thu hồi tin nhắn trong 1 ngày sau khi gửi
                                        
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                        
                    <strong>Forbidden: </strong>
                    - Bạn không phải là thành viên của phòng chat này
                    - Chỉ người gửi mới được thu hồi
                                        
                    <strong>Not Found: </strong>
                    - Không tìm thấy ID phòng chat
                    - Không tìm thấy tin nhắn
                    """
    )
    @PutMapping("/{chat-id}/messages/{message-id}")
    public Message unsendMessage(@PathVariable("chat-id") String chatId, @PathVariable("message-id") String messageId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.unsendMessage(messageId, chatId, userPrincipal);
    }

    @Operation(
            summary = "Xóa lịch sử trò chuyện (xóa toàn bộ tin nhắn)",
            description = """
                    Xóa lịch sử trò chuyện
                                       
                    `lastDeleteChatTime: LocalDateTime.now()`
                                       
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                                       
                     <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của phòng chat này
                     
                     <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                     """
    )
    @DeleteMapping("/{chat-id}/messages")
    public String deleteAllMessages(@PathVariable("chat-id") String chatId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return chatService.deleteAllMessages(userPrincipal, chatId);
    }

    @Operation(
            summary = "Lấy thông tin nhóm chat theo ID",
            description = """
                    Lấy thông tin nhóm chat theo ID<br>
                    
                    <strong>⚠️ Vui lòng không xử lý các lỗi dưới đây phía client. Các lỗi này chỉ đóng vai trò bảo vệ API khỏi các lỗi cố tình.⚠️</strong>
                    <strong>Not Found: </strong>
                    - Không tìm thấy ID phòng chat
                    """
    )
    @GetMapping("/{chat-id}")
    public Chat getChat(@PathVariable("chat-id") String id) {
        return chatService.findById(id);
    }
}
