package vn.edu.iuh.services;

import vn.edu.iuh.dto.FriendRequestDTO;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.UserInfoDTO;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface UserInfoService {
    UserInfo createUserInfo(String phone, UserInfoDTO userInfoDTO);

    UserInfo findUserInfo(String phone);
    UserInfo findUserInfoByUserId(String userId);

    List<GroupDTO> findAllGroupToUserInfoByUserId(String userId);

    UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO);

    // friends
    Friend addFriendByUserId(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);

    Friend addFriendByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal);

    Friend blockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);
    Friend unblockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);
    Friend deleteFriend(String friendId, UserPrincipal userPrincipal);
    Friend acceptFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);
    Friend declineFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);

    void updateAvatar(UserPrincipal userPrincipal, String linkAvatar);
    void updateCoverImage(UserPrincipal userPrincipal, String linkCoverImage);
}
