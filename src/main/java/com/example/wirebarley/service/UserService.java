package com.example.wirebarley.service;

import com.example.wirebarley.domain.User;
import com.example.wirebarley.dto.CreateAccountRequestDTO;
import com.example.wirebarley.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(CreateAccountRequestDTO requestDTO) {
        final String phone = requestDTO.getPhone();
        final String email = requestDTO.getEmail();
        final String username = requestDTO.getUsername();
        Optional<User> oUser = userRepository.findByPhone(phone);

        return oUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setUsername(username);
            return userRepository.save(newUser);
        });
    }
}
