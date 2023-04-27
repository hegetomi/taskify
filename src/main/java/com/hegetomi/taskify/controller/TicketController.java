package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.EditAssignedTicketCommand;
import com.hegetomi.taskify.command.EditSubmittedTicketCommand;
import com.hegetomi.taskify.command.NewTicketCommand;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.dto.TicketHistoryDto;
import com.hegetomi.taskify.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a new ticket if role is employee or user")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto createNewTicket(@RequestBody @Valid NewTicketCommand ticketCommand,
                                     Principal principal) {
        return ticketService.createNewTicket(ticketCommand, principal);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns a user's submitted tickets as a list")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", content = @Content)
    public List<TicketDto> getMySubmissions(Principal principal, @RequestParam(defaultValue = "false") boolean full) {
        return ticketService.getSubmittedTickets(principal.getName(), full);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_USER') && @ticketOwnershipService.isPostedByUser(#id,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns a user's submitted ticket by id")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto getMySubmissionById(Principal principal, @PathVariable Long id) {
        return ticketService.getTicketDetail(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE','ROLE_USER') && @ticketOwnershipService.isPostedByUser(#id,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Edits a user's submitted ticket by id")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto editMySubmission(@PathVariable Long id,
                                      @RequestBody EditSubmittedTicketCommand ticketCommand) {
        return ticketService.editSubmittedTicketDetail(id, ticketCommand);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns a user's assigned tickets as a list")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", content = @Content)
    public List<TicketDto> getAssignedTickets(Principal principal, @RequestParam(defaultValue = "false") boolean full) {
        return ticketService.getAssignedTickets(principal.getName(),full);
    }

    @GetMapping("/assigned/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE') && @ticketOwnershipService.isAssignedToUser(#id,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns a user's assigned ticket by id")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto getAssignedTicketById(Principal principal, @PathVariable Long id) {
        return ticketService.getTicketDetail(id);
    }

    @PutMapping("/assigned/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE') && @ticketOwnershipService.isAssignedToUser(#id,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Edits a user's assigned ticket by id")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto editAssignedTicket(
            @PathVariable Long id,
            @RequestBody EditAssignedTicketCommand ticketCommand) {
        return ticketService.editAssignedTicketDetail(id, ticketCommand);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns unassigned tickets as a list")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "403", content = @Content)
    public List<TicketDto> getUnassignedTickets() {
        return ticketService.getUnassignedTickets();
    }

    @PutMapping("/assign/{ticketId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE') && @ticketOwnershipService.isAvailableToAssign(#ticketId,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Assigns a ticket to a user")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public TicketDto assignToUser(@PathVariable Long ticketId, @RequestParam Long user, Principal principal) {
        return ticketService.assignToUser(ticketId, user);
    }


    @GetMapping("/history/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE') && @ticketOwnershipService.isAssignedToUser(#id,principal)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns every changed state of a ticket by id")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    @ApiResponse(responseCode = "403", content = @Content)
    public List<TicketHistoryDto> findTicketHistoryById(@PathVariable Long id){
        return ticketService.findTicketHistoryById(id);
    }
}
