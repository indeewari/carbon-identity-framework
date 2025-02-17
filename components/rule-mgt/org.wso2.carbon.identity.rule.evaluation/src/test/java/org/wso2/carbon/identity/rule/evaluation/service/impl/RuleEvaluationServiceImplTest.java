/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.rule.evaluation.service.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.core.RuleEvaluationDataManager;
import org.wso2.carbon.identity.rule.evaluation.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.internal.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.rule.management.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.internal.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.management.model.Expression;
import org.wso2.carbon.identity.rule.management.model.Rule;
import org.wso2.carbon.identity.rule.management.model.Value;
import org.wso2.carbon.identity.rule.management.service.RuleManagementService;
import org.wso2.carbon.identity.rule.management.util.RuleBuilder;
import org.wso2.carbon.identity.rule.metadata.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.config.RuleMetadataConfigFactory;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.model.Link;
import org.wso2.carbon.identity.rule.metadata.model.Operator;
import org.wso2.carbon.identity.rule.metadata.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.service.RuleMetadataService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RuleEvaluationServiceImplTest {

    private OperatorConfig operatorConfig;
    private RuleManagementService ruleManagementService;
    private RuleMetadataService ruleMetadataService;
    private RuleEvaluationDataManager ruleEvaluationDataManager;
    private RuleEvaluationDataProvider ruleEvaluationDataProvider;
    private RuleEvaluationServiceImpl ruleEvaluationService;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUpClass() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        operatorConfig = OperatorConfig.load(new File(filePath));

        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();

        ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getApplicableOperatorsInExpressions()).thenReturn(
                new ArrayList<>(operatorConfig.getOperatorsMap().values()));
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1")).thenReturn(
                mockedFieldDefinitions);
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);

        ruleManagementService = mock(RuleManagementService.class);
        RuleEvaluationComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);

        ruleEvaluationDataManager = RuleEvaluationDataManager.getInstance();
        ruleEvaluationDataProvider = mock(RuleEvaluationDataProvider.class);
        when(ruleEvaluationDataProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        when(ruleEvaluationDataProvider.getEvaluationData(any(), any(), any())).thenReturn(getMockedFieldValues());
        ruleEvaluationDataManager.registerRuleEvaluationDataProvider(ruleEvaluationDataProvider);

        ruleEvaluationService = new RuleEvaluationServiceImpl();
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testEvaluateRuleSuccess() throws Exception {

        String tenantDomain = "tenant1";
        Rule rule = createRule(tenantDomain);
        String ruleId = rule.getId();
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);

        RuleEvaluationResult result = ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);

        assertNotNull(result);
        assertEquals(result.getRuleId(), ruleId);
        assertTrue(result.isRuleSatisfied());
    }

    @Test
    public void testEvaluateInactiveRule() throws Exception {

        String tenantDomain = "tenant1";
        String ruleId = "rule1";

        Rule mockRule = mock(Rule.class);
        when(mockRule.getId()).thenReturn(ruleId);
        when(mockRule.isActive()).thenReturn(false);
        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(mockRule);

        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        RuleEvaluationResult result = ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);

        assertNotNull(result);
        assertEquals(result.getRuleId(), ruleId);
        assertFalse(result.isRuleSatisfied());
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Rule not found for the given ruleId: rule1")
    public void testFailureWhenRuleNotFound() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(null);
        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving the Rule.")
    public void testFailureWithRuleManagementExceptionWhenRetrievingRule() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenThrow(
                new RuleManagementException("Error"));

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Expression metadata from RuleMetadataService is null or empty.")
    public void testFailureForNullOrEmptyMetadata() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        when(rule.isActive()).thenReturn(true);
        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);

        when(ruleMetadataService.getExpressionMeta(any(), any())).thenReturn(null);

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving expression metadata from RuleMetadataService.")
    public void testFailureWithRuleMetadataExceptionWhenRetrievingMetadata() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        when(rule.isActive()).thenReturn(true);

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);
        when(ruleMetadataService.getExpressionMeta(any(), any())).thenThrow(
                new RuleMetadataException("Error", "ErrorMessage", "ErrorDescription"));

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    private Rule createRule(String tenantDomain) throws Exception {

        RuleBuilder ruleBuilder =
                RuleBuilder.create(org.wso2.carbon.identity.rule.management.model.FlowType.PRE_ISSUE_ACCESS_TOKEN,
                        tenantDomain);

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
    }

    private List<FieldDefinition> getMockedFieldDefinitions() {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();

        org.wso2.carbon.identity.rule.metadata.model.Field
                applicationField = new org.wso2.carbon.identity.rule.metadata.model.Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        List<Link> links = Arrays.asList(new Link("/applications?offset=0&limit=10", "GET", "values"),
                new Link("/applications?filter=name+eq+*&limit=10", "GET", "filter"));
        org.wso2.carbon.identity.rule.metadata.model.Value
                applicationValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(
                        org.wso2.carbon.identity.rule.metadata.model.Value.ValueType.REFERENCE).links(links).build();
        fieldDefinitionList.add(new FieldDefinition(applicationField, operators, applicationValue));

        org.wso2.carbon.identity.rule.metadata.model.Field
                grantTypeField = new org.wso2.carbon.identity.rule.metadata.model.Field("grantType", "grantType");
        List<OptionsValue> optionsValues = Arrays.asList(new OptionsValue("authorization_code", "authorization code"),
                new OptionsValue("password", "password"), new OptionsValue("refresh_token", "refresh token"),
                new OptionsValue("client_credentials", "client credentials"),
                new OptionsValue("urn:ietf:params:oauth:grant-type:token-exchange", "token exchange"));
        org.wso2.carbon.identity.rule.metadata.model.Value
                grantTypeValue =
                new OptionsInputValue(org.wso2.carbon.identity.rule.metadata.model.Value.ValueType.STRING,
                        optionsValues);
        fieldDefinitionList.add(new FieldDefinition(grantTypeField, operators, grantTypeValue));

        org.wso2.carbon.identity.rule.metadata.model.Field
                consentedField = new org.wso2.carbon.identity.rule.metadata.model.Field("consented", "consented");
        org.wso2.carbon.identity.rule.metadata.model.Value
                consentedValue = new InputValue(org.wso2.carbon.identity.rule.metadata.model.Value.ValueType.BOOLEAN);
        fieldDefinitionList.add(new FieldDefinition(consentedField, operators, consentedValue));

        org.wso2.carbon.identity.rule.metadata.model.Field
                riskScoreField = new org.wso2.carbon.identity.rule.metadata.model.Field("riskScore", "risk score");
        org.wso2.carbon.identity.rule.metadata.model.Value
                riskScoreValue = new InputValue(org.wso2.carbon.identity.rule.metadata.model.Value.ValueType.NUMBER);
        fieldDefinitionList.add(new FieldDefinition(riskScoreField, operators, riskScoreValue));

        return fieldDefinitionList;
    }

    private List<FieldValue> getMockedFieldValues() {

        List<FieldValue> fieldValues = new ArrayList<>();
        fieldValues.add(new FieldValue("application", "testapp", ValueType.REFERENCE));
        fieldValues.add(new FieldValue("grantType", "authorization_code", ValueType.STRING));
        return fieldValues;
    }
}
