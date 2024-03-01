package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.FriendRequestDTO;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.UserInfoDTO;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.Group;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Information Controller", description = "Quản lý thông tin người dùng")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserInfoService userInfoService;

    @Operation(summary = "Tìm kiếm người dùng bằng số điện thoại", description = "Tìm kiếm người dùng bằng số điện thoại")
    @GetMapping("/profile/{phone}")
    public UserInfo getUserInfoByPhone(@PathVariable String phone) {
        return userInfoService.findUserInfo(phone);
    }

    @Operation(summary = "Lấy thông tin tất cả nhóm của người dùng", description = "Lấy danh sách nhóm mà người dùng đang là tham gia", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/profile/groups")
    public List<GroupDTO> getAllGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(summary = "Chấp nhận lời mời kết bạn", description = "Chấp nhận lời mời kết bạn từ người khác", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/profile/friends/accept")
    public List<Friend> acceptFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.acceptFriendRequest(friendRequestDTO, userPrincipal);
    }

    @Operation(summary = "Không chấp nhận lời mời kết bạn", description = "Không chấp nhận lời mời kết bạn từ người khác", security = {@SecurityRequirement(name = "bearerAuth")}, hidden = true)
    @PostMapping("/profile/friends/decline")
    public List<Friend> declineFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return null;
    }

    @Operation(summary = "Chặn bạn bè", description = "Chặn một người dùng khác thông qua ID. Người bị chặn sẽ không thể tìm thấy, gửi tin nhắn, xem profile", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/profile/friends/block")
    public List<Friend> blockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.blockFriend(friendRequestDTO, userPrincipal);
    }

    @Operation(summary = "Bỏ chặn bạn bè", description = "Bỏ chặn một người dùng khác thông qua ID", security = {@SecurityRequirement(name = "bearerAuth")}, hidden = true)
    @PostMapping("/profile/friends/unblock")
    public List<Friend> unblockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return null;
    }

    @Operation(summary = "Gửi lời mời kết bạn theo số điện thoại", description = "Gửi lời mời kết bạn đến người dùng khác", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/profile/friends/by_phone")
    public List<Friend> addFriendByPhone(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PhoneNumberDTO phoneNumberDTO) {
        return userInfoService.addFriendToUserInfoByPhone(phoneNumberDTO, userPrincipal);
    }

    @Operation(summary = "Cập nhật tên gợi nhớ", description = "Cập nhật tên gợi nhớ", security = {@SecurityRequirement(name = "bearerAuth")}, hidden = true)
    @PutMapping("/profile/friends/{friend_id}")
    public List<Friend> updateFriendById(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend_id") String friendId) {
        return null;
    }

    @Operation(summary = "Gửi lời mời kết bạn theo ID", description = "Gửi lời mời kết bạn đến người dùng khác", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/profile/friends")
    public List<Friend> addFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.addFriendToUserInfo(friendRequestDTO, userPrincipal);
    }

    @Operation(summary = "Lấy danh sách của người dùng", description = "Lấy danh sách nhóm mà người dùng đang là tham gia", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/profile/friends")
    public List<GroupDTO> getAllFriends(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(summary = "Xóa kết bạn", description = "Lấy danh sách nhóm mà người dùng đang là tham gia", security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/profile/friends/{friend_id}")
    public List<GroupDTO> deleteFriends(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable(name = "friend_id") String friend_id) {
        return userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(summary = "Tạo thông tin người dùng", description = "Tạo thông tin người dùng", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/profile")
    public UserInfo createUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserInfoDTO userInfoDTO) {
        return userInfoService.createUserInfo(userDetails.getUsername(), userInfoDTO);
    }

    @Operation(summary = "Lấy thông tin người dùng", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/profile")
    public UserInfo getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return userInfoService.findUserInfo(userDetails.getUsername());
    }

    @Operation(summary = "Cập nhật thông tin người dùng", description = "Cập nhật thông tin người dùng", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/profile")
    public UserInfo updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserInfoDTO userInfoDTO) {
        return userInfoService.updateUserInfo(userDetails.getUsername(), userInfoDTO);
    }
}
