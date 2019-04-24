package com.example.demo.nacos.configuration;

/**
 * Sentinel Config Constant
 *
 * @author longqiang
 */
public final class SentinelConfigConstant {

    private SentinelConfigConstant() {
        throw new IllegalStateException("constant class");
    }

    public static final String NAMESPACE = "application";

    public static final String CONFIG_NAMESPACE = "Sentinel-Common";

    public static final String FLOW_RULES_KEY = "flowRules";

    public static final String AUTHORITY_RULES_KEY = "authorityRules";

    public static final String DEGRADE_RULES_KEY = "degradeRules";

    public static final String SYSTEM_RULES_KEY = "systemRules";

    public static final String PARAM_FLOW_RULES_KEY = "paramFlowRules";

    public static final String APOLLO_PORTAL_URL_KEY = "portalUrl";

    public static final String APOLLO_TOKEN_KEY = "apolloApiClientToken";

    public static final String APOLLO_CONNECTION_TIMEOUT_KEY = "apolloApiClientConnectTimeout";

    public static final String APOLLO_READ_TIMEOUT_KEY = "apolloApiClientReadTimeout";

    public static final String APOLLO_OPERATOR_KEY = "sentinelOperator";

    public static final String CLIENT_INIT_OPERATOR = "client-default-operator";

    public static final String DATASOURCE_APOLLO = "Apollo";

    public static final String DATASOURCE_NACOS = "Nacos";

    public static final String NACOS_RULES_GROUP = "SENTINEL_GROUP";

}
