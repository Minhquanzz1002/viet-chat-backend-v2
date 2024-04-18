package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.GroupRequestCreateDTO;
import vn.edu.iuh.dto.GroupRoleUpdateRequestDTO;
import vn.edu.iuh.dto.GroupUpdateRequestDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.InvalidRequestException;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.GroupMemberRole;
import vn.edu.iuh.models.enums.GroupStatus;
import vn.edu.iuh.models.enums.MessageType;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.GroupRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.GroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserInfoRepository userInfoRepository;
    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;

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
    public String leaveGroup(String groupId, UserPrincipal userPrincipal) {
        Group group = findById(groupId);
        UserInfo senderInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        if (group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo) && groupMember.getRole().equals(GroupMemberRole.GROUP_LEADER))) {
            throw new AccessDeniedException("Bạn là nhóm trưởng không thể rời nhóm. Hãy chuyển giao vị trí trước khi rời đi");
        }
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo));
        if (isValid) {
            group.getMembers().removeIf(groupMember -> groupMember.getProfile().equals(senderInfo));
            groupRepository.save(group);

            senderInfo.getChats().removeIf(userChat -> userChat.getChat().equals(group.getChat()));
            senderInfo.getGroups().remove(group);
            userInfoRepository.save(senderInfo);
            return "Rời khỏi nhóm thành công";
        } else {
            throw new AccessDeniedException("Bạn không phải là thành viên của nhóm");
        }
    }

    @Override
    public GroupMember changeRoleMember(String groupId, String memberId, GroupRoleUpdateRequestDTO groupRoleUpdateRequestDTO, UserPrincipal userPrincipal) {
        UserInfo senderInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        UserInfo memberInfo = userInfoRepository.findById(memberId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(groupId);
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo) && groupMember.getRole().equals(GroupMemberRole.GROUP_LEADER));
        if (isValid) {
            Optional<GroupMember> updatedMember = group.getMembers().stream()
                    .filter(groupMember -> groupMember.getProfile().equals(memberInfo))
                    .findFirst()
                    .map(groupMember -> {
                        groupMember.setRole(groupRoleUpdateRequestDTO.getRole());
                        return groupMember;
                    });
            if (updatedMember.isPresent()) {
                groupRepository.save(group);
                return updatedMember.get();
            } else {
                throw new DataNotFoundException("Không tìm thấy thành viên trong nhóm");
            }
        } else {
            throw new AccessDeniedException("Bạn phải là nhóm trưởng");
        }
    }

    @Override
    public Group findById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhóm có ID là " + id));
    }

    @Override
    public GroupDTO updateById(String id, GroupUpdateRequestDTO groupUpdateRequestDTO, UserPrincipal userPrincipal) {
        UserInfo senderInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(id);
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo));
        if (isValid) {
            modelMapper.map(groupUpdateRequestDTO, group);
            groupRepository.save(group);
            return modelMapper.map(group, GroupDTO.class);
        } else {
            throw new AccessDeniedException("Bạn không phải là thành viên nhóm");
        }
    }

    @Override
    public Page<Group> findAllWithPagination(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    @Override
    public void deleteById(String id, UserPrincipal userPrincipal) {
        UserInfo sender = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(id);
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(sender) && groupMember.getRole().equals(GroupMemberRole.GROUP_LEADER));
        if (isValid) {
            group.getMembers().forEach(groupMember -> {
                UserInfo member = userInfoRepository.findById(groupMember.getProfile().getId()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy thành viên"));
                member.getGroups().remove(group);
                member.getChats().removeIf(userChat -> group.getChat().equals(userChat.getChat()));
                userInfoRepository.save(member);
            });
            group.setStatus(GroupStatus.DELETED);
            groupRepository.save(group);
        } else {
            throw new AccessDeniedException("Bạn không phải là nhóm trưởng nên không thể giải tán nhóm");
        }
    }

    @Override
    public List<GroupMember> addMembersToGroup(String groupId, List<String> users, UserDetails userDetails) {
        // validate whether the user is in the group
        UserInfo senderInfo = userInfoRepository.findByUser(new User(((UserPrincipal) userDetails).getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(groupId);

        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo));
        List<GroupMember> addedMembers = new ArrayList<>();
        if (isValid) {
            List<UserInfo> memberInfos = userInfoRepository.findAllById(users);
            memberInfos.forEach(memberInfo -> {
                if (group.getMembers().stream().noneMatch(groupMember -> groupMember.getProfile().equals(memberInfo))) {
                    GroupMember newGroupMember;
                    boolean isLeader = group.getMembers().stream().anyMatch(groupMember1 -> groupMember1.getProfile().equals(senderInfo) && groupMember1.getRole().equals(GroupMemberRole.GROUP_LEADER));
                    if (isLeader) {
                        newGroupMember = new GroupMember(memberInfo, GroupMemberRole.MEMBER, "Thêm bởi nhóm trưởng");
                    } else {
                        newGroupMember = new GroupMember(memberInfo, GroupMemberRole.MEMBER, "Thêm bởi " + senderInfo.getLastName());
                    }
                    addedMembers.add(newGroupMember);
                    group.getMembers().add(newGroupMember);
                    memberInfo.getGroups().add(group);
                    memberInfo.getChats().add(UserChat.builder().chat(group.getChat()).build());
                    userInfoRepository.save(memberInfo);
                }
            });
            groupRepository.save(group);
            return addedMembers;
        }
        throw new AccessDeniedException("Bạn không phải thành viên của nhóm này");
    }


    @Override
    public void deleteMemberById(String groupId, String memberId, UserPrincipal userPrincipal) {
        UserInfo senderInfo = userInfoRepository.findByUser(new User(userPrincipal.getId())).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
        Group group = findById(groupId);
        boolean isValid = group.getMembers().stream().anyMatch(groupMember -> groupMember.getProfile().equals(senderInfo) && (groupMember.getRole().equals(GroupMemberRole.GROUP_LEADER) || groupMember.getRole().equals(GroupMemberRole.DEPUTY_GROUP_LEADER)));
        if (isValid) {
            if (!group.getMembers().removeIf(member -> member.getProfile().getId().equals(memberId))) {
                throw new DataNotFoundException("Thành viên này không thuộc nhóm hoặc không tồn tại");
            }
            UserInfo memberInfo = userInfoRepository.findById(memberId).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
            memberInfo.getGroups().remove(group);
            memberInfo.getChats().remove(UserChat.builder().chat(group.getChat()).build());
            userInfoRepository.save(memberInfo);
            groupRepository.save(group);
        } else {
            throw new AccessDeniedException("Bạn phải là thành viên và có vai trò nhóm trưởng hoặc nhóm phó");
        }
    }
}
