package io.github.alexisTrejo11.construction.company.modules.notification.features.list;

public record ListNotificationsQuery(Boolean read, int page, int size, String sort) {
}
