package com.taskflow.dto.task;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.taskflow.enums.TaskPriority;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload de atualização parcial de tarefa.
 * As flags *Provided diferenciam omissão do campo no JSON de envio explícito de null.
 */
@Getter
@NoArgsConstructor
public class UpdateTaskRequest {

    @Setter
    @Size(min = 1, max = 150, message = "Title must have between 1 and 150 characters.")
    private String title;

    @Size(max = 5000, message = "Description must have at most 5000 characters.")
    private String description;
    private boolean descriptionProvided;

    @Setter
    private TaskPriority priority;

    private Long categoryId;
    private boolean categoryIdProvided;

    private Instant dueDate;
    private boolean dueDateProvided;

    @JsonSetter("description")
    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    @JsonSetter("categoryId")
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        this.categoryIdProvided = true;
    }

    @JsonSetter("dueDate")
    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
        this.dueDateProvided = true;
    }
}
