package org.example.vanvooren.service;

import org.example.vanvooren.dto.UserDTO;
import org.example.vanvooren.mapper.UserMapper;
import org.example.vanvooren.model.User;
import org.example.vanvooren.repository.UserRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(User.class,"User with ID " + id + " not found"));
        return UserMapper.toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO saveUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email already in use: " + user.getEmail());
        }
  
        User savedUser = userRepository.save(user);
        return UserMapper.toDTO(savedUser);
    }

    public UserDTO updateUser(Long id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(User.class, "User with ID " + id + " not found"));

        userRepository.findByEmail(user.getEmail())
                .filter(userBdd -> !userBdd.getId().equals(id))
                .ifPresent(userBdd -> {
                    throw new DataIntegrityViolationException("Email already in use: " + user.getEmail());
                });

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toDTO(updatedUser);
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(User.class,"User with ID " + id + " not found"));
        userRepository.deleteById(user.getId());
    }
}
