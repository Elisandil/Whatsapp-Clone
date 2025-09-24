package com.whatsappclone.services.user;

import com.whatsappclone.entities.user.User;
import com.whatsappclone.mappers.UserMapper;
import com.whatsappclone.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        String userId = token.getSubject();
        Optional<User> optionalUser = userRepository.findUserByPublicId(userId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            updateUserFromToken(user, token.getClaims());
        } else {
            user = userMapper.fromTokenAttributes(token.getClaims());
        }
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    private void updateUserFromToken(User user, Map<String, Object> claims) {

        if (claims.containsKey("given_name")) {
            user.setFirstName(claims.get("given_name").toString());
        }
        if (claims.containsKey("family_name")) {
            user.setLastName(claims.get("family_name").toString());
        }
        if (claims.containsKey("email")) {
            user.setEmail(claims.get("email").toString());
        }
    }
}
