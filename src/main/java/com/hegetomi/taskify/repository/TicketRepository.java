package com.hegetomi.taskify.repository;

import com.hegetomi.taskify.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query(value = "select distinct t from Ticket t left join fetch t.comments where t.id = :id")
    Optional<Ticket> findByIdWithComments(Long id);

    @Query(value = "select t from Ticket  t where t.poster.name = :poster and (:status is null or UPPER(t.status) not like :status)")
    List<Ticket> findAllByPosterName(String poster, Optional<String> status);

    @Query(value = "select t from Ticket  t where t.assignee.name = :assignee and (:status is null or UPPER(t.status) not like :status)")
    List<Ticket> findAllByAssigneeName(String assignee, Optional<String> status);

    List<Ticket> findAllByAssigneeNull();

    Optional<Ticket> findByPosterNameAndId(String posterName, Long id);

    Optional<Ticket> findByAssigneeNameAndId(String posterName, Long id);

    @Query("select t from Ticket t left join t.assignee a where t.id = :id and (t.assignee is null or a.name = :assigneeName)")
    Optional<Ticket> findByIdAndAssigneeNameOrIdAndAssigneeNull(Long id, String assigneeName);
}
