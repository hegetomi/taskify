package com.hegetomi.taskify.service;

import com.hegetomi.taskify.command.EditAssignedTicketCommand;
import com.hegetomi.taskify.command.EditSubmittedTicketCommand;
import com.hegetomi.taskify.command.NewTicketCommand;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.dto.TicketHistoryDto;
import com.hegetomi.taskify.entity.Ticket;
import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.enums.Status;
import com.hegetomi.taskify.enums.UserRole;
import com.hegetomi.taskify.exception.TicketNotFoundException;
import com.hegetomi.taskify.exception.UserNotFoundException;
import com.hegetomi.taskify.mapper.TicketMapper;
import com.hegetomi.taskify.repository.TaskifyUserRepository;
import com.hegetomi.taskify.repository.TicketRepository;
import com.hegetomi.taskify.util.TimeMachine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TaskifyUserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final TimeMachine timeMachine;
    private static final String NOT_FOUND = "Requested ticket is not found";

    @PersistenceContext
    private final EntityManager entityManager;


    public TicketDto createNewTicket(NewTicketCommand ticketCommand, Principal principal) {
        Ticket ticket = ticketMapper.commandToModel(ticketCommand);
        ticket.setPoster(userRepository.findByName(principal.getName()).orElse(null));
        ticket.setStatus(Status.BACKLOG);
        ticket.setPostedAt(timeMachine.getTime());
        return ticketMapper.modelToDto(ticketRepository.save(ticket));
    }

    public List<TicketDto> getSubmittedTickets(String username, boolean full) {
        Optional<String> queryParam =
                full ? Optional.empty() : Optional.of(Status.DONE.toString());
        return ticketMapper.modelsToDtos(ticketRepository.findAllByPosterName(username, queryParam));
    }

    @Transactional
    public TicketDto editSubmittedTicketDetail(Long id, EditSubmittedTicketCommand ticketCommand) {
        Ticket requestedTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(NOT_FOUND));
        updateSubmission(ticketCommand, requestedTicket);
        return ticketMapper.modelToDto(requestedTicket);
    }

    public List<TicketDto> getAssignedTickets(String username, boolean full) {
        Optional<String> queryParam =
                full ? Optional.empty() : Optional.of(Status.DONE.toString());
        return ticketMapper.modelsToDtos(ticketRepository.findAllByAssigneeName(username, queryParam));
    }

    public TicketDto getTicketDetail(Long id) {
        Ticket requestedTicket = ticketRepository.findByIdWithComments(id)
                .orElseThrow(() -> new TicketNotFoundException(NOT_FOUND));
        return ticketMapper.modelToDto(requestedTicket);
    }

    @Transactional
    public TicketDto editAssignedTicketDetail(Long id, EditAssignedTicketCommand ticketCommand) {
        Ticket requestedTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(NOT_FOUND));
        updateAssigned(ticketCommand, requestedTicket);
        return ticketMapper.modelToDto(requestedTicket);

    }

    public List<TicketDto> getUnassignedTickets() {
        return ticketMapper.modelsToDtos(ticketRepository.findAllByAssigneeNull());
    }

    @Transactional
    public TicketDto assignToUser(Long ticketId, Long user) {
        Ticket requestedTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(NOT_FOUND));
        User selectedUser = userRepository.findByIdAndRolesContaining(user, UserRole.ROLE_EMPLOYEE)
                .orElseThrow(UserNotFoundException::new);
        requestedTicket.setAssignee(selectedUser);
        return ticketMapper.modelToDto(requestedTicket);
    }

    private void updateSubmission(EditSubmittedTicketCommand ticketCommand, Ticket requestedTicket) {
        requestedTicket.setType(ticketCommand.getType());
        requestedTicket.setDescription(ticketCommand.getDescription());
        requestedTicket.setTitle(ticketCommand.getTitle());
        requestedTicket.setPriority(ticketCommand.getPriority());
    }

    private void updateAssigned(EditAssignedTicketCommand ticketCommand, Ticket requestedTicket) {
        requestedTicket.setType(ticketCommand.getType());
        requestedTicket.setDescription(ticketCommand.getDescription());
        requestedTicket.setTitle(ticketCommand.getTitle());
        requestedTicket.setPriority(ticketCommand.getPriority());
        requestedTicket.setStatus(ticketCommand.getStatus());
        if (requestedTicket.getStatus().equals(Status.DONE)) {
            requestedTicket.setClosureDate(timeMachine.getTime());
        } else {
            requestedTicket.setClosureDate(null);
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<TicketHistoryDto> findTicketHistoryById(Long id) {
        return AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(Ticket.class, false, true)
                .add(AuditEntity.property("id").eq(id))
                .getResultList()
                .stream()
                .map(auditObject -> mapTicketHistoryDto((Object[]) auditObject))
                .toList();
    }


    private TicketHistoryDto mapTicketHistoryDto(Object[] auditObject) {
        DefaultRevisionEntity revEntity = (DefaultRevisionEntity) auditObject[1];
        Ticket currentTicket = (Ticket) auditObject[0];
        TicketDto currentTicketAsDto = ticketMapper.modelToDto(currentTicket);
        TicketHistoryDto ticketHistoryDto = new TicketHistoryDto();
        ticketHistoryDto.setTicketDto(currentTicketAsDto);
        ticketHistoryDto.setRevisionDate(revEntity.getRevisionDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        return ticketHistoryDto;
    }
}
