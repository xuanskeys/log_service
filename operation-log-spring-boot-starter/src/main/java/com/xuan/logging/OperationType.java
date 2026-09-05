package com.xuan.logging;

/** Standard operation routing categories. */
public enum OperationType {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete");

    private final String routingValue;

    OperationType(String routingValue) {
        this.routingValue = routingValue;
    }

    public String routingKey() {
        return "log." + routingValue;
    }

    public String queueSuffix() {
        return ".log." + routingValue;
    }
}
