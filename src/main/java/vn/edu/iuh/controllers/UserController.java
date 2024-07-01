package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.GroupService;
import vn.edu.iuh.services.UserInfoService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Information Controller", description = "Quản lý thông tin người dùng")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserInfoService userInfoService;
    private final GroupService groupService;
    private final ModelMapper modelMapper;

    @Operation(
            summary = "Lấy tất cả nhóm bạn bè đã tham gia",
            description = """
                    
                        
                    <strong>Bad Request:</strong>
                    - Người được tìm kiếm là người yêu cầu
                                        
                    <strong>Not Found: </strong>
                    - Không tìm thấy
                    """

    )
    @GetMapping("/profile/friends/{friend-id}/groups")
    public List<GroupDTO> getAllGroups(@PathVariable("friend-id") String friendId, @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(defaultValue = "asc") String sort) {
        if (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Giá trị của tham số 'sort' phải là 'asc' hoặc 'desc'.");
        }
        List<GroupDTO> groupDTOList =  userInfoService.findAllGroupToUserInfoByUserInfoId(friendId);
        if (sort.equals("asc")) {
            groupDTOList.sort(Comparator.comparing(GroupDTO::getName));
        }else {
            groupDTOList.sort(Comparator.comparing(GroupDTO::getName).reversed());
        }
        return groupDTOList;
    }

    @Operation(
            summary = "Tìm kiếm người dùng bằng số điện thoại",
            description = """
                    Tìm kiếm người dùng bằng số điện thoại. Dùng cho phần tìm kiếm để kết bạn
                    
                    Lưu ý phần `{phone}` chỉ chấp nhận số, nếu không sẽ trả về lỗi `Not Found`
                        
                    <strong>Bad Request:</strong>
                    - Người được tìm kiếm là người yêu cầu
                                        
                    <strong>Not Found: </strong>
                    - Không tìm thấy
                    """

    )
    @GetMapping("/profile/{phone:^\\d+$}")
    public OtherUserInfoDTO getUserInfoByPhone(@PathVariable String phone, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findUserInfoByPhone(phone, userPrincipal);
    }

    @Operation(
            summary = "Danh sách người dùng đã tìm kiếm gần đây",
            description = "Danh sách người dùng đã tìm kiếm gần đây. Nếu không truyền size mặc định sẽ là 6"
    )
    @GetMapping("/profile/search/recent")
    public List<OtherUserInfoDTO> getUserInfoRecentSearches(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(defaultValue = "6") int size) {
        return userInfoService.findRecentSearches(size, userPrincipal);
    }

    @Operation(
            summary = "Rời nhóm",
            description = """
                    Chú ý: nếu bạn là nhóm trưởng bạn sẽ không được phép rời nhóm. Vui lòng chuyển quyền nhóm trưởng trước khi rời đi
                    
                    <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của nhóm
                     - Bạn là nhóm trưởng không thể rời nhóm. Hãy chuyển giao vị trí trước
                     
                    <strong>Not Found: </strong>
                     - Không tìm thấy ID nhóm
                    """
    )
    @PutMapping("/profile/groups/{group-id}/leave")
    public String leaveGroup(@PathVariable("group-id") String groupId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return groupService.leaveGroup(groupId, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách tất cả nhóm của người dùng",
            description = """
                    Lấy danh sách nhóm mà người dùng đang là tham gia. Chỉ trả về các thông tin cơ bản phục vụ cho render danh sách nhóm
                    
                    Danh sách đang được sắp xếp theo `name`. sort chỉ chấp nhận `asc` hoặc `desc`
                    
                    <strong>Internal Server Error: </strong>
                    - Lỗi tham số sort
                    """
    )
    @GetMapping("/profile/groups")
    public List<GroupDTO> getAllGroups(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(defaultValue = "asc") String sort) {
        if (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Giá trị của tham số 'sort' phải là 'asc' hoặc 'desc'.");
        }
        List<GroupDTO> groupDTOList =  userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
        if (sort.equals("asc")) {
            groupDTOList.sort(Comparator.comparing(GroupDTO::getName));
        }else {
            groupDTOList.sort(Comparator.comparing(GroupDTO::getName).reversed());
        }
        return groupDTOList;
    }

    @Operation(
            summary = "Chấp nhận lời mời kết bạn",
            description = """
                    Chấp nhận lời mời kết bạn từ người khác<br>
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """
    )
    @PutMapping("/profile/friends/{friend-id}/accept")
    public Friend acceptFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.acceptFriendRequest(friendId, userPrincipal);
    }

    @Operation(
            summary = "Thu hồi lời mời kết bạn",
            description = """
                    Thu hồi lời mời kết bạn đã gửi
                    
                    <strong>Bad Request:</strong>
                    - Không tìm thấy lời mời đã gửi
                    - Bị chặn
                    - Bạn đã chặn đối phương
                    """
    )
    @PutMapping("/profile/friends/{friend-id}/cancel")
    public Friend cancelFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.cancelFriendRequest(friendId, userPrincipal);
    }

    @Operation(
            summary = "Từ chối lời mời kết bạn",
            description = """
                    Không chấp nhận lời mời kết bạn từ người khác<br>
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """
    )
    @PutMapping("/profile/friends/{friend-id}/decline")
    public Friend declineFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.declineFriendRequest(friendId, userPrincipal);
    }

    @Operation(
            summary = "Chặn bạn bè",
            description = """
                    Chặn một người dùng khác thông qua ID. Người bị chặn sẽ không thể tìm thấy, gửi tin nhắn, xem profile<br>
                    <strong>Lỗi nếu: </strong>
                    * Bạn đã chặn đối phương trước đó (status == BLOCK)
                    * Bạn đã bị đối phương chặn (status == BLOCKED)
                    """
    )
    @PutMapping("/profile/friends/{friend-id}/block")
    public Friend blockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.blockFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Bỏ chặn bạn bè",
            description = """
                    Bỏ chặn một người dùng khác thông qua ID<br>
                    <strong>Chú ý: </strong> nếu tài khoản không bị khóa trước đó sẽ trả về lỗi
                    """
    )
    @PutMapping("/profile/friends/{friend-id}/unblock")
    public Friend unblockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.unblockFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Gửi lời mời kết bạn",
            description = """
                    Gửi lời mời kết bạn<br>
                    <strong>Lỗi nếu:</strong><br>
                    * Bạn đã chặn đối phương (status == BLOCK)
                    * Bạn đã bị chặn đối phương (status == BLOCKED)
                    * Bạn đã gửi lời mời kết bạn cho đối phương trước đó (status == FRIEND_REQUEST)
                    * Bạn có lời mời kết bạn từ đối phương (status == PENDING)
                    """
    )
    @PutMapping("/profile/friends/{friend-id}")
    public Friend addFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return userInfoService.addFriendByUserId(friendId, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách bạn bè, chặn, lời mời kết bạn của người dùng",
            description = """
                    Lấy danh sách bạn bè, chặn, chờ kết bạn của người dùng
                    + request: danh sách lời mời kết bạn
                    + sent: danh sách lời mời kết bạn đã gửi
                    + friend: danh sách bạn bè
                    + block: danh sách bị chặn
                    """
    )
    @GetMapping("/profile/friends")
    public List<FriendDTO> getAllFriends(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam FriendTypeRequest type) {
        List<Friend> friends = userInfoService.findUserInfo(userPrincipal.getUsername()).getFriends();
        return friends.stream()
                .filter(friend -> friend.getStatus().equals(mapToFriendStatus(type)))
                .map(friend -> modelMapper.map(friend, FriendDTO.class))
                .toList();
    }

    private FriendStatus mapToFriendStatus(FriendTypeRequest type) {
        return switch (type) {
            case friend -> FriendStatus.FRIEND;
            case request -> FriendStatus.PENDING;
            case block -> FriendStatus.BLOCKED;
            case sent -> FriendStatus.FRIEND_REQUEST;
        };
    }

    @Operation(
            summary = "Xóa kết bạn",
            description = """
                    Xóa kết bạn. Không xóa dữ liệu mối liên hệ giữa 2 người mà đổi trạng thái sang người lạ (status == STRANGER). Để duy trì tên gợi nhớ của người dùng (display_name)<br>
                    <strong>Lưu ý:</strong> nếu 2 người dùng chưa kết bạn thì trả về lỗi
                    """
    )
    @DeleteMapping("/profile/friends/{friend-id}")
    public Friend deleteFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable(name = "friend-id") String friendId) {
        return userInfoService.deleteFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách phòng chat",
            description = """
                    Lấy danh sách phòng chat. Cả chat đơn và chat nhóm
                    """
    )
    @GetMapping("/profile/chats")
    public List<ChatRoomDTO> getAllChats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.getAllChats(userPrincipal);
    }

    @Operation(
            summary = "Cập nhật ẩn/hiện, xóa hội thoại hoặc ghim/bỏ ghim tin nhắn",
            description = """
                    Cập nhật ẩn/hiện, xóa hội thoại hoặc ghim/bỏ ghim tin nhắn
                    
                    <strong>Not Found: </strong>
                     - Không tìm thấy ID phòng chat
                    """
    )
    @PutMapping("/profile/chats/{chat-id}")
    public ChatRoomDTO updateUserChat(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("chat-id") String chatId,@Valid  @RequestBody UserChatUpdateDTO userChatUpdateDTO) {
        return userInfoService.updateUserChat(userPrincipal, chatId, userChatUpdateDTO);
    }

    @Operation(
            summary = "Lấy thông tin người dùng",
            description = "Lấy thông tin của người dùng dựa trên chuỗi JWT trong header"
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
                    """
    )
    @PutMapping("/profile")
    public UserInfo updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserInfoDTO userInfoDTO) {
        return userInfoService.updateUserInfo(userDetails.getUsername(), userInfoDTO);
    }
}
