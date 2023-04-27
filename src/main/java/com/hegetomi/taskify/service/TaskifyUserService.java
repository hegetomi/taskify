package com.hegetomi.taskify.service;

import com.hegetomi.taskify.command.EditUserRightsCommand;
import com.hegetomi.taskify.command.LoginCommand;
import com.hegetomi.taskify.command.RegisterCommand;
import com.hegetomi.taskify.command.UpdateUserPasswordCommand;
import com.hegetomi.taskify.dto.*;
import com.hegetomi.taskify.dto.manager.TicketAverageSolveTimeDto;
import com.hegetomi.taskify.dto.manager.TicketSolveRateDto;
import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.enums.UserRole;
import com.hegetomi.taskify.exception.InvalidOldPasswordException;
import com.hegetomi.taskify.exception.UserExistsException;
import com.hegetomi.taskify.mapper.UserMapper;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import com.hegetomi.taskify.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaskifyUserService {

    private final TaskifyUserRepository repository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND = "User was not found";

    public JwtTokenDto login(LoginCommand loginCommand) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginCommand.getUsername(), loginCommand.getPassword()));
        JwtTokenDto token = new JwtTokenDto();
        token.setJwt(jwtService.createJwtToken((UserDetails) auth.getPrincipal()));
        return token;
    }

    public RegisterDto register(RegisterCommand registerCommand) {
        User newUser = userMapper.registerCommandToUser(registerCommand);
        if (repository.findByName(newUser.getName()).isPresent()) {
            throw new UserExistsException();
        }
        newUser.getRoles().add(UserRole.ROLE_USER);
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User save = repository.save(newUser);
        return new RegisterDto(save.getId(), save.getName());
    }

    public List<UserDto> findAllUsers() {
        return userMapper.usersToDtosNoTickets(repository.findAll());
    }

    @Transactional
    public UserDto findCurrentUser(String name) {

        User user = repository.findWithSubmittedAndAssignedTickets(name)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(USER_NOT_FOUND));
        return userMapper.userToDto(user);
    }

    @Transactional
    public UserDto updateUserPassword(String name, UpdateUserPasswordCommand updateUserPasswordCommand) {
        User user = repository.findByName(name)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(USER_NOT_FOUND));
        if (passwordEncoder.matches(updateUserPasswordCommand.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(updateUserPasswordCommand.getNewPassword()));
            return userMapper.userToDto(user);
        }
        throw new InvalidOldPasswordException();
    }


    @Transactional
    public UserDto updateUserRights(Long id, EditUserRightsCommand editUserRightsCommand) {
        User user = repository.findWithSubmittedAndAssignedTicketsById(id)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(USER_NOT_FOUND));
        if (!editUserRightsCommand.getUserRoles().contains(UserRole.ROLE_EMPLOYEE)) {
            user.getAssignedTickets().forEach(e -> e.setAssignee(null));
            user.getAssignedTickets().clear();
        }
        if (!(editUserRightsCommand.getUserRoles().contains(UserRole.ROLE_USER)
                || editUserRightsCommand.getUserRoles().contains(UserRole.ROLE_EMPLOYEE))) {
            user.getTickets().forEach(e -> e.setPoster(null));
            user.getTickets().clear();
        }
        user.setRoles(editUserRightsCommand.getUserRoles());

        return userMapper.userToDto(user);
    }

    public List<TicketSolveRateDto> getTicketStatsByUser(boolean open) {
        return open ? repository.getOpenTicketStats()
                : repository.getClosedTicketStats();
    }

    public List<TicketAverageSolveTimeDto> getAverageSolveTimes() {
        return repository.getAverageSolveTimes();
    }
}
