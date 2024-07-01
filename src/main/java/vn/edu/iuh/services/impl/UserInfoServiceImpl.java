package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.FriendshipRelationshipException;
import vn.edu.iuh.exceptions.InvalidFriendshipRequestException;
import vn.edu.iuh.exceptions.InvalidRequestException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.*;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public OtherUserInfoDTO findUserInfoByPhone(String phone, UserPrincipal userPrincipal) {
        if (userPrincipal.getUsername().equals(phone)) {
            throw new InvalidRequestException("Bạn đang tự tìm kiếm chính mình");
        }

        UserInfo senderInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));

        User user = userRepository.findByPhone(phone).orElseThrow(() -> new DataNotFoundException("Số điện thoại chưa đăng ký tài khoản."));
        UserInfo userInfo = userInfoRepository.findByUser(user).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));

        senderInfo.getRecentSearches().remove(userInfo);
        senderInfo.getRecentSearches().add(0, userInfo);

        userInfoRepository.save(senderInfo);
        OtherUserInfoDTO otherUserInfoDTO = modelMapper.map(userInfo, OtherUserInfoDTO.class);
        otherUserInfoDTO.setPhone(userInfo.getUser().getPhone());
        return otherUserInfoDTO;
    }

    @Override
    public List<OtherUserInfoDTO> findRecentSearches(int size, UserPrincipal userPrincipal) {
        UserInfo senderInfo = findUserInfoByUserId(userPrincipal.getId());
        List<Friend> friends = senderInfo.getFriends();

        List<UserInfo> recentSearches = senderInfo.getRecentSearches();
        List<UserInfo> limitedRecentSearches = recentSearches.subList(0, Math.min(size, recentSearches.size()));

        return limitedRecentSearches.stream()
                .map(recentUserInfo -> {
                    OtherUserInfoDTO dto = modelMapper.map(recentUserInfo, OtherUserInfoDTO.class);
                    dto.setPhone(recentUserInfo.getUser().getPhone());

                    friends.forEach(friend -> {
                        if (friend.getProfile().equals(recentUserInfo)) {
                            dto.setStatus(friend.getStatus());
                            dto.setDisplayName(friend.getDisplayName());
                        }
                    });

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserInfo findUserInfo(String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + phone));
        return userInfoRepository.findByUser(user).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
    }

    @Override
    public UserInfo findById(String id) {
        return userInfoRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng có ID là " + id));
    }

    @Override
    public UserInfo findUserInfoByUserId(String userId) {
        log.info("Get profile by user ID");
        return userInfoRepository.findByUser(new User(userId)).orElseThrow(() -> new DataNotFoundException("Thông tin người dùng không tồn tại"));
    }

    @Override
    public List<GroupDTO> findAllGroupToUserInfoByUserId(String userId) {
        UserInfo userInfo = findUserInfoByUserId(userId);
        List<Group> groups = userInfo.getGroups();
        List<GroupDTO> groupDTOList = new ArrayList<>();
        groups.forEach(group -> {
            GroupDTO groupDTO = modelMapper.map(group, GroupDTO.class);
            groupDTOList.add(groupDTO);
        });
        return groupDTOList;
    }

    @Override
    public List<GroupDTO> findAllGroupToUserInfoByUserInfoId(String userInfoId) {
        UserInfo userInfo = findById(userInfoId);
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
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());

        if (friendId.equals(currentUserInfo.getId())) {
            throw new InvalidFriendshipRequestException("Bạn không thể kết bạn với chính mình");
        }

        UserInfo friendUserInfo = findById(friendId);

        return addFriend(currentUserInfo, friendUserInfo);
    }

    private Friend addFriend(UserInfo senderUserInfo, UserInfo receiverUserInfo) {
        return senderUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(receiverUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.STRANGER)) {
                        friend.setStatus(FriendStatus.FRIEND_REQUEST);

                        receiverUserInfo.getFriends().stream()
                                .filter(f -> f.getProfile().getId().equals(senderUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.PENDING));
                        userInfoRepository.save(senderUserInfo);
                        userInfoRepository.save(receiverUserInfo);
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
                    userInfoRepository.save(senderUserInfo);
                    userInfoRepository.save(receiverUserInfo);
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

        UserInfo friendUserInfo = findById(friendId);

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này.");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else {
                        friend.setStatus(FriendStatus.BLOCK);

                        friendUserInfo.getFriends().stream()
                                .filter(f -> f.getProfile().getId().equals(currentUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.BLOCKED));
                        userInfoRepository.save(currentUserInfo);
                        userInfoRepository.save(friendUserInfo);
                        Notification notification = new Notification(currentUserInfo.getLastName() + " đã chặn bạn", NotificationType.BLOCKED_FRIEND, currentUserInfo.getId(), LocalDateTime.now());
                        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                        return friend;
                    }
                })
                .orElseGet(() -> {
                    Friend senderFriend = new Friend(friendUserInfo, friendUserInfo.getFirstName() + " " + friendUserInfo.getLastName(), FriendStatus.BLOCK);
                    Friend receiverFriend = new Friend(currentUserInfo, currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName(), FriendStatus.BLOCKED);
                    currentUserInfo.getFriends().add(senderFriend);
                    friendUserInfo.getFriends().add(receiverFriend);
                    userInfoRepository.save(currentUserInfo);
                    userInfoRepository.save(friendUserInfo);
                    Notification notification = new Notification(currentUserInfo.getLastName() + " đã chặn bạn", NotificationType.BLOCKED_FRIEND, currentUserInfo.getId(), LocalDateTime.now());
                    simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                    return senderFriend;
                });
    }

    @Override
    public Friend unblockFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());
        UserInfo friendUserInfo = findById(friendId);

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()) && friend.getStatus().equals(FriendStatus.BLOCK))
                .findFirst()
                .map(friend -> {
                    friend.setStatus(FriendStatus.STRANGER);
                    friendUserInfo.getFriends().stream()
                            .filter(f -> f.getProfile().getId().equals(currentUserInfo.getId()))
                            .findFirst()
                            .ifPresent(f -> f.setStatus(FriendStatus.STRANGER));
                    userInfoRepository.save(currentUserInfo);
                    userInfoRepository.save(friendUserInfo);
                    Notification notification = new Notification(currentUserInfo.getLastName() + " đã bỏ chặn bạn", NotificationType.UNBLOCKED_FRIEND, currentUserInfo.getId(), LocalDateTime.now());
                    simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                    return friend;
                })
                .orElseThrow(() -> new DataNotFoundException("Bạn không chặn đối phương nên không thể bỏ chặn"));
    }

    @Override
    public Friend deleteFriend(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());
        UserInfo friendUserInfo = findById(friendId);

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.FRIEND)) {
                        friend.setStatus(FriendStatus.STRANGER);

                        friendUserInfo.getFriends().stream()
                                .filter(f -> f.getProfile().getId().equals(currentUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.STRANGER));

                        userInfoRepository.save(currentUserInfo);
                        userInfoRepository.save(friendUserInfo);
                        Notification notification = new Notification(currentUserInfo.getLastName() + " vừa xóa kết bạn với bạn", NotificationType.DELETED_FRIEND, currentUserInfo.getId(), LocalDateTime.now());
                        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                        return friend;
                    } else if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này. Hãy bỏ chặn trước");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else {
                        throw new FriendshipRelationshipException("Chưa kết bạn.");
                    }
                })
                .orElseThrow(() -> new FriendshipRelationshipException("Chưa kết bạn."));
    }

    @Override
    public Friend acceptFriendRequest(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());
        UserInfo friendUserInfo = findById(friendId);


        Friend pendingFriendRequest = currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()) && friend.getStatus().equals(FriendStatus.PENDING))
                .findFirst()
                .orElseThrow(() -> new FriendshipRelationshipException("Không có lời mời kết bạn nào được tìm thấy."));

        Friend acceptedFriend = friendUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(currentUserInfo.getId()) && friend.getStatus().equals(FriendStatus.FRIEND_REQUEST))
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
        Chat chat;

        if (pendingFriendRequest.getChat() == null || pendingFriendRequest.getChat().getId() == null) {
            chat = chatRepository.save(
                    Chat.builder()
                            .messages(List.of(message))
                            .members(List.of(currentUserInfo, friendUserInfo))
                            .lastMessage(
                                    LastMessage.builder()
                                            .messageId(message.getMessageId())
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
        } else {
            chat = chatRepository.findById(pendingFriendRequest.getChat().getId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat"));
            chat.getMessages().add(message);
            chat.setLastMessage(
                    LastMessage.builder()
                            .messageId(message.getMessageId())
                            .content("Hai bạn đã trở thành bạn bè")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            chatRepository.save(chat);

            pendingFriendRequest.setStatus(FriendStatus.FRIEND);
            acceptedFriend.setStatus(FriendStatus.FRIEND);
        }

        userInfoRepository.save(currentUserInfo);
        userInfoRepository.save(friendUserInfo);
        Notification notification = new Notification(currentUserInfo.getLastName() + " vừa chấp nhận lời mời kết bạn", NotificationType.ACCEPT_FRIEND_REQUEST, null, LocalDateTime.now());
        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
        return pendingFriendRequest;
    }

    @Override
    public String declineFriendRequest(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());
        UserInfo friendUserInfo = findById(friendId);

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.PENDING)) {
                        friend.setStatus(FriendStatus.STRANGER);

                        friendUserInfo.getFriends().stream()
                                .filter(f -> f.getProfile().getId().equals(currentUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.STRANGER));

                        userInfoRepository.save(currentUserInfo);
                        userInfoRepository.save(friendUserInfo);
                        Notification notification = new Notification(currentUserInfo.getLastName() + " đã từ chối lời mời kết bạn của bạn", NotificationType.DECLINE_FRIEND_REQUEST, currentUserInfo.getId(), LocalDateTime.now());
                        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                        return "Từ chối lời mời kết bạn từ " + friendUserInfo.getUser().getPhone() + " thành công.";
                    } else if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này. Hãy bỏ chặn trước");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else {
                        throw new FriendshipRelationshipException("Không có yêu cầu kết bạn nào được tìm thấy.");
                    }
                })
                .orElseThrow(() -> new FriendshipRelationshipException("Không có yêu cầu kết bạn nào được tìm thấy."));
    }

    @Override
    public Friend cancelFriendRequest(String friendId, UserPrincipal userPrincipal) {
        UserInfo currentUserInfo = findUserInfoByUserId(userPrincipal.getId());
        UserInfo friendUserInfo = findById(friendId);

        return currentUserInfo.getFriends().stream()
                .filter(friend -> friend.getProfile().getId().equals(friendUserInfo.getId()))
                .findFirst()
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.FRIEND_REQUEST)) {
                        friend.setStatus(FriendStatus.STRANGER);

                        friendUserInfo.getFriends().stream()
                                .filter(f -> f.getProfile().getId().equals(currentUserInfo.getId()))
                                .findFirst()
                                .ifPresent(f -> f.setStatus(FriendStatus.STRANGER));

                        userInfoRepository.save(currentUserInfo);
                        userInfoRepository.save(friendUserInfo);
                        Notification notification = new Notification(currentUserInfo.getLastName() + " đã thu hồi lời mời kết bạn", NotificationType.CANCEL_FRIEND_REQUEST, currentUserInfo.getId(), LocalDateTime.now());
                        simpMessagingTemplate.convertAndSendToUser(friendUserInfo.getId(), "/private", notification);
                        return friend;
                    } else if (friend.getStatus().equals(FriendStatus.BLOCK)) {
                        throw new FriendshipRelationshipException("Bạn đã chặn người này. Hãy bỏ chặn trước");
                    } else if (friend.getStatus().equals(FriendStatus.BLOCKED)) {
                        throw new FriendshipRelationshipException("Bạn đã bị chặn đối phương");
                    } else {
                        throw new FriendshipRelationshipException("Không có yêu cầu kết bạn nào được tìm thấy.");
                    }
                })
                .orElseThrow(() -> new FriendshipRelationshipException("Không có yêu cầu kết bạn nào được tìm thấy."));
    }

    @Override
    public List<ChatRoomDTO> getAllChats(UserPrincipal userPrincipal) {
        UserInfo senderInfo = findUserInfoByUserId(userPrincipal.getId());
        List<UserChat> userChats = senderInfo.getChats();
        return userChats.stream()
                .filter(userChat -> !userChat.getStatus().equals(UserChatStatus.DELETED))
                .map(chat -> buildChatRoomDTO(chat, senderInfo))
                .sorted((chat1, chat2) -> {
                    if (chat1.getStatus().equals(UserChatStatus.PINNED) && chat2.getStatus().equals(UserChatStatus.PINNED)) {
                        return chat2.getName().compareTo(chat1.getName());
                    } else if (chat1.getStatus().equals(UserChatStatus.PINNED)) {
                        return -1;
                    } else if (chat2.getStatus().equals(UserChatStatus.PINNED)) {
                        return 1;
                    } else {
                        LocalDateTime time1 = chat1.getLastMessage().getCreatedAt();
                        LocalDateTime time2 = chat2.getLastMessage().getCreatedAt();
                        return time2.compareTo(time1);
                    }
                })
                .collect(Collectors.toList());

    }

    @Override
    public ChatRoomDTO updateUserChat(UserPrincipal userPrincipal, String chatId, UserChatUpdateDTO userChatUpdateDTO) {
        UserInfo senderInfo = findUserInfoByUserId(userPrincipal.getId());
        Optional<UserChat> chatToUpdate = senderInfo.getChats().stream()
                .filter(userChat -> userChat.getChat().getId().equals(chatId))
                .findFirst();
        if (chatToUpdate.isPresent()) {
            UserChat userChat = chatToUpdate.get();
            userChat.setStatus(userChatUpdateDTO.getStatus());
            if (userChatUpdateDTO.getStatus().equals(UserChatStatus.DELETED)) {
                userChat.setLastDeleteChatTime(LocalDateTime.now());
                Chat chat = chatRepository.findById(userChat.getChat().getId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy phòng chat để cập nhật"));
                chat.getDeleteBy().add(senderInfo.getId());
                chatRepository.save(chat);
            }
            userInfoRepository.save(senderInfo);
            return buildChatRoomDTO(userChat, senderInfo);
        }
        throw new DataNotFoundException("Không tìm thấy phòng chat");
    }

    private ChatRoomDTO buildChatRoomDTO(UserChat chat, UserInfo senderInfo) {
        Chat chatRoom = chat.getChat();
        Group group = chatRoom.getGroup();
        boolean isGroup = (group != null);
        String name = isGroup ? chatRoom.getGroup().getName() : getMemberName(chatRoom, senderInfo);
        String avatar = isGroup ? chatRoom.getGroup().getThumbnailAvatar() : getMemberAvatar(chatRoom, senderInfo);
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .name(name)
                .avatar(avatar)
                .lastMessage(chatRoom.getLastMessage())
                .groupId(isGroup ? chatRoom.getGroup().getId() : null)
                .lastSeenMessageId(chat.getLastSeenMessageId())
                .status(chat.getStatus())
                .build();
    }

    public static String getMemberName(Chat chatRoom, UserInfo senderInfo) {
        return chatRoom.getMembers().stream()
                .filter(member -> !member.getId().equals(senderInfo.getId()))
                .map(member -> member.getFirstName() + " " + member.getLastName())
                .findFirst()
                .orElse(null);
    }

    public static String getMemberAvatar(Chat chatRoom, UserInfo senderInfo) {
        return chatRoom.getMembers().stream()
                .filter(member -> !member.getId().equals(senderInfo.getId()))
                .map(UserInfo::getThumbnailAvatar)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
