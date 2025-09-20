package com.whatsappclone.services.user;

import com.whatsappclone.entities.user.User;
import com.whatsappclone.mappers.UserMapper;
import com.whatsappclone.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSynchronizer {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void synchronizeWithIdp(Jwt token) {
        log.info("Synchronizing user with idp");
        getUserEmail(token).ifPresent(email -> {
            log.info("Synchronizing user with email {}", email);
            Optional<User> optionalUser = userRepository.findByEmail(email);
            User user = userMapper.fromTokenAttributes(token.getClaims());
            optionalUser.ifPresent(value -> user.setId(optionalUser.get().getId()));

            userRepository.save(user);
        });
    }

    private Optional<String> getUserEmail(Jwt token) {
        Map<String, Object> attributes = token.getClaims();

        if(attributes.containsKey("email")) {
            return Optional.of(attributes.get("email").toString());
        }
        return Optional.empty();
    }
}
