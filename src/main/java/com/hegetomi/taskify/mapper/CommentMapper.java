package com.hegetomi.taskify.mapper;

import com.hegetomi.taskify.dto.CommentDto;
import com.hegetomi.taskify.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto modelToDto(Comment comment);

    List<CommentDto> modelsToDtos(List<Comment> commentList);
}
