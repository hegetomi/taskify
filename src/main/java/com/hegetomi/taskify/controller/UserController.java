package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.EditUserRightsCommand;
import com.hegetomi.taskify.command.UpdateUserPasswordCommand;
import com.hegetomi.taskify.dto.UserDto;
import com.hegetomi.taskify.dto.manager.TicketAverageSolveTimeDto;
import com.hegetomi.taskify.dto.manager.TicketSolveRateDto;
import com.hegetomi.taskify.service.TaskifyUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("http://localhost:4200")
public class UserController {

    private final TaskifyUserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns all users as a list")
    @ApiResponse(responseCode = "200")
    public List<UserDto> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns information about logged in user")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", content = @Content)
    public UserDto findMe(Principal principal) {
        return userService.findCurrentUser(principal.getName());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Modifies logged in user's password")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public UserDto modifyPassword(@RequestBody @Valid UpdateUserPasswordCommand updateUserPasswordCommand,
                                  Principal principal) {
        return userService.updateUserPassword(principal.getName(), updateUserPasswordCommand);
    }

    @PutMapping("/rights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Modifies a user's roles")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "403", content = @Content)
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public UserDto modifyUserRights(@PathVariable Long id, @RequestBody EditUserRightsCommand editUserRightsCommand) {
        return userService.updateUserRights(id, editUserRightsCommand);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves all open/closed tickets by employee based on the reuqest parameter")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "403", content = @Content)
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public List<TicketSolveRateDto> getTicketsStatisticsByUser(@RequestParam(defaultValue = "true") boolean open) {
        return userService.getTicketStatsByUser(open);
    }

    @GetMapping("/average")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves an average ticket solve time by employee")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "403", content = @Content)
    public List<TicketAverageSolveTimeDto> getTicketAverageTimeToSolve() {
        return userService.getAverageSolveTimes();
    }

}
