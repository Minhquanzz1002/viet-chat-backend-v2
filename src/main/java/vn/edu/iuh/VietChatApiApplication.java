package vn.edu.iuh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.AttachmentType;
import vn.edu.iuh.models.enums.RoleType;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.ChatRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@Slf4j
public class VietChatApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VietChatApiApplication.class, args);
    }

//    @Bean
    public CommandLineRunner runner(ChatRepository chatRepository) {
        return args -> {
            Attachment attachment = new Attachment(AttachmentType.IMAGE, "https://example.com/image.jpg", "image.jpg");
            Attachment attachment1 = new Attachment(AttachmentType.IMAGE, "https://example.com/image.jpg", "image.jpg");
            Message message = new Message(List.of(attachment, attachment1));
            Message message1 = new Message(List.of(attachment, attachment1));
            Message message2 = new Message("Mot tin nhan", List.of(attachment, attachment1));
            chatRepository.insert(new Chat(null, List.of(message, message1, message2), null));
        };
    }

//    @Bean
    public CommandLineRunner saveUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.insert(new User("0354927402", passwordEncoder.encode("123456789"), UserStatus.ACTIVE, RoleType.USER));
            userRepository.insert(new User("0375716638", passwordEncoder.encode("123456789"), UserStatus.ACTIVE, RoleType.USER));
        };
    }

//    @Bean
    public CommandLineRunner saveUser(UserInfoRepository userInfoRepository) {
        return args -> {
            userInfoRepository.insert(new UserInfo("Nguyễn Minh", "Quân", "Dev", "Avatar", "cover image", true, LocalDate.now(), new User("65dcd570e36d2418dacb1b61")));
        };
    }
}
