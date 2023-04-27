package com.hegetomi.taskify.repository;

import com.hegetomi.taskify.dto.manager.TicketAverageSolveTimeDto;
import com.hegetomi.taskify.dto.manager.TicketSolveRateDto;
import com.hegetomi.taskify.entity.User;
import com.hegetomi.taskify.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskifyUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    @Query(nativeQuery = true, value = "Select tu.* from taskify_users tu " +
            "left join tickets t on tu.id = t.assignee_id " +
            "left join tickets t2 on tu.id = t2.poster_id " +
            "where tu.user_name = :name")
    Optional<User> findWithSubmittedAndAssignedTickets(String name);

    @Query(nativeQuery = true, value = "Select tu.* from taskify_users tu " +
            "left join tickets t on tu.id = t.assignee_id " +
            "left join tickets t2 on tu.id = t2.poster_id " +
            "where tu.id = :id")
    Optional<User> findWithSubmittedAndAssignedTicketsById(Long id);

    Optional<User> findByIdAndRolesContaining(Long user, UserRole role);

    @Query(nativeQuery = true, value = "select tu.user_name as name, count(t.id) as count from taskify_users tu " +
            "left join tickets t on tu.id = t.assignee_id " +
            "left join user_roles ur on tu.id = ur.user_id " +
            "where ur.roles= 'ROLE_EMPLOYEE' " +
            "and t.status <> 'DONE' " +
            "group by tu.user_name " +
            "order by count desc")
    List<TicketSolveRateDto> getOpenTicketStats();

    @Query(nativeQuery = true, value = "select tu.user_name as name, count(t.id) as count from taskify_users tu " +
            "left join tickets t on tu.id = t.assignee_id " +
            "left join user_roles ur on tu.id = ur.user_id " +
            "where ur.roles= 'ROLE_EMPLOYEE' " +
            "and t.status = 'DONE' " +
            "group by tu.user_name " +
            "order by count desc")
    List<TicketSolveRateDto> getClosedTicketStats();

    @Query(nativeQuery = true, value = "select tu.user_name as name, avg(datediff(t.closed_at,t.posted_at)) as days " +
            "from taskify_users tu " +
            "left join tickets t on tu.id = t.assignee_id " +
            "left join user_roles ur on tu.id = ur.user_id " +
            "where ur.roles= 'ROLE_EMPLOYEE' " +
            "group by tu.user_name " +
            "order by days desc")
    List<TicketAverageSolveTimeDto> getAverageSolveTimes();
}
