package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.GroupRequestCreateDTO;
import vn.edu.iuh.models.Group;
import vn.edu.iuh.models.GroupMember;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.GroupService;

import java.util.List;

@RestController
@RequestMapping("/v1/groups")
@Tag(name = "Groups Controller", description = "Quản lý nhóm chat")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/{group_id}/members")
    @Operation(summary = "Thêm thành viên vào nhóm", description = "Thêm một hoặc nhiều thành viên vào nhóm chat")
    public Group addMembers(@PathVariable(name = "group_id") String groupId, @RequestBody List<String> users, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.addMembersToGroup(groupId, users, userDetails);
    }

    @GetMapping("/{group_id}/members")
    @Operation(
            summary = "Lấy danh sách thành viên của nhóm",
            description = """
                    Lấy danh sách thành viên của nhóm
                    
                    <strong>Forbidden: </strong>
                     - Bạn không phải là thành viên của nhóm
                    
                    <strong>Not Found: </strong>
                     - Không tìm thấy ID nhóm
                    """
    )
    public List<GroupMember> getMembers(@PathVariable(name = "group_id") String groupId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return groupService.getAllMembers(groupId, userPrincipal);
    }

    @DeleteMapping("/{group_id}/members/{member_id}")
    @Operation(summary = "Xóa một thành viên khỏi nhóm. Chức năng này chỉ dành cho nhóm trưởng", description = "Xóa một thành viên khỏi nhóm chat theo ID. **Chức năng này chỉ dành cho nhóm trưởng**")
    public Group deleteMember(@PathVariable(name = "group_id") String groupId, @PathVariable(name = "member_id") String memberId) {
        return groupService.deleteMemberById(groupId, memberId);
    }

    @Operation(
            summary = "Tạo nhóm mới",
            description = """
                    Tạo nhóm với tối thiểu 2 thành viên (trừ bạn). Chú ý ResponseStatus khi thành công là `CREATED-201`
                    
                    Ảnh nhóm được phép bỏ qua. Hãy xử lý nó dưới client
                    
                    <strong>Bad Request: </strong>
                     - Dữ liệu đầu vào không hợp lệ
                     - Có một người dùng trong danh sách `members` không tồn tại
                    """
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Group createGroup(@RequestBody @Valid GroupRequestCreateDTO groupRequestCreateDTO, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.create(groupRequestCreateDTO, (UserPrincipal) userDetails);
    }

    @Operation(summary = "Lấy thông tin nhóm theo ID")
    @GetMapping("/{group-id}")
    public Group getGroup(@PathVariable("group-id") String id) {
        return groupService.findById(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Giải tán nhóm",
            description = """
                    Giải tán nhóm. Chức năng này chỉ dành cho nhóm trưởng `role="GROUP_LEADER"`
                    
                    <strong>Forbidden: </strong>
                     - Bạn không phải là nhóm trưởng
                    
                    <strong>Not Found: </strong>
                     - Không tìm thấy ID nhóm
                    """
    )
    @DeleteMapping("/{group-id}")
    public void deleteGroup(@PathVariable("group-id") String id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        groupService.deleteById(id, userPrincipal);
    }

}
