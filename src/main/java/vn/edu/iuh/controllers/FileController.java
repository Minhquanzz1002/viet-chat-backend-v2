package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.dto.UploadFileRequestDTO;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.impl.S3Service;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Files Controller", description = "Quản lý file")
public class FileController {
    private final S3Service s3Service;
    @PostMapping
    @Operation(
            summary = "Lấy link upload file lên S3",
            description = """
            Lấy link upload file lên S3. Sau khi gọi sẽ trả về 1 url. Dùng url vừa nhận để upload file lên S3 Bucket bằng phương thức PUT
            """
    )
    public String saveFile(@RequestBody @Valid UploadFileRequestDTO uploadFileRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return s3Service.save(uploadFileRequestDTO.getFilename(), uploadFileRequestDTO.getType(), userPrincipal.getId());
    }
}
