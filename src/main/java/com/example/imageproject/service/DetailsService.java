package com.example.imageproject.service;

import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.repository.PrivilegeRepository;
import com.example.imageproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = getUserByUsername(username);
        var privileges = privilegeService.findPrivilegesByRole(user.getRole());

        user.setAuthorities(privileges);

        return user;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("There is no user with that name"));
    }

}
