package com.hegetomi.taskify.service;

import com.hegetomi.taskify.command.CreateCommentCommand;
import com.hegetomi.taskify.dto.CommentDto;
import com.hegetomi.taskify.entity.Comment;
import com.hegetomi.taskify.entity.Ticket;
import com.hegetomi.taskify.exception.TicketNotFoundException;
import com.hegetomi.taskify.mapper.CommentMapper;
import com.hegetomi.taskify.repository.CommentRepository;
import com.hegetomi.taskify.repository.TicketRepository;
import com.hegetomi.taskify.util.TimeMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CommentMapper commentMapper;
    private final TimeMachine timeMachine;

    public List<CommentDto> getCommentsForTicket(Long id) {
        return commentMapper.modelsToDtos(commentRepository.findAllByTicketId(id));
    }

    public CommentDto postCommentToTicket(Long id, CreateCommentCommand command, String name) {

        Ticket requestedTicket = ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
        Comment comment = Comment.builder()
                .commentDate(timeMachine.getTime())
                .ticket(requestedTicket)
                .posterName(name)
                .value(command.getValue())
                .build();
        return commentMapper.modelToDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }
}
