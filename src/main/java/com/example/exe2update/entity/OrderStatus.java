package com.example.exe2update.entity;

public enum OrderStatus {
    Pending("Đang chờ xử lý"),
    Completed("Hoàn thành"),
    Cancelled("Đã hủy");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromString(String status) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + status);
    }
}
