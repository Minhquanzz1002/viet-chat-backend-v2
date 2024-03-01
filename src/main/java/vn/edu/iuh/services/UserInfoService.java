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

    List<GroupDTO> findAllGroupToUserInfoByUserId(String userId);

    UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO);

    // friends

    List<Friend> addFriendToUserInfo(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);

    List<Friend> addFriendToUserInfoByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal);

    List<Friend> blockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);

    List<Friend> acceptFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal);
}
