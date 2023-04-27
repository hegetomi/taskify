package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.*;
import com.hegetomi.taskify.dto.CommentDto;
import com.hegetomi.taskify.dto.JwtTokenDto;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.entity.Ticket;
import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.enums.Priority;
import com.hegetomi.taskify.enums.Status;
import com.hegetomi.taskify.enums.Type;
import com.hegetomi.taskify.enums.UserRole;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import com.hegetomi.taskify.repository.TicketRepository;
import com.hegetomi.taskify.service.CommentService;
import com.hegetomi.taskify.service.TaskifyUserService;
import com.hegetomi.taskify.service.TicketService;
import com.hegetomi.taskify.util.TimeMachine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {"delete from comments", "delete from tickets", "delete from user_roles", "delete from taskify_users"})
@Slf4j
class CommentControllerIT {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    TaskifyUserService userService;
    @Autowired
    TaskifyUserRepository userRepository;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    TicketService ticketService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TimeMachine timeMachine;
    @Autowired
    CommentService commentService;

    User user;
    User otherUser;
    User employee;
    User otherEmployee;
    User admin;
    JwtTokenDto userLoginDto;
    JwtTokenDto employeeLoginDto;
    static final String VALID_TITLE = "Lorem ipsu";
    static final String VALID_DESCR = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean m";



    @BeforeEach
    public void init() {
        user = new User(null, "user", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_USER));
        otherUser = new User(null, "otherUser", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_USER));
        employee = new User(null, "employee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        otherEmployee = new User(null, "otherEmployee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        otherEmployee = new User(null, "admin", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));

        userRepository.save(user);
        userRepository.save(otherUser);
        userRepository.save(employee);
        userRepository.save(otherEmployee);

        userLoginDto = userService.login(new LoginCommand("user", "p4ssWord*"));
        employeeLoginDto = userService.login(new LoginCommand("employee", "p4ssWord*"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "user"})
    void postNewComment(String username) {
        Ticket save = ticketRepository.save(new Ticket(null, VALID_TITLE, VALID_DESCR, timeMachine.getTime(), user, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG,null));
        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        CreateCommentCommand command = new CreateCommentCommand("Hello");

        CommentDto responseBody = webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/forTicket/")
                        .path(save.getId() + "")
                        .build())
                .bodyValue(command)
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isCreated().expectBody(CommentDto.class).returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getCommentDate()).isEqualTo(timeMachine.getTime());
        assertThat(responseBody.getId()).isNotNull();
        assertThat(responseBody.getPosterName()).isEqualTo(username);
        assertThat(responseBody.getValue()).isEqualTo(command.getValue());

        TicketDto ticketDetail = ticketService.getTicketDetail(save.getId());
        assertThat(ticketDetail.getComments()).extracting(CommentDto::getValue).contains(command.getValue());

    }

    @Test
    void postNewCommentInvalidTicket() {
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        CreateCommentCommand command = new CreateCommentCommand("Hello");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/forTicket/")
                        .path("5")
                        .build())
                .bodyValue(command)
                .headers(h -> h.setBearerAuth(userLoginDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void postNewCommentNotLoggedIn() {
        CreateCommentCommand command = new CreateCommentCommand("Hello");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/forTicket/")
                        .path("5")
                        .build())
                .bodyValue(command)
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "user"})
    void postNewCommentNoRole(String username) {
        Ticket save = ticketRepository.save(new Ticket(null, VALID_TITLE, VALID_DESCR, timeMachine.getTime(), otherUser, otherEmployee, null, Priority.HIGH, Type.BUG, Status.BACKLOG,null));
        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        CreateCommentCommand command = new CreateCommentCommand("Hello");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/forTicket/")
                        .path(save.getId() + "")
                        .build())
                .bodyValue(command)
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "user"})
    void deleteExistingComment(String username) {
        Ticket save = ticketRepository.save(new Ticket(null, VALID_TITLE, VALID_DESCR, timeMachine.getTime(), user, employee, null, Priority.HIGH, Type.BUG, Status.BACKLOG,null));
        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        CreateCommentCommand command = new CreateCommentCommand("Hello");
        CommentDto commentDto = commentService.postCommentToTicket(save.getId(), command, username);

        ticketService.getTicketDetail(save.getId());

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/")
                        .path(commentDto.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isOk();


        TicketDto ticketDetail = ticketService.getTicketDetail(save.getId());
        assertThat(ticketDetail.getComments()).isEmpty();

    }

    @Test
    void deleteNotExistingComment() {

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/")
                        .path("5")
                        .build())
                .headers(h -> h.setBearerAuth(userLoginDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();

    }

    @Test
    void deleteNotLoggedIn() {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/")
                        .path("5")
                        .build())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteNoRole() {
        JwtTokenDto otherDto = userService.login(new LoginCommand("admin", "p4ssWord*"));

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/comment/")
                        .path("5")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }
}
