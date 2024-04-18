package vn.edu.iuh.services;

import vn.edu.iuh.dto.*;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface UserInfoService {
    OtherUserInfoDTO findUserInfoByPhone(String phone, String senderId);
    UserInfo findUserInfo(String phone);
    UserInfo findById(String id);
    UserInfo findUserInfoByUserId(String userId);

    List<GroupDTO> findAllGroupToUserInfoByUserId(String userId);

    UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO);

    // friends
    String addFriendByUserId(String friendId, UserPrincipal userPrincipal);

    String addFriendByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal);

    String blockFriend(String friendId, UserPrincipal userPrincipal);
    String unblockFriend(String friendId, UserPrincipal userPrincipal);
    String deleteFriend(String friendId, UserPrincipal userPrincipal);
    String acceptFriendRequest(String friendId, UserPrincipal userPrincipal);
    String declineFriendRequest(String friendId, UserPrincipal userPrincipal);
    String cancelFriendRequest(String friendId, UserPrincipal userPrincipal);

    List<ChatRoomDTO> getAllChats(UserPrincipal userPrincipal);
}
