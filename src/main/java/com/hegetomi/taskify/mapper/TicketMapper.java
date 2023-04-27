package com.hegetomi.taskify.mapper;

import com.hegetomi.taskify.command.NewTicketCommand;
import com.hegetomi.taskify.dto.TicketDto;
import com.hegetomi.taskify.dto.TicketHistoryDto;
import com.hegetomi.taskify.entity.Ticket;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    Ticket commandToModel(NewTicketCommand ticketCommand);

    @Mapping(source = "closureDate", target = "closedAt")
    TicketDto modelToDto(Ticket ticket);

    @Mapping(target = "poster.submitted", ignore = true)
    @Mapping(target = "poster.assigned", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "closureDate", target = "closedAt")
    @Named("noSub")
    TicketDto commandToModelSub(Ticket ticketCommand);

    @IterableMapping(qualifiedByName = "noSub")
    List<TicketDto> modelsToDtos(List<Ticket> tickets);

    TicketHistoryDto modelToHistoryDto(Ticket ticket);


}