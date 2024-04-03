package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.ChatRoomDTO;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.PhoneNumberDTO;
import vn.edu.iuh.dto.UserInfoDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.FriendshipRelationshipException;
import vn.edu.iuh.exceptions.InvalidFriendshipRequestException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.models.enums.MessageStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.models.enums.NotificationType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

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
    public Friend addFriendByUserId(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId()))
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        if (friendId.equals(currentUserInfo.getId())) {
            throw new InvalidFriendshipRequestException("Bạn không thể kết bạn với chính mình");
        }

        UserInfo friendUserInfo = userInfoRepository.findById(friendId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        return addFriend(currentUserInfo, friendUserInfo);
    }

    @Override
    public Friend addFriendByPhone(PhoneNumberDTO phoneNumberDTO, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId()))
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        User friendUser = userRepository.findByPhone(phoneNumberDTO.getPhone())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        UserInfo friendUserInfo = userInfoRepository.findByUser(friendUser).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));
        return addFriend(currentUserInfo, friendUserInfo);
    }

    private Friend addFriend(UserInfo senderUserInfo, UserInfo receiverUserInfo) {
        return senderUserInfo.getFriends().stream()
                .filter(friend -> friend.getUser().getId().equals(receiverUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.STRANGER)) {
                        friend.setStatus(FriendStatus.FRIEND_REQUEST);

                        receiverUserInfo.getFriends().stream()
                                .filter(f -> f.getUser().getId().equals(senderUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.PENDING));
                        userInfoRepository.saveAll(Arrays.asList(senderUserInfo, receiverUserInfo));
                        Notification notification = new Notification(senderUserInfo.getLastName() + " vừa gửi lời mời kết bạn", NotificationType.FRIEND_REQUEST, senderUserInfo.getId(), LocalDateTime.now());
                        simpMessagingTemplate.convertAndSendToUser(receiverUserInfo.getId(), "/private", notification);
                        return friend;
                    } else if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này. Hãy bỏ chặn trước khi kết bạn");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else if (friend.getStatus().equals(FriendStatus.FRIEND)) {
                        throw new FriendshipRelationshipException("Hai người đã là bạn bè");
                    } else if (friend.getStatus().equals(FriendStatus.FRIEND_REQUEST)) {
                        throw new FriendshipRelationshipException("Bạn đã gửi lời mời kết bạn cho đối phương trước đó");
                    } else {
                        throw new FriendshipRelationshipException("Bạn có lời mời kết bạn từ đối phương");
                    }
                })
                .orElseGet(() -> {
                    Friend senderFriend = new Friend(receiverUserInfo, receiverUserInfo.getFirstName() + " " + receiverUserInfo.getLastName(), FriendStatus.FRIEND_REQUEST);
                    Friend receiverFriend = new Friend(senderUserInfo, senderUserInfo.getFirstName() + " " + senderUserInfo.getLastName(), FriendStatus.PENDING);
                    senderUserInfo.getFriends().add(senderFriend);
                    receiverUserInfo.getFriends().add(receiverFriend);
                    userInfoRepository.saveAll(Arrays.asList(senderUserInfo, receiverUserInfo));
                    Notification notification = new Notification(senderUserInfo.getLastName() + " vừa gửi lời mời kết bạn", NotificationType.FRIEND_REQUEST, senderUserInfo.getId(), LocalDateTime.now());
                    simpMessagingTemplate.convertAndSendToUser(receiverUserInfo.getId(), "/private", notification);
                    return senderFriend;
                });
    }

    @Override
    public Friend blockFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId()))
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        if (friendId.equals(currentUserInfo.getId())) {
            throw new InvalidFriendshipRequestException("Bạn không thể chặn chính mình");
        }

        UserInfo friendUserInfo = userInfoRepository.findById(friendId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để chặn"));

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getUser().getId().equals(friendUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này.");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else {
                        friend.setStatus(FriendStatus.BLOCK);

                        friendUserInfo.getFriends().stream()
                                .filter(f -> f.getUser().getId().equals(currentUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.BLOCKED));
                        userInfoRepository.saveAll(Arrays.asList(currentUserInfo, friendUserInfo));
                        log.info(friend.toString());
                        Friend fr = Friend.builder()
                                .isBestFriend(friend.isBestFriend())
                                .displayName(friend.getDisplayName())
                                .status(friend.getStatus())
                                .user(friend.getUser())
                                .build();
                        return fr;
                    }
                })
                .orElseGet(() -> {
                    Friend senderFriend = new Friend(friendUserInfo, friendUserInfo.getFirstName() + " " + friendUserInfo.getLastName(), FriendStatus.BLOCK);
                    Friend receiverFriend = new Friend(currentUserInfo, currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName(), FriendStatus.BLOCKED);
                    currentUserInfo.getFriends().add(senderFriend);
                    friendUserInfo.getFriends().add(receiverFriend);
                    userInfoRepository.saveAll(Arrays.asList(currentUserInfo, friendUserInfo));
                    return senderFriend;
                });
    }

    @Override
    public Friend unblockFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getUser().getId().equals(friendUserInfo.getId()) && friend.getStatus().equals(FriendStatus.BLOCK))
                .findFirst()
                .map(friend -> {
                    friend.setStatus(FriendStatus.STRANGER);
                    friendUserInfo.getFriends().stream()
                            .filter(f -> f.getUser().getId().equals(currentUserInfo.getId()))
                            .findFirst()
                            .ifPresent(f -> f.setStatus(FriendStatus.STRANGER));
                    userInfoRepository.saveAll(Arrays.asList(currentUserInfo, friendUserInfo));
                    return friend;
                })
                .orElseThrow(() -> new DataNotFoundException("Bạn không chặn đối phương nên không thể bỏ chặn"));
    }

    @Override
    public Friend deleteFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId()))
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để hủy kết bạn"));

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
    public Friend acceptFriendRequest(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId()))
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng để kết bạn"));


        Friend pendingFriendRequest = currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getUser().getId().equals(friendUserInfo.getId()) && friend.getStatus().equals(FriendStatus.PENDING))
                .findFirst()
                .orElseThrow(() -> new FriendshipRelationshipException("Không có lời mời kết bạn nào được tìm thấy."));

        Friend acceptedFriend = friendUserInfo.getFriends().stream()
                .filter(friend -> friend.getUser().getId().equals(currentUserInfo.getId()) && friend.getStatus().equals(FriendStatus.FRIEND_REQUEST))
                .findFirst()
                .orElseThrow(() -> new FriendshipRelationshipException("Không có lời mời kết bạn nào được tìm thấy."));

        Message message = Message
                .builder()
                .messageId(new ObjectId())
                .content("Hai bạn đã trở thành bạn bè")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .type(MessageType.EVENT)
                .status(MessageStatus.SENT)
                .build();
        Chat chat = chatRepository.save(
                Chat.builder()
                        .messages(List.of(message))
                        .members(List.of(currentUserInfo, friendUserInfo))
                        .lastMessage(
                                LastMessage.builder()
                                        .content("Hai bạn đã trở thành bạn bè")
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                        .build()
        );

        pendingFriendRequest.setStatus(FriendStatus.FRIEND);
        pendingFriendRequest.setChat(chat);

        acceptedFriend.setStatus(FriendStatus.FRIEND);
        acceptedFriend.setChat(chat);

        currentUserInfo.getChats().add(
                UserChat
                        .builder()
                        .chat(chat)
                        .joinTime(LocalDateTime.now())
                        .build()
        );

        friendUserInfo.getChats().add(
                UserChat
                        .builder()
                        .chat(chat)
                        .joinTime(LocalDateTime.now())
                        .build()
        );

        userInfoRepository.save(friendUserInfo);
        userInfoRepository.save(currentUserInfo);
        Notification notification = new Notification(currentUserInfo.getLastName() + " vừa chấp nhận lời mời kết bạn", NotificationType.NEW_MESSAGE, null, LocalDateTime.now());
        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
        return pendingFriendRequest;
    }

    @Override
    public Friend declineFriendRequest(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo friendUserInfo = userInfoRepository.findById(friendId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy đối phương, không thể hủy yêu cầu kết bạn"));

        for (Friend friend : currentUserInfo.getFriends()) {
            if (friend.getUser().getId().equals(friendUserInfo.getId())) {
                if (friend.getStatus().equals(FriendStatus.PENDING)) {
                    friend.setStatus(FriendStatus.STRANGER);
                    break;
                } else {
                    throw new FriendshipRelationshipException("Không tìm thấy lời mời kết bạn.");
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
    public List<ChatRoomDTO> getAllChats(UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        List<UserChat> userChatList =  userInfo.getChats();
        List<ChatRoomDTO> chatRoomDTOList = new ArrayList<>();
        userChatList.forEach(
                chat -> {
                    String name = null;
                    String avatar = null;
                    if (chat.getChat().getGroup() != null) {
                        name = chat.getChat().getGroup().getName();
                        avatar = chat.getChat().getGroup().getThumbnailAvatar();
                    }else {
                        for (int i = 0; i < chat.getChat().getMembers().size(); i++) {
                            if (!chat.getChat().getMembers().get(i).getId().equals(userInfo.getId())) {
                                name = chat.getChat().getMembers().get(i).getFirstName() + " " + chat.getChat().getMembers().get(i).getLastName();
                                avatar = chat.getChat().getMembers().get(i).getThumbnailAvatar();
                                break;
                            }
                        }
                    }
                    ChatRoomDTO chatRoomDTO = ChatRoomDTO.builder()
                            .id(chat.getChat().getId())
                            .name(name)
                            .avatar(avatar)
                            .lastMessage(chat.getChat().getLastMessage())
                            .isGroup(chat.getChat().getGroup() != null)
                            .build();
                    chatRoomDTOList.add(chatRoomDTO);
                }
        );
        return chatRoomDTOList;
    }
}
