package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.GroupRequestCreateDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.InvalidRequestException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.GroupMemberRole;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.GroupRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.GroupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserInfoRepository userInfoRepository;
    private final ChatRepository chatRepository;

    @Override
    public List<GroupMember> getAllMembers(String groupId, UserPrincipal userPrincipal) {
        Group group = findById(groupId);
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy thông tin người dùng"));
        if (!group.getMembers().contains(GroupMember.builder().profile(userInfo).build())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của nhóm này");
        }
        return group.getMembers();
    }


    @Override
    public Group create(GroupRequestCreateDTO groupRequestCreateDTO, UserPrincipal userPrincipal) {
        groupRequestCreateDTO.getMembers().forEach((memberId) -> {
            if (!userInfoRepository.existsById(memberId)) {
                throw new InvalidRequestException("Có một người dùng không tồn tại. Không thể tạo nhóm");
            }
        });

        Group group = Group
                .builder()
                .name(groupRequestCreateDTO.getName())
                .thumbnailAvatar(groupRequestCreateDTO.getThumbnailAvatar())
                .build();
        Group insertedGroup = groupRepository.insert(group);

        Message message = Message.builder()
                .messageId(new ObjectId())
                .type(MessageType.EVENT)
                .content("Hãy bắt đầu trò chuyện và chia sẻ cùng nhau.")
                .createdAt(LocalDateTime.now())
                .build();
        Chat chat = Chat.builder()
                .group(group)
                .messages(List.of(message))
                .lastMessage(
                        LastMessage.builder()
                                .messageId(message.getMessageId())
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build()
                )
                .build();
        chatRepository.save(chat);

        // add group leader
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy thông tin người dùng"));
        insertedGroup.getMembers().add(new GroupMember(userInfo, GroupMemberRole.GROUP_LEADER, GroupMemberRole.GROUP_LEADER.getDescription()));
        userInfo.getGroups().add(insertedGroup);
        userInfo.getChats().add(UserChat.builder().chat(chat).joinTime(LocalDateTime.now()).build());
        userInfoRepository.save(userInfo);

        // add members
        groupRequestCreateDTO.getMembers().forEach((memberId) -> {
            UserInfo userInfoMember = userInfoRepository.findById(memberId).orElseThrow(() -> new DataNotFoundException("Người dùng không tồn tại"));
            insertedGroup.getMembers().add(new GroupMember(userInfoMember, GroupMemberRole.MEMBER, "Thêm bởi nhóm trưởng"));
            userInfoMember.getGroups().add(insertedGroup);
            userInfoMember.getChats().add(UserChat.builder().chat(chat).joinTime(LocalDateTime.now()).build());
            userInfoRepository.save(userInfoMember);
        });

        insertedGroup.setChat(chat);
        return groupRepository.save(insertedGroup);
    }

    @Override
    public Group findById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhóm có ID là " + id));
    }

    @Override
    public Page<Group> findAllWithPagination(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    @Override
    public void deleteById(String id, UserPrincipal userPrincipal) {
        UserInfo userInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(id);
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(userInfo) && groupMember.getRole().equals(GroupMemberRole.GROUP_LEADER));
        if (isValid) {
            groupRepository.delete(group);
        } else {
            throw new AccessDeniedException("Bạn không phải là nhóm trưởng nên không thể giải tán nhóm");
        }
    }

    @Override
    public Group addMembersToGroup(String groupId, List<String> users, UserDetails userDetails) {
        // validate whether the user is in the group
        UserInfo userInfo = userInfoRepository.findByUser(new User(((UserPrincipal) userDetails).getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(groupId);

        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(userInfo));

        if (isValid) {
            List<UserInfo> userInfos = userInfoRepository.findAllById(users);
            userInfos.forEach(userInfo1 -> {
                if (group.getMembers().stream().noneMatch(groupMember -> groupMember.getProfile().equals(userInfo1))) {
                    GroupMember groupMember = new GroupMember(userInfo1, GroupMemberRole.MEMBER, "Thêm bởi " + userInfo.getLastName());
                    group.getMembers().add(groupMember);
                    userInfo1.getGroups().add(group);
                    userInfoRepository.save(userInfo1);
                }
            });
            return groupRepository.save(group);
        }
        throw new AccessDeniedException("Bạn không phải thành viên của nhóm này");
    }


    @Override
    public Group deleteMemberById(String groupId, String memberId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhóm có ID là " + groupId));
        List<GroupMember> members = group.getMembers();
        boolean removed = members.removeIf(member -> member.getProfile().getId().equals(memberId));
        if (!removed) {
            throw new DataNotFoundException("Không tìm thấy thành viên.");
        }
        return groupRepository.save(group);
    }
}
