package com.hegetomi.taskify.command;

import com.hegetomi.taskify.enums.Priority;
import com.hegetomi.taskify.enums.Type;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditSubmittedTicketCommand {

    @NotBlank
    @Length(max = 75, min = 10)
    private String title;
    @NotBlank
    @Length(max = 250, min = 50)
    private String description;
    private Priority priority;
    private Type type;

}
