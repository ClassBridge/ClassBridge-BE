package com.linked.classbridge.service;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.security.CustomUserDetails;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .toList();
        userDto.setRoles(roles);

        return new CustomUserDetails(userDto);
    }
}
