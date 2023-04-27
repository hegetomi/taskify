package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.*;
import com.hegetomi.taskify.dto.JwtTokenDto;
import com.hegetomi.taskify.dto.UserDto;
import com.hegetomi.taskify.dto.manager.TicketAverageSolveTimeDtoImpl;
import com.hegetomi.taskify.dto.manager.TicketSolveRateDtoImpl;
import com.hegetomi.taskify.entity.Ticket;
import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.enums.Priority;
import com.hegetomi.taskify.enums.Status;
import com.hegetomi.taskify.enums.Type;
import com.hegetomi.taskify.enums.UserRole;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import com.hegetomi.taskify.repository.TicketRepository;
import com.hegetomi.taskify.service.TaskifyUserService;
import com.hegetomi.taskify.service.TicketService;
import com.hegetomi.taskify.util.TimeMachine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {"delete from tickets", "delete from user_roles", "delete from taskify_users"})
@Slf4j
class UserControllerIT {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    TaskifyUserService userService;
    @Autowired
    TaskifyUserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TicketService ticketService;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    TimeMachine timeMachine;

    static final String VALID_TITLE = "Lorem ipsu";
    static final String VALID_DESCR = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean m";
    User user;
    User employee;
    User otherEmployee;
    User admin;
    User manager;
    JwtTokenDto managerJwt;
    JwtTokenDto adminJwt;

