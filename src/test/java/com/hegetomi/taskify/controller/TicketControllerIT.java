package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.EditAssignedTicketCommand;
import com.hegetomi.taskify.command.EditSubmittedTicketCommand;
import com.hegetomi.taskify.command.LoginCommand;
import com.hegetomi.taskify.command.NewTicketCommand;
import com.hegetomi.taskify.dto.JwtTokenDto;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.dto.TicketHistoryDto;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {"delete from comments", "delete from tickets", "delete from user_roles", "delete from taskify_users"})
@Slf4j
class TicketControllerIT {

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
    TicketController ticketController;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TimeMachine timeMachine;
    User user;
    User otherUser;
    User employee;
    User otherEmployee;
    User manager;
    User admin;
    Map<String, User> usersMap;
    JwtTokenDto userLoginDto;
    JwtTokenDto employeeLoginDto;
    static final String LONG_TITLE = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo lig";
    static final String VALID_TITLE = "Lorem ipsu";
    static final String VALID_TITLE_EDIT = "Lorem ipsum";
    static final String LONG_DESCR = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium q";
    static final String VALID_DESCR = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean m";
    static final String VALID_DESCR_EDIT = "adipiscing elit. Aenean commodo ligula eget dolor. Aenean m";

    NewTicketCommand ticketCommandOne;
    EditSubmittedTicketCommand editCommand;
    EditAssignedTicketCommand editAssignedCommand;

