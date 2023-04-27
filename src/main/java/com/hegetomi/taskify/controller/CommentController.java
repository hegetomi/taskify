package com.hegetomi.taskify.controller;

import com.hegetomi.taskify.command.CreateCommentCommand;
import com.hegetomi.taskify.dto.CommentDto;
import com.hegetomi.taskify.service.CommentService;
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
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/forTicket/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_USER') " +
            "&& (@ticketOwnershipService.isPostedByUser(#id,principal) " +
            "|| @ticketOwnershipService.isAssignedToUser(#id,principal))")
    public List<CommentDto> getCommentsForTicket(@PathVariable Long id) {
        return commentService.getCommentsForTicket(id);
    }

    @PostMapping("/forTicket/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_USER') " +
            "&& (@ticketOwnershipService.isPostedByUser(#id,principal) " +
            "|| @ticketOwnershipService.isAssignedToUser(#id,principal))")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Post a new comment to a ticket")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "403", content = @Content)
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public CommentDto postCommentToTicket(@PathVariable Long id,
                                          @RequestBody @Valid CreateCommentCommand command,
                                          Principal principal) {
        return commentService.postCommentToTicket(id, command, principal.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_USER') && @commentOwnershipService.isCommentedByUser(principal,#id)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a comment by id")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "403", content = @Content)
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ProblemDetail.class))})
    public void deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
    }

}
