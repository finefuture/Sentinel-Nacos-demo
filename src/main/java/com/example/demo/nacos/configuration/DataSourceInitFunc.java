package com.example.demo.nacos.configuration;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

import java.util.List;
import java.util.Properties;

/**
 * init sentinel DataSource
 *
 * @author longqiang
 */
public class DataSourceInitFunc implements InitFunc {

    @Override
    public void init() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "localhost:8848");

        //flow rule
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, SentinelConfigConstant.NACOS_RULES_GROUP,
                RulesKeyUtils.getFlowRulesKey(), source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        //system rule
        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new NacosDataSource<>(properties, SentinelConfigConstant.NACOS_RULES_GROUP,
                RulesKeyUtils.getSystemRulesKey(), source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());

        //authority rule
        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new NacosDataSource<>(properties, SentinelConfigConstant.NACOS_RULES_GROUP,
                RulesKeyUtils.getAuthorityRulesKey(), source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());

        //param flow rule
        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new NacosDataSource<>(properties, SentinelConfigConstant.NACOS_RULES_GROUP,
                RulesKeyUtils.getParamFlowRulesKey(), source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());

        //degrade rule
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(properties, SentinelConfigConstant.NACOS_RULES_GROUP,
                RulesKeyUtils.getDegradeRulesKey(), source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
    }

}
