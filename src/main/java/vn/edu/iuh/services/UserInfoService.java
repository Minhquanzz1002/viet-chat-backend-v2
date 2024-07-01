package vn.edu.iuh.services;

import vn.edu.iuh.dto.*;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface UserInfoService {
    OtherUserInfoDTO findUserInfoByPhone(String phone, UserPrincipal userPrincipal);
    List<OtherUserInfoDTO> findRecentSearches(int size ,UserPrincipal userPrincipal);
    UserInfo findUserInfo(String phone);
    UserInfo findById(String id);
    UserInfo findUserInfoByUserId(String userId);

    List<GroupDTO> findAllGroupToUserInfoByUserId(String userId);
    List<GroupDTO> findAllGroupToUserInfoByUserInfoId(String userInfoId);

    UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO);

    // friends
    Friend addFriendByUserId(String friendId, UserPrincipal userPrincipal);

    Friend blockFriend(String friendId, UserPrincipal userPrincipal);
    Friend unblockFriend(String friendId, UserPrincipal userPrincipal);
    Friend deleteFriend(String friendId, UserPrincipal userPrincipal);
    Friend acceptFriendRequest(String friendId, UserPrincipal userPrincipal);
    String declineFriendRequest(String friendId, UserPrincipal userPrincipal);
    Friend cancelFriendRequest(String friendId, UserPrincipal userPrincipal);

    List<ChatRoomDTO> getAllChats(UserPrincipal userPrincipal);
    ChatRoomDTO updateUserChat(UserPrincipal userPrincipal, String chatId, UserChatUpdateDTO userChatUpdateDTO);
}
