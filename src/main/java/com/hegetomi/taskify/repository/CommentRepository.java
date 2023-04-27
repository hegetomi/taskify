package com.hegetomi.taskify.repository;

import com.hegetomi.taskify.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByTicketId(Long id);

    Optional<Comment> findByPosterNameAndId(String poster, Long id);

}
