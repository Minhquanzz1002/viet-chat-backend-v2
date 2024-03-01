package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.FriendRequestDTO;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.UserInfoDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.Group;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserInfo createUserInfo(String phone, UserInfoDTO userInfoDTO) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + phone));
        if (userInfoRepository.existsByUser(user)) {
            throw new DataExistsException("Thông tin người dùng đã tồn tại. Không thể tạo mới.");
        }
        UserInfo userInfo = modelMapper.map(userInfoDTO, UserInfo.class);
        userInfo.setUser(user);
        return userInfoRepository.insert(userInfo);
    }

    @Override
    public UserInfo findUserInfo(String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + phone));
        return userInfoRepository.findByUser(user).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
    }

    @Override
    public List<GroupDTO> findAllGroupToUserInfoByUserId(String userId) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userId)).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
        List<Group> groups = userInfo.getGroups();
        List<GroupDTO> groupDTOList = new ArrayList<>();
        groups.forEach(group -> {
            GroupDTO groupDTO = modelMapper.map(group, GroupDTO.class);
            groupDTOList.add(groupDTO);
        });
        return groupDTOList;
    }

    @Override
    public UserInfo updateUserInfo(String phone, UserInfoDTO userInfoDTO) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + phone));
        UserInfo existingUserInfo = userInfoRepository.findByUser(user).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
        modelMapper.map(userInfoDTO, existingUserInfo);
        return userInfoRepository.save(existingUserInfo);
    }

    @Override
    public List<Friend> addFriendToUserInfo(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo userInfoFriend = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        Friend friendRequest = new Friend(userInfo, userInfo.getFirstName() + " " + userInfo.getLastName(), FriendStatus.FRIEND_REQUEST);
        userInfoFriend.getFriends().add(friendRequest);

        Friend friendPending = new Friend(userInfoFriend, userInfoFriend.getFirstName() + " " + userInfoFriend.getLastName(), FriendStatus.PENDING);
        userInfo.getFriends().add(friendPending);

        userInfoRepository.save(userInfoFriend);
        return userInfoRepository.save(userInfo).getFriends();
    }

    @Override
    public List<Friend> addFriendToUserInfoByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        User user = userRepository.findByPhone(phoneNumberDTO.getPhone()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng để kết bạn"));
        UserInfo userInfoFriend = userInfoRepository.findByUser(user).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        Friend friendRequest = new Friend(userInfo, userInfo.getFirstName() + " " + userInfo.getLastName(), FriendStatus.FRIEND_REQUEST);
        userInfoFriend.getFriends().add(friendRequest);

        Friend friendPending = new Friend(userInfoFriend, userInfoFriend.getFirstName() + " " + userInfoFriend.getLastName(), FriendStatus.PENDING);
        userInfo.getFriends().add(friendPending);

        userInfoRepository.save(userInfoFriend);
        return userInfoRepository.save(userInfo).getFriends();
    }

    @Override
    public List<Friend> blockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {

        return null;
    }

    @Override
    public List<Friend> acceptFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo userInfoFriend = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        userInfo.getFriends().forEach(friend -> {
            if (friend.getUser().getId().equals(friendRequestDTO.getUserId())) {
                friend.setStatus(FriendStatus.FRIEND);
            }
        });
        userInfoFriend.getFriends().forEach(friend -> {
            if (friend.getUser().getId().equals(userInfo.getId())) {
                friend.setStatus(FriendStatus.FRIEND);
            }
        });

        userInfoRepository.save(userInfoFriend);
        return userInfoRepository.save(userInfo).getFriends();
    }
}
