package com.hegetomi.taskify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
@Audited
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;
    @Column(name = "poster_name")
    private String posterName;
    @Column(name = "comment_date")
    private LocalDateTime commentDate;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

}
