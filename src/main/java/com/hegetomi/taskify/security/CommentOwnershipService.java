package com.hegetomi.taskify.security;

import com.hegetomi.taskify.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentOwnershipService {

    private final CommentRepository commentRepository;

    public boolean isCommentedByUser(UserDetails userDetails, Long id){
        return commentRepository.findByPosterNameAndId(userDetails.getUsername(),id).isPresent();
    }

}
