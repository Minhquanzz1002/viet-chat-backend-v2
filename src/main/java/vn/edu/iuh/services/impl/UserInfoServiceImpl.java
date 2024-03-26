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
import vn.edu.iuh.exceptions.FriendshipRelationshipException;
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
    public UserInfo findUserInfoByUserId(String userId) {
        return userInfoRepository.findByUser(new User(userId)).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
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
    public Friend addFriendByUserId(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        return addFriend(currentUserInfo, friendUserInfo);
    }

    @Override
    public Friend addFriendByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        User friendUser = userRepository.findByPhone(phoneNumberDTO.getPhone()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        UserInfo friendUserInfo = userInfoRepository.findByUser(friendUser).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        return addFriend(currentUserInfo, friendUserInfo);
    }

    private Friend addFriend(UserInfo senderUserInfo, UserInfo receiverUserInfo) {
        boolean existed = false;
        for (Friend friend : senderUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(receiverUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.STRANGER)) {
                    existed = true;
                    friend.setStatus(FriendStatus.FRIEND_REQUEST);
                    break;
                }
                if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                    throw new FriendshipRelationshipException("Bạn đã chặn người này");
                }
                if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                    throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                }
                if (friend.getStatus().equals(FriendStatus.FRIEND)) {
                    throw new FriendshipRelationshipException("Hai người đã là bạn bè");
                }
                if (friend.getStatus().equals(FriendStatus.FRIEND_REQUEST)) {
                    throw new FriendshipRelationshipException("Bạn đã gửi lời mời kết bạn cho đối phương trước đó");
                }
                if (friend.getStatus().equals(FriendStatus.PENDING)) {
                    throw new FriendshipRelationshipException("Bạn có lời mời kết bạn từ đối phương");
                }
            }
        }

        if (existed) {
            for (Friend friend : receiverUserInfo.getFriends()) {
                if (friend.getUser().getId().equals(senderUserInfo.getId())) {
                    if (friend.getStatus().equals(FriendStatus.STRANGER)) {
                        friend.setStatus(FriendStatus.PENDING);
                    }
                }
            }
        } else {
            senderUserInfo.getFriends().add(new Friend(receiverUserInfo, receiverUserInfo.getFirstName() + " " + receiverUserInfo.getLastName(), FriendStatus.FRIEND_REQUEST));
            receiverUserInfo.getFriends().add(new Friend(senderUserInfo, senderUserInfo.getFirstName() + " " + senderUserInfo.getLastName(), FriendStatus.PENDING));
        }
        userInfoRepository.save(receiverUserInfo);
        UserInfo updatedSenderUserInfo = userInfoRepository.save(senderUserInfo);

        for (Friend friend : updatedSenderUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(receiverUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè sau khi kết bạn");
    }

    @Override
    public Friend blockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        userInfoRepository.findByIdAndFriendIdAndStatus(currentUserInfo.getId(), friendUserInfo.getId(), FriendStatus.BLOCK).ifPresent(userInfo -> {throw new DataExistsException("Người dùng đã bị chặn trước đó");});

        boolean currentUserFriendNotFound = true;
        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                friend.setStatus(FriendStatus.BLOCK);
                currentUserFriendNotFound = false;
                break;
            }
        }

        if (currentUserFriendNotFound) {
            currentUserInfo.getFriends().add(new Friend(friendUserInfo, friendUserInfo.getFirstName() + " " + friendUserInfo.getLastName(), FriendStatus.BLOCK));
        }

        boolean friendOfUserNotFound = true;
        for (Friend friend : friendUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(currentUserInfo.getId())) {
                friend.setStatus(FriendStatus.BLOCKED);
                friendOfUserNotFound = false;
                break;
            }
        }

        if (friendOfUserNotFound) {
            friendUserInfo.getFriends().add(new Friend(currentUserInfo, currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName(), FriendStatus.BLOCKED));
        }

        userInfoRepository.save(friendUserInfo);
        UserInfo updatedCurrentUserInfo = userInfoRepository.save(currentUserInfo);

        for (Friend friend : updatedCurrentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè sau khi chặn");
    }

    @Override
    public Friend unblockFriend(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                    friend.setStatus(FriendStatus.STRANGER);
                    break;
                } else {
                    throw new FriendshipRelationshipException("Không bị chặn.");
                }
            }
        }
        for (Friend friend : friendUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(currentUserInfo.getId())) {
                friend.setStatus(FriendStatus.STRANGER);
                break;
            }
        }

        userInfoRepository.save(friendUserInfo);
        UserInfo updatedCurrentUserInfo = userInfoRepository.save(currentUserInfo);

        for (Friend friend : updatedCurrentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè.");
    }

    @Override
    public Friend deleteFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.FRIEND)) {
                    friend.setStatus(FriendStatus.STRANGER);
                    break;
                } else {
                    throw new FriendshipRelationshipException("Chưa kết bạn.");
                }
            }
        }
        for (Friend friend : friendUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(currentUserInfo.getId())) {
                friend.setStatus(FriendStatus.STRANGER);
                break;
            }
        }

        userInfoRepository.save(friendUserInfo);
        UserInfo updatedCurrentUserInfo = userInfoRepository.save(currentUserInfo);

        for (Friend friend : updatedCurrentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè.");
    }

    @Override
    public Friend acceptFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.PENDING)) {
                    friend.setStatus(FriendStatus.FRIEND);
                    break;
                } else {
                    throw new FriendshipRelationshipException("Không có lời mời kết bạn nào được tìm thấy.");
                }
            }
        }
        for (Friend friend : friendUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(currentUserInfo.getId())) {
                friend.setStatus(FriendStatus.FRIEND);
                break;
            }
        }

        userInfoRepository.save(friendUserInfo);
        UserInfo updatedCurrentUserInfo = userInfoRepository.save(currentUserInfo);

        for (Friend friend : updatedCurrentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè.");
    }

    @Override
    public Friend declineFriendRequest(FriendRequestDTO friendRequestDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendRequestDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.PENDING)) {
                    friend.setStatus(FriendStatus.STRANGER);
                    break;
                } else {
                    throw new FriendshipRelationshipException("Không có lời mời kết bạn nào được tìm thấy.");
                }
            }
        }
        for (Friend friend : friendUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(currentUserInfo.getId())) {
                friend.setStatus(FriendStatus.STRANGER);
                break;
            }
        }

        userInfoRepository.save(friendUserInfo);
        UserInfo updatedCurrentUserInfo = userInfoRepository.save(currentUserInfo);

        for (Friend friend : updatedCurrentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                return friend;
            }
        }
        throw new DataNotFoundException("Không tìm thấy thông tin bạn bè.");
    }

    @Override
    public void updateAvatar(UserPrincipal userPrincipal, String linkAvatar) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        userInfo.setThumbnailAvatar(linkAvatar);
        userInfoRepository.save(userInfo);
    }

    @Override
    public void updateCoverImage(UserPrincipal userPrincipal, String linkCoverImage) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        userInfo.setCoverImage(linkCoverImage);
        userInfoRepository.save(userInfo);
    }
}
