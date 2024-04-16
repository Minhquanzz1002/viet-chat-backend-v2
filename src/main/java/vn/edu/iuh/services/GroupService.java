package vn.edu.iuh.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.dto.GroupRequestCreateDTO;
import vn.edu.iuh.models.Group;
import vn.edu.iuh.models.GroupMember;
import vn.edu.iuh.security.UserPrincipal;

import java.util.List;

public interface GroupService {
    List<GroupMember> getAllMembers(String groupId, UserPrincipal userPrincipal);
    Group create(GroupRequestCreateDTO groupRequestCreateDTO, UserPrincipal userPrincipal);
    Group findById(String id);
    Page<Group> findAllWithPagination(Pageable pageable);
    void deleteById(String id, UserPrincipal userPrincipal);
    Group addMembersToGroup(String groupId, List<String> users, UserDetails userDetails);
    Group deleteMemberById(String groupId, String memberId);
}
