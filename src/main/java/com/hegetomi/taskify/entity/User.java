package com.hegetomi.taskify.entity;

import com.hegetomi.taskify.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "taskify_users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20, name = "user_name")
    private String name;
    @Column(name = "user_password")
    private String password;
    @Enumerated(value = EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<UserRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "poster")
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "assignee")
    private List<Ticket> assignedTickets = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(e -> new SimpleGrantedAuthority(e.name()))
                .toList();
    }

    public User(Long id, String name, String password, List<UserRole> roles) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
