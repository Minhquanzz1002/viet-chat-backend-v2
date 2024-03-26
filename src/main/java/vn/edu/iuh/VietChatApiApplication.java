package vn.edu.iuh;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.models.*;
import vn.edu.iuh.models.enums.*;
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
            Message message1 = new Message((ObjectId) null, new UserInfo("65ddba794f8ab150db6dfa81"), "Hello", List.of(attachment, attachment1), null, MessageStatus.SENT);
            Message message2 = new Message((ObjectId) null, new UserInfo("65ddba794f8ab150db6dfa81"), "Hello", List.of(attachment, attachment1), List.of(new Reaction(new UserInfo("65ddba794f8ab150db6dfa81"), ReactionType.LIKE, 1)), MessageStatus.SENT);
            Message message3 = new Message((ObjectId) null, new UserInfo("65ddba794f8ab150db6dfa81"), null, List.of(attachment, attachment1), null, MessageStatus.SENT);
            chatRepository.insert(new Chat(false,List.of(message1, message2, message3)));
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

//    @Bean
//    public CommandLineRunner userChat(UserInfoRepository userInfoRepository) {
//        return args -> {
//            UserInfo userInfo = userInfoRepository.findById("65ddba794f8ab150db6dfa81").get();
//            userInfo.getChats().add(new UserChat(new Chat("65e84882726cb907cc6b6040"), new LastMessage("Đã gửi 1 tin nhắn", new User("65ddba794f8ab150db6dfa81"), LocalDateTime.now())));
//            userInfoRepository.save(userInfo);
//        };
//    }

//    @Bean
    public CommandLineRunner query2(UserInfoRepository userInfoRepository) {
        return args -> {
            UserInfo userInfo = userInfoRepository.findByIdAndFriendIdAndStatus("65ddba794f8ab150db6dfa81", "65df83a0f52cda6a46bde971", FriendStatus.FRIEND).get();
            log.info(userInfo.getId());
        };
    }
}