    @BeforeEach
    void init() {
        user = new User(null, "user", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_USER));
        employee = new User(null, "employee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        otherEmployee = new User(null, "otherEmployee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        admin = new User(null, "admin", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_ADMIN));
        manager = new User(null, "manager", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_MANAGER));
        userRepository.save(user);
        userRepository.save(admin);
        userRepository.save(employee);
        userRepository.save(manager);
        userRepository.save(otherEmployee);
        managerJwt = userService.login(new LoginCommand("manager", "p4ssWord*"));
        adminJwt = userService.login(new LoginCommand("admin", "p4ssWord*"));
    }

    @Test
    void testGetAllUsersNotAdmin() {
        userService.register(new RegisterCommand("name1", "asddsA1*"));
        userService.register(new RegisterCommand("name2", "asddsA1*"));
        userService.register(new RegisterCommand("name3", "asddsA1*"));
        JwtTokenDto token = webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginCommand("name1", "asddsA1*"))
                .exchange()
                .expectBody(JwtTokenDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(token).isNotNull();
        webTestClient.get()
                .uri("/api/user")
                .headers(e -> e.setBearerAuth(token.getJwt()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testGetAllUsersAsAdmin() {
        JwtTokenDto login = userService.login(new LoginCommand("admin", "p4ssWord*"));
        List<UserDto> responseBody = webTestClient.get()
                .uri("/api/user")
                .headers(e -> e.setBearerAuth(login.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBodyList(UserDto.class).returnResult().getResponseBody();
        assertThat(responseBody).hasSize(5);
    }

    @Test
    void testFindMeNotLoggedIn() {
        webTestClient.get()
                .uri("/api/user/me")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testFindMeLoggedIn() {
        userService.register(new RegisterCommand("name1", "asddsA1*"));
        JwtTokenDto token = userService.login(new LoginCommand("name1", "asddsA1*"));
        UserDto responseBody = webTestClient.get()
                .uri("/api/user/me")
                .headers(e -> e.setBearerAuth(token.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBody(UserDto.class).returnResult().getResponseBody();
        assertThat(responseBody).isNotNull().extracting(UserDto::getName).isEqualTo("name1");
    }

    @Test
    void testModifyPassword() {
        userService.register(new RegisterCommand("name1", "asddsA1*"));
        JwtTokenDto token = userService.login(new LoginCommand("name1", "asddsA1*"));
        webTestClient.put().uri("/api/user")
                .headers(h -> h.setBearerAuth(token.getJwt()))
                .bodyValue(new UpdateUserPasswordCommand("asddsA1*", "asddsA2*"))
                .exchange().expectStatus().isOk();
    }

    @Test
    void testModifyPasswordBadOldPass() {
        userService.register(new RegisterCommand("name1", "asddsA1*"));
        JwtTokenDto token = userService.login(new LoginCommand("name1", "asddsA1*"));

        webTestClient.put().uri("/api/user")
                .headers(h -> h.setBearerAuth(token.getJwt()))
                .bodyValue(new UpdateUserPasswordCommand("asddsA5*", "asddsA2*"))
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void testSameUsernameThrowsUserExistsException() {
        userService.register(new RegisterCommand("name1", "asddsA1*"));
        ProblemDetail problemDetail = webTestClient.post().uri("/api/auth/register")
                .bodyValue(new RegisterCommand("name1", "asddsA1*"))
                .exchange().expectStatus().isBadRequest().expectBody(ProblemDetail.class)
                .returnResult().getResponseBody();
        assert problemDetail != null;
        assertThat(problemDetail.getDetail()).isEqualTo("Username is taken");

    }

    @Test
    void testValidJwtDeletedUserThrowsException() {
        UserDto registered = webTestClient.post().uri("/api/auth/register")
                .bodyValue(new RegisterCommand("name1", "asddsA1*"))
                .exchange().expectStatus().isCreated().expectBody(UserDto.class).returnResult().getResponseBody();
        JwtTokenDto token = userService.login(new LoginCommand("name1", "asddsA1*"));
        assert registered != null;
        userRepository.deleteById(registered.getId());
        ProblemDetail responseBody = webTestClient.get()
                .uri("/api/user/me")
                .headers(e -> e.setBearerAuth(token.getJwt()))
                .exchange()
                .expectStatus().isBadRequest().expectBody(ProblemDetail.class).returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getDetail()).isEqualTo("User was not found");
    }

    @Test
    void testAddEmployeeToUserNotAffectsPostedTicket() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto login = userService.login(new LoginCommand("admin", "p4ssWord*"));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(user.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)))
                .headers(h -> h.setBearerAuth(login.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).hasSize(1);
    }

    @Test
    void testAddUserToEmployeeNotAffectsPostedTicket() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto login = userService.login(new LoginCommand("admin", "p4ssWord*"));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)))
                .headers(h -> h.setBearerAuth(login.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).hasSize(1);
    }

    @Test
    void testAddUserToEmployeeNotAffectsAssigned() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto login = userService.login(new LoginCommand("admin", "p4ssWord*"));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)))
                .headers(h -> h.setBearerAuth(login.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getAssigned()).hasSize(1);
    }

    @Test
    void testRevokeUserNotEmployeeDropsPosted() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(user.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of()))
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).isEmpty();
    }

    @Test
    void testRevokeEmployeeNotUserDropsPosted() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of()))
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).isEmpty();
    }

    @Test
    void testRevokeEmployeeIsUserNotDropsPosted() {
        userService.updateUserRights(employee.getId(), new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_USER)))
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).hasSize(1);
    }

    @Test
    void testRevokeUserIsEmployeeNotDropsPosted() {
        userService.updateUserRights(employee.getId(), new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_EMPLOYEE)))
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getSubmitted()).hasSize(1);
    }

    @Test
    void testRevokeEmployeeRemovesAssigned() {
        userService.updateUserRights(employee.getId(), new EditUserRightsCommand(List.of(UserRole.ROLE_USER, UserRole.ROLE_EMPLOYEE)));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, employee, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        UserDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/user/rights/").path(employee.getId() + "").build())
                .bodyValue(new EditUserRightsCommand(List.of(UserRole.ROLE_USER)))
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getAssigned()).isEmpty();
    }

    @Test
    void testGetStatsOpenByEmployee() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.DONE));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        List<TicketSolveRateDtoImpl> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/user/stats")
                        .queryParam("open", true)
                        .build())
                .headers(h -> h.setBearerAuth(managerJwt.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBodyList(TicketSolveRateDtoImpl.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).hasSize(2).extracting(TicketSolveRateDtoImpl::getCount).containsExactly(2, 1);
    }

    @Test
    void testGetStatsClosedByEmployee() {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.DONE));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.DONE));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.DONE));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.DONE));

        List<TicketSolveRateDtoImpl> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/user/stats")
                        .queryParam("open", false)
                        .build())
                .headers(h -> h.setBearerAuth(managerJwt.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBodyList(TicketSolveRateDtoImpl.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).hasSize(2).extracting(TicketSolveRateDtoImpl::getCount).containsExactly(2, 1);
    }

    @Test
    void testGetStatsClosedByEmployeeNoRole() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/user/stats")
                        .queryParam("open", false)
                        .build())
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testGetAverageCloseDays() {
        timeMachine.setTime(LocalDateTime.of(2023, 1, 1, 1, 1));
        Ticket empTicket = new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        Ticket empTicket2 = new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        Ticket otherEmpTicket = new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        Ticket otherEmpTicket2 = new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        Ticket otherEmpTicket3 = new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        EditAssignedTicketCommand editToDone = new EditAssignedTicketCommand(null, null, Priority.LOW, Type.BUG, Status.DONE);

        empTicket.setPostedAt(timeMachine.getTime());
        empTicket2.setPostedAt(timeMachine.getTime());
        otherEmpTicket.setPostedAt(timeMachine.getTime());
        otherEmpTicket2.setPostedAt(timeMachine.getTime());
        otherEmpTicket3.setPostedAt(timeMachine.getTime());
        Ticket save = ticketRepository.save(empTicket);
        Ticket save2 = ticketRepository.save(empTicket2);
        Ticket save3 = ticketRepository.save(otherEmpTicket);
        Ticket save4 = ticketRepository.save(otherEmpTicket2);
        Ticket save5 = ticketRepository.save(otherEmpTicket3);

        timeMachine.setTime(LocalDateTime.of(2023, 1, 10, 1, 1));
        ticketService.editAssignedTicketDetail(save.getId(), editToDone);
        ticketService.editAssignedTicketDetail(save3.getId(), editToDone);

        timeMachine.setTime(LocalDateTime.of(2023, 1, 15, 1, 1));
        ticketService.editAssignedTicketDetail(save2.getId(), editToDone);
        ticketService.editAssignedTicketDetail(save4.getId(), editToDone);

        timeMachine.setTime(LocalDateTime.of(2023, 1, 20, 1, 1));
        ticketService.editAssignedTicketDetail(save5.getId(), editToDone);
        List<TicketAverageSolveTimeDtoImpl> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/user/average")
                        .build())
                .headers(h -> h.setBearerAuth(managerJwt.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBodyList(TicketAverageSolveTimeDtoImpl.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).hasSize(2).extracting(TicketAverageSolveTimeDtoImpl::getDays).containsExactly(14.0, 11.5);
    }

    @Test
    void testGetAverageCloseDaysNoRole() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/user/average")
                        .build())
                .headers(h -> h.setBearerAuth(adminJwt.getJwt()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

}
