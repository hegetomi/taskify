package com.hegetomi.taskify.security;

import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskifyUserDetailsService implements UserDetailsService {

    private final TaskifyUserRepository repository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByName(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}