package com.hegetomi.taskify.entity;

import com.hegetomi.taskify.enums.Priority;
import com.hegetomi.taskify.enums.Status;
import com.hegetomi.taskify.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tickets")
@Audited
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_title", length = 75)
    private String title;
    @Column(name = "ticket_description", length = 250)
    private String description;
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @ManyToOne
    @JoinColumn(name = "poster_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private User poster;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private User assignee;

    @OneToMany(mappedBy = "ticket", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private Priority priority;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "ticket_type")
    private Type type;
    @Enumerated(value = EnumType.STRING)
    private Status status;
    @Column(name = "closed_at")
    private LocalDateTime closureDate;


    public Ticket(String title, String description, User poster, User assignee, Priority priority, Type type, Status status) {
        this.title = title;
        this.description = description;
        this.poster = poster;
        this.assignee = assignee;
        this.priority = priority;
        this.type = type;
        this.status = status;
    }

}
