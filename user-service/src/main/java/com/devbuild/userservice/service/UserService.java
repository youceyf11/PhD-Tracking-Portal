package com.devbuild.userservice.service;


import com.devbuild.userservice.entity.User;
import com.devbuild.userservice.enums.Role;
import com.devbuild.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        log.info("Creating a new account for: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateCreation(LocalDate.now());

        if (user.getRole() == null) {
            user.setRole(Role.DOCTORANT);
        }

        if (user.getActive() == null) {
            user.setActive(true);
        }

        User savedUser = userRepository.save(user);
        log.info("Created a new account for: {}", savedUser.getId());

        return savedUser;
    }


    @Transactional( readOnly = true)
    public User getUserEmail(String email){
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User with email not found"+ email));
        }

    @Transactional( readOnly = true)
    public List<User> getAllUsers(){
            return userRepository.findAll();
        }



    public User updateUserRole(Long id, Role newRole){
        log.info("Updating user role for user with id: {} to: {} ", id, newRole);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id not found: " + id));
        user.setRole(newRole);
        return userRepository.save(user);
    }



   public void desactivateUser(Long id){
        log.info("Desactivating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id not found: " + id));
        user.setActive(false);
        userRepository.save(user);
   }

   public void updateLastLogin(String email){
        log.info("Updating last login for user with email: {}", email);

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
   }

}
