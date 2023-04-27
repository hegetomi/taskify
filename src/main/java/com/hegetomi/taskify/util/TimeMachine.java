package com.hegetomi.taskify.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeMachine {
    private LocalDateTime time;

    public LocalDateTime getTime() {
        if(time == null){
            return LocalDateTime.now();
        }
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
