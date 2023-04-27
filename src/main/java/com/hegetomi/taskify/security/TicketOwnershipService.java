package com.hegetomi.taskify.security;

import com.hegetomi.taskify.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketOwnershipService {

    private final TicketRepository ticketRepository;

    public boolean isPostedByUser(Long ticketId, UserDetails userDetails) {
        return ticketRepository.findByPosterNameAndId(userDetails.getUsername(), ticketId).isPresent();
    }

    public boolean isAssignedToUser(Long ticketId, UserDetails userDetails) {
        return ticketRepository.findByAssigneeNameAndId(userDetails.getUsername(), ticketId).isPresent();
    }

    public boolean isAvailableToAssign(Long ticketId, UserDetails userDetails) {
        return ticketRepository.findByIdAndAssigneeNameOrIdAndAssigneeNull(ticketId, userDetails.getUsername()).isPresent();
    }

}