    @BeforeEach
    public void init() {
        user = new User(null, "user", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_USER));
        otherUser = new User(null, "otherUser", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_USER));
        employee = new User(null, "employee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        otherEmployee = new User(null, "otherEmployee", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_EMPLOYEE));
        manager = new User(null, "manager", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_MANAGER));
        admin = new User(null, "admin", passwordEncoder.encode("p4ssWord*"), List.of(UserRole.ROLE_ADMIN));
        userRepository.save(user);
        userRepository.save(otherUser);
        userRepository.save(employee);
        userRepository.save(otherEmployee);
        userRepository.save(manager);
        userRepository.save(admin);

        ticketCommandOne = new NewTicketCommand(VALID_TITLE,
                VALID_DESCR, Priority.HIGH, Type.BUG);
        editCommand = new EditSubmittedTicketCommand(VALID_TITLE_EDIT, VALID_DESCR_EDIT, Priority.HIGH, Type.CHANGE);

        editAssignedCommand = new EditAssignedTicketCommand(VALID_TITLE_EDIT, VALID_DESCR_EDIT, Priority.MEDIUM, Type.CHANGE, Status.DOING);
        usersMap = new HashMap<>();
        usersMap.put("user", user);
        usersMap.put("employee", employee);

        userLoginDto = userService.login(new LoginCommand("user", "p4ssWord*"));
        employeeLoginDto = userService.login(new LoginCommand("employee", "p4ssWord*"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testUserCreateValidTicket(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        TicketDto responseBody = webTestClient.post().uri("/api/ticket").headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(ticketCommandOne).exchange().expectStatus().isCreated()
                .expectBody(TicketDto.class).returnResult().getResponseBody();
        assert responseBody != null;
        assertThat(responseBody.getId()).isNotNull();
        assertThat(responseBody.getTitle()).isEqualTo(VALID_TITLE);
        assertThat(responseBody.getPostedAt()).isEqualTo(timeMachine.getTime());
        assertThat(responseBody.getStatus()).isEqualTo(Status.BACKLOG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "admin"})
    void testRoleCannotPostTicket(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.post().uri("/api/ticket").headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(ticketCommandOne).exchange().expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testUserCreateInvalidTicketTitle(String username) {
        NewTicketCommand ticketCommand = new NewTicketCommand(LONG_TITLE,
                "description", Priority.HIGH, Type.BUG);
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.post().uri("/api/ticket").headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(ticketCommand).exchange().expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testUserCreateInvalidTicketDescription(String username) {
        NewTicketCommand ticketCommand = new NewTicketCommand("title",
                LONG_DESCR, Priority.HIGH, Type.BUG);
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.post().uri("/api/ticket").headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(ticketCommand).exchange().expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class).returnResult().getResponseBody();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testGetSubmittedTicketsNotDone(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save2 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.DONE));

        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        List<TicketDto> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket")
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectBodyList(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).containsAll(List.of(save.getId(), save2.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testGetSubmittedTicketsFull(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save2 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket done = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.DONE));

        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        List<TicketDto> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket").queryParam("full", true)
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectBodyList(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).containsAll(List.of(save.getId(), save2.getId(), done.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testGetSubmittedTicketsInvalidParam(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket").queryParam("full", "asdasdad")
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "manager"})
    void testGetSubmittedTicketsNoRole(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket")
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isForbidden();
    }

    @Test
    void testGetSubmittedTicketsNoLogin() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket")
                        .build())
                .exchange().expectStatus().isForbidden();
    }


    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testGetSubmittedTicketDetail(String username) {
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        Ticket ticket = new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(username), null, Priority.HIGH, Type.BUG, Status.BACKLOG);
        ticket.setPostedAt(timeMachine.getTime());
        Ticket save = ticketRepository.save(ticket);
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        TicketDto responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/" + save.getId())
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).isEqualTo(save.getId());
        assertThat(responseBody).extracting(e -> e.getPostedAt().getDayOfMonth()).isEqualTo(28);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testGetSubmittedTicketDetailNotExistingForbidden(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/5")
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testOtherUserTicketForbidden(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, otherUser, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/").path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "admin"})
    void testNoTicketDetailForRoles(String username) {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, otherUser, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }


    @ParameterizedTest
    @ValueSource(strings = {"user", "employee", "admin", "manager"})
    void testPosterIsNull(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/").path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testEditSubmittedTicketDetail(String name) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, usersMap.get(name), null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand(name, "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/").path(save.getId() + "").build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isOk();
        TicketDto responseBody = ticketService.getTicketDetail(save.getId());

        assertThat(responseBody.getId()).isEqualTo(save.getId());
        assertThat(responseBody.getTitle()).isEqualTo(editCommand.getTitle());
        assertThat(responseBody.getDescription()).isEqualTo(editCommand.getDescription());
        assertThat(responseBody.getPriority()).isEqualTo(editCommand.getPriority());
        assertThat(responseBody.getType()).isEqualTo(editCommand.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testEditOtherUsersSubmittedTicketDetail(String name) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, otherUser, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto dto = userService.login(new LoginCommand(name, "p4ssWord*"));
        webTestClient.put().uri("/api/ticket/" + save.getId()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isForbidden();

    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testEditNotFoundSubmittedTicketDetailIsForbidden(String name) {
        JwtTokenDto dto = userService.login(new LoginCommand(name, "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/").path("1").build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isForbidden();

    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "employee"})
    void testEditNullTicketSubmittedTicketDetail(String name) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto dto = userService.login(new LoginCommand(name, "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/").path(save.getId() + "").build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isForbidden();
    }

    @Test
    void testAssignTicketAnon() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand("employee", "p4ssWord*"));
        TicketDto responseBody = webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assign/").path(save.getId() + "").queryParam("user", employee.getId()).build())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange()
                .expectStatus().isOk()
                .expectBody(TicketDto.class).returnResult().getResponseBody();
        assertThat(responseBody).extracting(e -> e.getAssignee().getName()).isEqualTo("employee");
    }

    @Test
    void testAssignTicketAnonToAnotherEmployee() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand("employee", "p4ssWord*"));
        TicketDto responseBody = webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assign/").path(save.getId() + "").queryParam("user", otherEmployee.getId()).build())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange()
                .expectStatus().isOk()
                .expectBody(TicketDto.class).returnResult().getResponseBody();
        assertThat(responseBody).extracting(e -> e.getAssignee().getName()).isEqualTo("otherEmployee");
    }

    @Test
    void testAssignTicketNotAnon() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand("employee", "p4ssWord*"));
        TicketDto responseBody = webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assign/").path(save.getId() + "").queryParam("user", employee.getId()).build())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange()
                .expectStatus().isOk()
                .expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(e -> e.getAssignee().getName()).isEqualTo("employee");
    }

    @Test
    void testAssignTicketNotAnonToAnotherEmployee() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand("employee", "p4ssWord*"));
        TicketDto responseBody = webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/assign/")
                        .path(save.getId() + "").queryParam("user", otherEmployee.getId()).build())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange()
                .expectStatus().isOk()
                .expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(e -> e.getAssignee().getName()).isEqualTo("otherEmployee");
    }

    @Test
    void testAssignFailure() {
        Ticket saved = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        log.warn(saved.getAssignee().getName());
        JwtTokenDto dto = userService.login(new LoginCommand("otherEmployee", "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assign/").path(saved.getId() + "")
                        .queryParam("user", otherEmployee.getId()).build())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange().expectStatus().isForbidden();
    }

    @Test
    void testAssignTicketNotFoundTicketId() {
        JwtTokenDto dto = userService.login(new LoginCommand("employee", "p4ssWord*"));
        webTestClient.put().uri("/api/ticket/assign/" + 5 + "?user=" + employee.getId())
                .headers(h -> h.setBearerAuth(dto.getJwt())).exchange().expectStatus().isForbidden();
    }

    @Test
    void testGetAssignedTicketsOnlyNotDone() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save2 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket done1 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.DONE));
        Ticket done2 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.DONE));

        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        ticketService.assignToUser(save.getId(), employee.getId());
        ticketService.assignToUser(save2.getId(), employee.getId());
        ticketService.assignToUser(done1.getId(), employee.getId());
        ticketService.assignToUser(done2.getId(), employee.getId());

        List<TicketDto> responseBody = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned")
                        .build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isOk().expectBodyList(TicketDto.class).returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).containsAll(List.of(save.getId(), save2.getId()));
    }

    @Test
    void testGetAssignedTicketsGetAll() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save2 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save3 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, null, Priority.HIGH, Type.BUG, Status.DONE));

        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        ticketService.assignToUser(save.getId(), employee.getId());
        ticketService.assignToUser(save2.getId(), employee.getId());
        ticketService.assignToUser(save3.getId(), employee.getId());

        List<TicketDto> responseBody = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned")
                        .queryParam("full", "true").build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isOk().expectBodyList(TicketDto.class).returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).containsExactly(save.getId(), save2.getId(), save3.getId());
    }

    @Test
    void testGetAssignedTicketsGetAllInvalidParam() {
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned")
                        .queryParam("full", "asdasdad").build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void testGetAssignedTicketsFailWithoutRole(String username) {
        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get().uri("/api/ticket/assigned").headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange().expectStatus().isForbidden();
    }


    @Test
    void testEditAssignedTicketDetail() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        TicketDto responseBody = webTestClient.put().uri("/api/ticket/assigned/" + save.getId())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editAssignedCommand).exchange().expectStatus().isOk().expectBody(TicketDto.class)
                .returnResult().getResponseBody();

        assert responseBody != null;
        assertThat(responseBody.getId()).isEqualTo(save.getId());
        assertThat(responseBody.getTitle()).isEqualTo(editAssignedCommand.getTitle());
        assertThat(responseBody.getDescription()).isEqualTo(editAssignedCommand.getDescription());
        assertThat(responseBody.getPriority()).isEqualTo(editAssignedCommand.getPriority());
        assertThat(responseBody.getType()).isEqualTo(editAssignedCommand.getType());
    }

    @Test
    void testEditAssignedTicketDoneSetsClosureDate() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        editAssignedCommand.setStatus(Status.DONE);
        timeMachine.setTime(LocalDateTime.of(1993, 2, 1, 1, 1));
        TicketDto responseBody = webTestClient.put().uri("/api/ticket/assigned/" + save.getId())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editAssignedCommand).exchange().expectStatus().isOk().expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getClosedAt).isEqualTo(timeMachine.getTime());
    }

    @Test
    void testEditAssignedTicketUnDoneResetsClosureDate() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        editAssignedCommand.setStatus(Status.DONE);
        timeMachine.setTime(LocalDateTime.of(1993, 2, 1, 1, 1));
        ticketService.editAssignedTicketDetail(save.getId(), editAssignedCommand);
        editAssignedCommand.setStatus(Status.TESTING);
        TicketDto responseBody = webTestClient.put().uri("/api/ticket/assigned/" + save.getId())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editAssignedCommand).exchange().expectStatus().isOk().expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getClosedAt).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "manager"})
    void testEditAssignedTicketDetailForbidden(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto dto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "").build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editAssignedCommand).exchange().expectStatus().isForbidden();
    }

    @Test
    void testEditOtherUsersAssignedTicketDetail() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto dto = userService.login(new LoginCommand(otherEmployee.getName(), "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "").build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editAssignedCommand).exchange().expectStatus().isForbidden();

    }

    @Test
    void testEditNotFoundAssignedTicketDetail() {
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path("1").build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isForbidden();
    }

    @Test
    void testEditNullAssigneeTicketDetail() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "").build()).headers(h -> h.setBearerAuth(dto.getJwt()))
                .bodyValue(editCommand).exchange().expectStatus().isForbidden();

    }

    @Test
    void testGetAssignedTicketDetail() {
        timeMachine.setTime(LocalDateTime.of(1993, 2, 28, 1, 1));
        Ticket ticket = new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG);
        ticket.setPostedAt(timeMachine.getTime());
        Ticket save = ticketRepository.save(ticket);

        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        TicketDto responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "").build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectBody(TicketDto.class)
                .returnResult().getResponseBody();
        assertThat(responseBody).extracting(TicketDto::getId).isEqualTo(save.getId());
        assertThat(responseBody).extracting(e -> e.getPostedAt().getDayOfMonth()).isEqualTo(28);
    }

    @Test
    void testGetAssignedTicketDetailNotExistingForbidden() {
        JwtTokenDto dto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path("5")
                        .build())
                .headers(h -> h.setBearerAuth(dto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void testOtherUserAssignedTicketForbidden() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, user, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(employee.getName(), "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"manager", "admin", "user"})
    void testNoAssignedTicketDetailForRoles(String username) {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, otherUser, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/assigned/").path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }


    @ParameterizedTest
    @ValueSource(strings = {"user", "admin"})
    void testUnassignedNoRole(String username) {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/unassigned")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "manager"})
    void testGetUnassigned(String username) {
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, otherEmployee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save3 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));
        Ticket save4 = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, null, Priority.HIGH, Type.BUG, Status.BACKLOG));

        JwtTokenDto otherDto = userService.login(new LoginCommand(username, "p4ssWord*"));
        List<TicketDto> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/unassigned")
                        .build())
                .headers(h -> h.setBearerAuth(otherDto.getJwt()))
                .exchange()
                .expectStatus().isOk().expectBodyList(TicketDto.class).returnResult().getResponseBody();

        assertThat(responseBody).hasSize(2).extracting(TicketDto::getId).containsExactly(save3.getId(), save4.getId());
    }

    @Test
    void testTicketHistoryById() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        save.setPriority(Priority.LOW);
        ticketService.editAssignedTicketDetail(save.getId(), new EditAssignedTicketCommand(VALID_TITLE, VALID_DESCR, Priority.LOW, Type.BUG, Status.BACKLOG));
        ticketService.editAssignedTicketDetail(save.getId(), new EditAssignedTicketCommand(VALID_TITLE, VALID_DESCR, Priority.MEDIUM, Type.BUG, Status.BACKLOG));

        List<TicketHistoryDto> responseBody = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/history/")
                        .path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(employeeLoginDto.getJwt()))
                .exchange()
                .expectStatus()
                .isOk().expectBodyList(TicketHistoryDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).hasSize(3).extracting(e -> e.getTicketDto().getPriority())
                .containsExactlyInAnyOrder(Priority.HIGH, Priority.LOW, Priority.MEDIUM);
    }

    @Test
    void testTicketHistoryNoRole() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/history/")
                        .path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(userLoginDto.getJwt()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testTicketHistoryNotAssigned() {
        Ticket save = ticketRepository.save(new Ticket(VALID_TITLE, VALID_DESCR, null, employee, Priority.HIGH, Type.BUG, Status.BACKLOG));
        JwtTokenDto login = userService.login(new LoginCommand("otherEmployee", "p4ssWord*"));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket/history/")
                        .path(save.getId() + "")
                        .build())
                .headers(h -> h.setBearerAuth(login.getJwt()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }


}
