package com.example.demo.nacos.enums;

/**
 * ConfigChangeType enum
 *
 * @author longqiang
 */
public enum ConfigChangeType {
    /**
     * flow rules
     */
    FLOW_RULE("flowRules"),

    /**
     * degrade rules
     */
    DEGRADE_RULE("degradeRules"),

    /**
     * authority rules
     */
    AUTHORITY_RULE("authorityRules"),

    /**
     * system rules
     */
    SYSTEM_RULE("systemRules"),

    /**
     * param flow rules
     */
    PARAM_FLOW_RULE("paramFlowRules");

    private String type;

    ConfigChangeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ConfigChangeType{" +
                "type='" + type + '\'' +
                '}';
    }
}
