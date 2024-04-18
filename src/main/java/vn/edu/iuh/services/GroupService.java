package vn.edu.iuh.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.dto.GroupDTO;
import vn.edu.iuh.dto.GroupRequestCreateDTO;
import vn.edu.iuh.dto.GroupRoleUpdateRequestDTO;
import vn.edu.iuh.dto.GroupUpdateRequestDTO;
import vn.edu.iuh.models.Group;
import vn.edu.iuh.models.GroupMember;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface GroupService {
    List<GroupMember> getAllMembers(String groupId, UserPrincipal userPrincipal);

    Group create(GroupRequestCreateDTO groupRequestCreateDTO, UserPrincipal userPrincipal);

    String leaveGroup(String groupId, UserPrincipal userPrincipal);

    GroupMember changeRoleMember(String groupId, String memberId, GroupRoleUpdateRequestDTO groupRoleUpdateRequestDTO, UserPrincipal userPrincipal);

    Group findById(String id);

    GroupDTO updateById(String id, GroupUpdateRequestDTO groupUpdateRequestDTO, UserPrincipal userPrincipal);

    Page<Group> findAllWithPagination(Pageable pageable);

    void deleteById(String id, UserPrincipal userPrincipal);

    List<GroupMember> addMembersToGroup(String groupId, List<String> users, UserDetails userDetails);

    void deleteMemberById(String groupId, String memberId, UserPrincipal userPrincipal);
}
