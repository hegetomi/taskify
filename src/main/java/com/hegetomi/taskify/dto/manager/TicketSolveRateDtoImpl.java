package com.hegetomi.taskify.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketSolveRateDtoImpl implements TicketSolveRateDto {
    String name;
    int count;
}
