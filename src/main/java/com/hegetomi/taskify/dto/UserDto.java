package com.hegetomi.taskify.dto;

import com.hegetomi.taskify.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {

    private Long id;
    private String name;
    private List<UserRole> roles;
    private List<TicketDto> submitted;
    private List<TicketDto> assigned;

}
