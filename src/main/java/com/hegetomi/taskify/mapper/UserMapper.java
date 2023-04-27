package com.hegetomi.taskify.mapper;

import com.hegetomi.taskify.command.RegisterCommand;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.dto.UserDto;
import com.hegetomi.taskify.entity.Ticket;
import com.hegetomi.taskify.entity.User;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "username", target = "name")
    User registerCommandToUser(RegisterCommand registerCommand);

    @IterableMapping(qualifiedByName = "withoutSub")
    List<UserDto> usersToDtos(List<User> users);

    @Named("withoutSub")
    @Mapping(source = "tickets", target = "submitted")
    @Mapping(source = "assignedTickets", target = "assigned")
    UserDto userToDto(User user);

    @Mapping(target = "poster.submitted", ignore = true)
    @Mapping(target = "assignee.assigned", ignore = true)
    TicketDto subModelToSubDto(Ticket ticket);

    List<TicketDto> subModelToSubDto(List<Ticket> ticket);


    @IterableMapping(qualifiedByName = "noTicket")
    List<UserDto> usersToDtosNoTickets(List<User> users);

    @Named("noTicket")
    @Mapping(target = "submitted", ignore = true)
    @Mapping(target = "assigned", ignore = true)
    UserDto userToDtoNoTicket(User user);


}
