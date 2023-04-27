package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.LoginCommand;
import com.hegetomi.taskify.command.RegisterCommand;
import com.hegetomi.taskify.dto.JwtTokenDto;
import com.hegetomi.taskify.dto.UserDto;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import com.hegetomi.taskify.service.TaskifyUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(statements = {"delete from comments","delete from tickets", "delete from user_roles", "delete from taskify_users"})
class AuthControllerIT {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    TaskifyUserService userService;

    @Test
    void testRegisterShortUsername() {
        RegisterCommand registerCommand = new RegisterCommand("ba", "pA55word*");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterLongUsername() {
        RegisterCommand registerCommand = new RegisterCommand("twentyusernamecharacter", "pA55word*");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterNoLowercasePassword() {
        RegisterCommand registerCommand = new RegisterCommand("username", "PA55WORD*");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterNoUppercasePassword() {
        RegisterCommand registerCommand = new RegisterCommand("username", "pa55word*");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterNoDigit() {
        RegisterCommand registerCommand = new RegisterCommand("username", "password*");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterNoSpecial() {
        RegisterCommand registerCommand = new RegisterCommand("username", "pa55word5");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterLongPassword() {
        RegisterCommand registerCommand = new RegisterCommand("username", "pa55word5*****");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterShortPassword() {
        RegisterCommand registerCommand = new RegisterCommand("username", "pP*5");

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testRegisterValidCommand() {
        RegisterCommand registerCommand = new RegisterCommand("username", "pA55word*");

        UserDto responseBody = webTestClient.post().uri("/api/auth/register")
                .bodyValue(registerCommand)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UserDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(responseBody)
                .extracting(UserDto::getName)
                .isEqualTo("username");
    }

    @Test
    void testSuccessfulLogin() {
        userService.register(new RegisterCommand("username","pass*5Aa"));
        JwtTokenDto tokenDto = webTestClient.post().uri("/api/auth/login")
                .bodyValue(LoginCommand.builder().username("username").password("pass*5Aa").build()).exchange()
                .expectStatus().isOk()
                .expectBody(JwtTokenDto.class).returnResult().getResponseBody();

        assertThat(tokenDto).isNotNull().extracting(JwtTokenDto::getJwt).isNotNull();
    }

    @Test
    void testFailedLogin() {
        ProblemDetail problemDetail = webTestClient.post().uri("/api/auth/login")
                .bodyValue(LoginCommand.builder().username("username").password("pass*5Aa").build()).exchange()
                .expectStatus().isBadRequest().expectBody(ProblemDetail.class)
                .returnResult().getResponseBody();

        assertThat(problemDetail).isNotNull().extracting(ProblemDetail::getDetail).isEqualTo("Bad credentials");
    }

}
