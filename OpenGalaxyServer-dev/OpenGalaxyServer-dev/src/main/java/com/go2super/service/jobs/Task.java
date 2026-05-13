package com.go2super.service.jobs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.concurrent.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class Task {

    private String taskName;

    private Runnable runnable;
    private Future future;

    private boolean executed;

    @Override
    public boolean equals(Object other) {

        return other instanceof Task && taskName.equals(((Task) other).getTaskName());
    }

}
