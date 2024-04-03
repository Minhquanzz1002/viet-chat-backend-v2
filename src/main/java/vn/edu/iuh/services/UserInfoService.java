package vn.edu.iuh.services;

import vn.edu.iuh.dto.ChatRoomDTO;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.UserInfoDTO;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserChat;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface UserInfoService {

    UserInfo findUserInfo(String phone);
    UserInfo findUserInfoByUserId(String userId);

    List<GroupDTO> findAllGroupToUserInfoByUserId(String userId);

    UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO);

    // friends
    Friend addFriendByUserId(String friendId, UserPrincipal userPrincipal);

    Friend addFriendByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal);

    Friend blockFriend(String friendId, UserPrincipal userPrincipal);
    Friend unblockFriend(String friendId, UserPrincipal userPrincipal);
    Friend deleteFriend(String friendId, UserPrincipal userPrincipal);
    Friend acceptFriendRequest(String friendId, UserPrincipal userPrincipal);
    Friend declineFriendRequest(String friendId, UserPrincipal userPrincipal);

    List<ChatRoomDTO> getAllChats(UserPrincipal userPrincipal);
}
