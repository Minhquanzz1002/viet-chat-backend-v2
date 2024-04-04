package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserChat;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Information Controller", description = "Quản lý thông tin người dùng")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserInfoService userInfoService;

    @Operation(
            summary = "Tìm kiếm người dùng bằng số điện thoại",
            description = "Tìm kiếm người dùng bằng số điện thoại. Dùng cho phần tìm kiếm để kết bạn"
    )
    @GetMapping("/profile/{phone:^\\d+$}")
    public UserInfo getUserInfoByPhone(@PathVariable String phone) {
        return userInfoService.findUserInfo(phone);
    }

    @Operation(
            summary = "Lấy danh sách tất cả nhóm của người dùng",
            description = "Lấy danh sách nhóm mà người dùng đang là tham gia. Chỉ trả về các thông tin cơ bản phục vụ cho render danh sách nhóm",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile/groups")
    public List<GroupDTO> getAllGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(
            summary = "Chấp nhận lời mời kết bạn",
            description = """
                    Chấp nhận lời mời kết bạn từ người khác<br>
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}/accept")
    public String acceptFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal,  @PathVariable("friend-id") String friendId) {
        return userInfoService.acceptFriendRequest(friendId, userPrincipal);
    }

    @Operation(
            summary = "Từ chối lời mời kết bạn",
            description = """
                    Không chấp nhận lời mời kết bạn từ người khác<br>
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}/decline")
    public String declineFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal,  @PathVariable("friend-id") String friendId) {
        return userInfoService.declineFriendRequest(friendId, userPrincipal);
    }

    @Operation(
            summary = "Chặn bạn bè",
            description = """
                    Chặn một người dùng khác thông qua ID. Người bị chặn sẽ không thể tìm thấy, gửi tin nhắn, xem profile<br>
                    <strong>Lỗi nếu: </strong>
                    * Bạn đã chặn đối phương trước đó (status == BLOCK)
                    * Bạn đã bị đối phương chặn (status == BLOCKED)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}/block")
    public String blockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.blockFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Bỏ chặn bạn bè",
            description = """
                    Bỏ chặn một người dùng khác thông qua ID<br>
                    <strong>Chú ý: </strong> nếu tài khoản không bị khóa trước đó sẽ trả về lỗi
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}/unblock")
    public String unblockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.unblockFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Gửi lời mời kết bạn theo số điện thoại",
            description = """
                    Gửi lời mời kết bạn bằng số điện thoại của đối phương<br>
                    <strong>Lỗi nếu:</strong><br>
                    * Bạn đã chặn đối phương (status == BLOCK)
                    * Bạn đã bị đối phương chặn(status == BLOCKED)
                    * Bạn đã gửi lời mời kết bạn cho đối phương trước đó (status == FRIEND_REQUEST)
                    * Bạn có lời mời kết bạn từ đối phương (status == PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/by-phone")
    public String addFriendByPhone(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid PhoneNumberDTO phoneNumberDTO) {
        return userInfoService.addFriendByPhone(phoneNumberDTO, userPrincipal);
    }

    @Operation(
            summary = "Gửi lời mời kết bạn theo User Info ID",
            description = """
                    Gửi lời mời kết bạn bằng ID của đối phương<br>
                    <strong>Lỗi nếu:</strong><br>
                    * Bạn đã chặn đối phương (status == BLOCK)
                    * Bạn đã bị chặn đối phương (status == BLOCKED)
                    * Bạn đã gửi lời mời kết bạn cho đối phương trước đó (status == FRIEND_REQUEST)
                    * Bạn có lời mời kết bạn từ đối phương (status == PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}")
    public String addFriend(@AuthenticationPrincipal UserPrincipal userPrincipal,  @PathVariable("friend-id") String friendId) {
        return userInfoService.addFriendByUserId(friendId, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách bạn bè, chặn, lời mời kết bạn của người dùng",
            description = """
                    Lấy danh sách bạn bè, chặn, chờ kết bạn của người dùng
                    + request: danh sách lời mời kết bạn
                    + friend: danh sách bạn bè
                    + block: danh sách bị chặn
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile/friends")
    public List<Friend> getAllFriends(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam FriendTypeRequest type) {
        List<Friend> friends = userInfoService.findUserInfo(userPrincipal.getUsername()).getFriends();
        return friends.stream().filter(friend -> friend.getStatus().equals(mapToFriendStatus(type))).collect(Collectors.toList());
    }

    private FriendStatus mapToFriendStatus(FriendTypeRequest type) {
        return switch (type) {
            case friend -> FriendStatus.FRIEND;
            case request -> FriendStatus.PENDING;
            case block -> FriendStatus.BLOCKED;
        };
    }

    @Operation(
            summary = "Xóa kết bạn",
            description = """
                    Xóa kết bạn. Không xóa dữ liệu mối liên hệ giữa 2 người mà đổi trạng thái sang người lạ (status == STRANGER). Để duy trì tên gợi nhớ của người dùng (display_name)<br>
                    <strong>Lưu ý:</strong> nếu 2 người dùng chưa kết bạn thì trả về lỗi
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @DeleteMapping("/profile/friends/{friend-id}")
    public String deleteFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable(name = "friend-id") String friendId) {
        return userInfoService.deleteFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách phòng chat",
            description = """
                    Lấy danh sách phòng chat. Cả chat đơn và chat nhóm
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile/chats")
    public List<ChatRoomDTO> updateUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.getAllChats(userPrincipal);
    }

    @Operation(
            summary = "Lấy thông tin người dùng",
            description = "Lấy thông tin của người dùng dựa trên chuỗi JWT trong header",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile")
    public UserInfo getUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(
            summary = "Cập nhật thông tin người dùng",
            description = """
                    Cập nhật thông tin người dùng: họ đệm, tên, bio, ảnh avatar, ảnh nền trang cá nhân, giới tính, ngày sinh.<b> Các thông tin không thay đổi có thể không cần truyền</b><br>
                    <b>Chú ý:</b> Đối với avatar và ảnh bìa hãy gọi lên /v1/files POST để lấy url upload lên S3 và tự upload phía client. Sau đó bỏ đường link file vào đây để cập nhật avatar
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile")
    public UserInfo updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserInfoDTO userInfoDTO) {
        return userInfoService.updateUserInfo(userDetails.getUsername(), userInfoDTO);
    }
}
