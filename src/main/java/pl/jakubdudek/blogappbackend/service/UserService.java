package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.role.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;
    private final FileService fileService;

    public UserDto getUser(Integer id) {
        return dtoMapper.mapUserToDto(
                userRepository.findById(id).orElseThrow(
                        () -> new EntityNotFoundException("User not found")
                )
        );
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(dtoMapper::mapUserToDto).toList();
    }

    public UserDto editUser(Integer id, User newUser) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        if(isUserPermittedToUser(user)) {
            if(newUser.getName() != null && !newUser.getName().isEmpty()) {
                user.setName(newUser.getName());
            }

            return dtoMapper.mapUserToDto(userRepository.save(user));
        }
        else {
            throw new ForbiddenException("You don't have permission to update this user");
        }
    }

    public String updateProfileImage(Integer id, MultipartFile file) throws IOException {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        if(isUserPermittedToUser(user)) {
            if(!(user.getProfileImage() == null || user.getProfileImage().isEmpty())) {
                fileService.deleteFile(user.getProfileImage());
            }

            String fileName = fileService.uploadFile(file);
            user.setProfileImage(fileName);
            userRepository.save(user);
            return fileName;
        }
        else {
            throw new ForbiddenException("You don't have permission to update this user");
        }
    }

    public void deleteUser(Integer id) throws IOException {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        if(isUserPermittedToUser(user)) {
            userRepository.deleteById(id);

            if(!(user.getProfileImage() == null || user.getProfileImage().isEmpty())) {
                fileService.deleteFile(user.getProfileImage());
            }
        }
        else {
            throw new ForbiddenException("You don't have permission to delete this user");
        }
    }

    private boolean isUserPermittedToUser(User user) {
        User authUser = authenticationManager.getAuthenticatedUser();
        return user.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(authUser.getId());
    }
}
