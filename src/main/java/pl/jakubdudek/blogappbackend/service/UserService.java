package pl.jakubdudek.blogappbackend.service;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.jakubdudek.blogappbackend.exception.ForbiddenException;
import pl.jakubdudek.blogappbackend.model.dto.request.UserUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.IUserSummaryDto;
import pl.jakubdudek.blogappbackend.util.mapper.DtoMapper;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.repository.UserRepository;
import pl.jakubdudek.blogappbackend.util.jwt.JwtAuthenticationManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtAuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;
    private final FileService fileService;

    public UserDto getUser(Integer id) {
        return dtoMapper.mapUserToDto(findUserById(id));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(dtoMapper::mapUserToDto).toList();
    }

    public UserDto editUser(Integer id, UserUpdateRequest request) {
        User user = findUserById(id);
        requirePermissionToUser(user);

        user.setName(Optional.of(request.getName()).orElse(user.getName()));
        user.setBio(request.getBio());

        return dtoMapper.mapUserToDto(userRepository.save(user));
    }

    @Transactional
    public void updateRole(Integer id, UserRole role) {
        int modified = userRepository.updateUserRole(id, role);
        if(modified == 0) {
            throw new EntityNotFoundException("User not found");
        }
    }

    public String updateProfileImage(Integer id, MultipartFile file) throws IOException {
        User user = findUserById(id);
        requirePermissionToUser(user);

        if(StringUtils.isEmpty(user.getProfileImage())) {
            fileService.deleteFile(user.getProfileImage());
        }

        String fileName = fileService.uploadFile(file);
        user.setProfileImage(fileName);
        userRepository.save(user);
        return fileName;
    }

    public void removeUserProfileImage(Integer id) throws IOException {
        User user = findUserById(id);
        requirePermissionToUser(user);

        fileService.deleteFile(user.getProfileImage());
        user.setProfileImage(null);
        userRepository.save(user);
    }

    public void deleteUser(Integer id) throws IOException {
        User user = findUserById(id);
        requirePermissionToUser(user);

        userRepository.deleteById(id);

        if(!StringUtils.isEmpty(user.getProfileImage())) {
            fileService.deleteFile(user.getProfileImage());
        }
    }

    private User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void requirePermissionToUser(User user) {
        User authUser = authenticationManager.getAuthenticatedUser();

        if(authUser == null || !(authUser.getRole() == UserRole.ROLE_ADMIN || user.getId().equals(authUser.getId()))) {
            throw new ForbiddenException("You don't have permission to this user");
        }
    }
}
