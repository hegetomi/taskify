package com.hegetomi.taskify.dto;

import com.hegetomi.taskify.enums.Priority;
import com.hegetomi.taskify.enums.Status;
import com.hegetomi.taskify.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime postedAt;
    private Priority priority;
    private Type type;
    private Status status;
    private UserDto poster;
    private UserDto assignee;
    private List<CommentDto> comments;
    private LocalDateTime closedAt;

}
