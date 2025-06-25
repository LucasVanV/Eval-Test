package org.example.vanvooren.service;

import org.example.vanvooren.dto.UserDTO;
import org.example.vanvooren.model.User;
import org.example.vanvooren.repository.UserRepository;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Récupérer un utilisateur existant par ID")
    void testGetUserById_Success() {
        User user = new User(1L, "John Doe", "john@example.com", "password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO userDTO = userService.getUserById(1L);

        assertAll("Récupérer un utilisateur existant",
            () -> assertNotNull(userDTO),
            () -> assertEquals("John Doe", userDTO.getName()),
            () -> verify(userRepository, times(1)).findById(1L)
        );
    }

    @Test
    @DisplayName("Lancer ObjectNotFoundException si utilisateur introuvable")
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertAll("Lancer ObjectNotFoundException si utilisateur introuvable",
            () -> assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(1L)),
            () -> verify(userRepository, times(1)).findById(1L)
        );
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs")
    void testGetAllUsers() {
        List<User> users = Arrays.asList(
                new User(1L, "John", "john@example.com", "pass1"),
                new User(2L, "Jane", "jane@example.com", "pass2")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> userDTOs = userService.getAllUsers();

        assertAll("Récupérer tous les utilisateurs", 
            () -> assertEquals(2, userDTOs.size()),
            () -> verify(userRepository, times(1)).findAll()
        );
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs, mais aucun dans la BDD")
    void testGetAllUsers_emptyBDD() {
        List<User> users = Arrays.asList();
        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> userDTOs = userService.getAllUsers();

        assertAll("Récupérer tous les utilisateurs", 
            () -> assertEquals(0, userDTOs.size()),
            () -> verify(userRepository, times(1)).findAll()
        );
    }

    @Test
    @DisplayName("Créer un utilisateur avec succès")
    void testCreateUser_Success() {
        User user = new User(null, "John", "john@example.com", "pass");
        User savedUser = new User(1L, "John", "john@example.com", "pass");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        //when(userRepository.save(any(User.class))).thenReturn(savedUser);
        Mockito.doReturn(savedUser).when(userRepository).save(any());

        UserDTO result = userService.saveUser(user);

        assertAll("Créer un utilisateur avec succès",
            () -> assertNotNull(result),
            () -> assertEquals("John", result.getName()),
            () -> verify(userRepository, times(1)).findByEmail(user.getEmail()),                              
            () -> verify(userRepository, times(1)).save(any(User.class))                             
        );
    }

    @Test
    @DisplayName("Échec création utilisateur si email déjà utilisé")
    void testCreateUser_EmailAlreadyExists() {
        User user = new User(null, "John", "john@example.com", "pass");
        User existingUser = new User(1L, "Jane", "john@example.com", "pass2");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(existingUser));

        assertAll("Échec création utilisateur si email déjà utilisé",
            () -> assertThrows(DataIntegrityViolationException.class, () -> userService.saveUser(user)),
            () -> verify(userRepository, times(1)).findByEmail(user.getEmail()),
            () -> verify(userRepository, never()).save(any(User.class))
        );
    }

    @Test
    @DisplayName("Échec création utilisateur si mdp trop court")
    void testCreateUser_ShortPassword() {
        User user = new User(null, "John", "john@example.com", "pa");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Mockito.doThrow(TransactionSystemException.class).when(userRepository).save(any());

        assertAll("Échec création utilisateur si mot de passe trop court",
            () -> assertThrows(TransactionSystemException.class, () -> userService.saveUser(user)),
            () -> verify(userRepository, times(1)).findByEmail(user.getEmail()),
            () -> verify(userRepository, times(1)).save(any(User.class))
        );
    }

    @Test
    @DisplayName("Échec création utilisateur si le nom est vide")
    void testCreateUser_emptyName() {
        User user = new User(null, " ", "john@example.com", "pass");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Mockito.doThrow(TransactionSystemException.class).when(userRepository).save(any());

        assertAll("Échec création utilisateur si le nom est vide",
            () -> assertThrows(TransactionSystemException.class, () -> userService.saveUser(user)),
            () -> verify(userRepository, times(1)).findByEmail(user.getEmail()),
            () -> verify(userRepository, times(1)).save(any(User.class))
        );
    }

    @Test
    @DisplayName("Échec création utilisateur si email non valide")
    void testCreateUser_EmailNotValid() {
        User user = new User(null, "John", "john@example", "pass");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Mockito.doThrow(TransactionSystemException.class).when(userRepository).save(any());


        assertAll("Échec création utilisateur si mot de passe trop court",
            () -> assertThrows(TransactionSystemException.class, () -> userService.saveUser(user)),
            () -> verify(userRepository, times(1)).findByEmail(user.getEmail()),
            () -> verify(userRepository, times(1)).save(any(User.class))
        );
    }

    @Test
    @DisplayName("Mettre à jour un utilisateur existant avec succès")
    void testUpdateUser_Success() {
        User existingUser = new User(1L, "Old Name", "old@example.com", "pass");
        User update = new User(1L, "New Name", "new@example.com", "newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(update.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDTO updatedUser = userService.updateUser(1L, update);

        assertAll("Mettre à jour un utilisateur existant avec succès",
            () -> assertNotNull(updatedUser),
            () -> assertEquals("New Name", updatedUser.getName()),
            () -> assertEquals("new@example.com", updatedUser.getEmail()),
            () -> verify(userRepository, times(1)).findById(1L),
            () -> verify(userRepository, times(1)).findByEmail(update.getEmail()),
            () -> verify(userRepository, times(1)).save(existingUser)
        );
    }

    @Test
    @DisplayName("Échec mise à jour utilisateur si email déjà utilisé")
    void testUpdateUser_EmailAlreadyExists() {
        User existingUser = new User(1L, "Old Name", "old@example.com", "pass");
        User update = new User(1L, "New Name", "used@example.com", "newpass");
        User anotherUser = new User(2L, "Someone", "used@example.com", "pass2");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(update.getEmail())).thenReturn(Optional.of(anotherUser));

        assertAll("Échec mise à jour utilisateur si email déjà utilisé", 
            () -> assertThrows(DataIntegrityViolationException.class, () -> userService.updateUser(1L, update)),
            () -> verify(userRepository, times(1)).findById(1L),
            () -> verify(userRepository, times(1)).findByEmail(update.getEmail()),
            () -> verify(userRepository, never()).save(any(User.class))
        );
    }

    @Test
    @DisplayName("Supprimer un utilisateur existant avec succès")
    void testDeleteUser_Success() {
        User user = new User(1L, "John", "john@example.com", "pass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUserById(1L);

        assertAll("Supprimer un utilisateur existant avec succès",
            () -> verify(userRepository, times(1)).findById(1L),
            () -> verify(userRepository, times(1)).deleteById(1L)
        );
    }

    @Test
    @DisplayName("Échec suppression utilisateur si introuvable")
    void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertAll("Échec suppression utilisateur si introuvable",
            () -> assertThrows(ObjectNotFoundException.class, () -> userService.deleteUserById(1L)),
            () -> verify(userRepository, times(1)).findById(1L),
            () -> verify(userRepository, never()).deleteById(anyLong())
        );
        
    }
}
