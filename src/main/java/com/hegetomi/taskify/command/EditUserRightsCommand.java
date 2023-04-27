package com.hegetomi.taskify.command;

import com.hegetomi.taskify.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditUserRightsCommand {
    private List<UserRole> userRoles;
}
