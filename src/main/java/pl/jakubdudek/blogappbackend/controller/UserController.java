package pl.jakubdudek.blogappbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.jakubdudek.blogappbackend.model.dto.request.UserUpdateRequest;
import pl.jakubdudek.blogappbackend.model.dto.response.UserDto;
import pl.jakubdudek.blogappbackend.model.dto.response.IUserDto;
import pl.jakubdudek.blogappbackend.model.entity.User;
import pl.jakubdudek.blogappbackend.model.enumerate.UserRole;
import pl.jakubdudek.blogappbackend.service.UserService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping
    public ResponseEntity<List<IUserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> editUser(@PathVariable Integer id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.editUser(id, request));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateRole(@PathVariable Integer id, @RequestBody UserRole role) {
        userService.updateRole(id, role);
        return ResponseEntity.ok("User: "+id+" has now role: "+role.getAuthority());
    }

    @PutMapping("/{id}/profile-image")
    public ResponseEntity<String> updateProfileImage(@PathVariable Integer id, @RequestParam("image") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userService.updateProfileImage(id, file));
    }

    @DeleteMapping("/{id}/profile-image")
    public ResponseEntity<String> removeUserProfileImage(@PathVariable Integer id) throws IOException {
        userService.removeUserProfileImage(id);
        return ResponseEntity.ok("Successfully deleted profile image");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) throws IOException {
        userService.deleteUser(id);
        return ResponseEntity.ok("Successfully deleted user: "+id);
    }
}
