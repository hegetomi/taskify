package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.LoginCommand;
import com.hegetomi.taskify.command.RegisterCommand;
import com.hegetomi.taskify.dto.JwtTokenDto;
import com.hegetomi.taskify.dto.RegisterDto;
import com.hegetomi.taskify.service.TaskifyUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class AuthController {

    private final TaskifyUserService userService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "User attempt to login with provided credentials")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public JwtTokenDto login(@RequestBody @Valid LoginCommand loginCommand) {
        return userService.login(loginCommand);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registration attempt with provided information")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public RegisterDto register(@RequestBody @Valid RegisterCommand registerCommand) {
        return userService.register(registerCommand);
    }

}
