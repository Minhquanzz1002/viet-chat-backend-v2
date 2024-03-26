package vn.edu.iuh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    private String replyMessageId;
    private String content;
    private List<MultipartFile> files;
}
