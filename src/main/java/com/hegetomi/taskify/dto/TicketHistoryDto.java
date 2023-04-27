package com.hegetomi.taskify.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistoryDto {

    private LocalDateTime revisionDate;
    private TicketDto ticketDto;
}
