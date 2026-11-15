package com.taskflow.enums;

/**
 * Tipos de eventos registrados no histórico de atividades.
 */
public enum ActivityAction {
    TASK_CREATED,
    TASK_UPDATED,
    TASK_STATUS_CHANGED,
    TASK_COMPLETED,
    TASK_CANCELLED,
    TASK_DELETED,
    TASK_RESTORED,
    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_DELETED,
    CATEGORY_RESTORED
}
