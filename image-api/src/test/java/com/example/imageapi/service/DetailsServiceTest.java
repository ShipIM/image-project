package com.example.imageapi.service;

import com.example.imageapi.config.BaseTest;
import com.example.imageapi.exception.EntityNotFoundException;
import com.example.imageapi.model.entity.User;
import com.example.imageapi.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DetailsServiceTest extends BaseTest {

    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DetailsService detailsService;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void loadUserByUsername_UserExists() {
        var username = "admin";
        var role = roleService.getRoleByName("ROLE_ADMIN");
        var privileges = privilegeService.findPrivilegesByRoleId(role.getId());

        var details = detailsService.loadUserByUsername(username);

        Assertions.assertAll(
                () -> Assertions.assertEquals(username, details.getUsername()),
                () -> Assertions.assertEquals(privileges, details.getAuthorities())
        );
    }

    @Test
    public void loadUserByUsername_UserDoesNotExist() {
        var username = "nonExistentUser";

        Assertions.assertThrows(EntityNotFoundException.class, () -> detailsService.loadUserByUsername(username));
    }

    @Test
    @Transactional
    public void createUser() {
        var user = new User();
        user.setUsername("user");
        user.setPassword("password");

        var createdUser = detailsService.createUser(user);
        user.setId(createdUser.getId());

        Assertions.assertAll(
                () -> Assertions.assertTrue(userRepository.existsById(createdUser.getId())),
                () -> Assertions.assertEquals(user, userRepository.findById(user.getId())
                        .orElseThrow(() -> new EntityNotFoundException("There is no user with that id")))
        );
    }

}
