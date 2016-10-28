package com.universal.web.support.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.universal.exception.Code;
import com.universal.exception.ValidationException;
import com.universal.web.support.annotation.Validation;

public abstract class AbstractValidationInterceptor extends HandlerInterceptorAdapter {

    private final static Map<String, Rule> rulesCache = new ConcurrentReferenceHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {

            return true;
        }

        final Method method = ((HandlerMethod) handler).getMethod();

        if (!method.isAnnotationPresent(Validation.class)) {

            return true;
        }

        String token = request.getParameter("token");
        String rawRequest = StringUtils.defaultString(request.getParameter("request"), "{}");

        String[] rawRules = method.getAnnotation(Validation.class).value();

        if (rawRules != null && rawRules.length >= 1) {
            this.validate(token, rawRequest, rawRules);
        }

        return true;
    }

    private void validate(String token, String rawRequest, String[] rawRules) throws ValidationException {

        Rule[] rules = Rule.generate(rawRules);

        ReadContext jsonObject = JsonPath.parse(rawRequest);

        for (Rule rule : rules) {

            if (rule.isLogined()) {

                if (!hasLogin(token)) {
                    throw new ValidationException(Code.USR_LOGIN_REQUIRED, StringUtils.defaultIfBlank(rule.getMessageOnUnlogined(), Code.USR_LOGIN_REQUIRED.getMessage()),
                            "Not found user by token " + token + ".");
                } else {

                    continue;
                }
            }

            String jsonValue = StringUtils.EMPTY;

            try {
                jsonValue = jsonObject.read(rule.getJsonPath()).toString();
            } catch (PathNotFoundException e) {
            }

            if (rule.isRequired() && StringUtils.isEmpty(jsonValue)) {
                throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, StringUtils.defaultIfBlank(rule.getMessageOnRequired(), Code.SYS_ILLEGAL_ARGUMENT.getMessage()),
                        "Not found " + rule.getJsonPath());
            }

            if (StringUtils.isNotEmpty(jsonValue) && StringUtils.isNotBlank(rule.getExpression()) && !jsonValue.matches(rule.getExpression())) {
                throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, StringUtils.defaultIfBlank(rule.getMessageOnExpression(), Code.SYS_ILLEGAL_ARGUMENT.getMessage()),
                        String.format("%s [%s] doesn't match %s", jsonValue, rule.getJsonPath(), rule.getExpression()));
            }
        }
    }

    protected abstract boolean hasLogin(final String token);

    private static class Rule {

        private String jsonPath;
        private boolean logined;
        private String messageOnUnlogined;
        private boolean required;
        private String messageOnRequired;
        private String expression;
        private String messageOnExpression;

        public String getJsonPath() {
            return jsonPath;
        }

        public void setJsonPath(String jsonPath) {
            this.jsonPath = jsonPath;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isLogined() {
            return logined;
        }

        public void setLogined(boolean logined) {
            this.logined = logined;
        }

        public String getMessageOnUnlogined() {
            return messageOnUnlogined;
        }

        public void setMessageOnUnlogined(String messageOnUnlogined) {
            this.messageOnUnlogined = messageOnUnlogined;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getMessageOnRequired() {
            return messageOnRequired;
        }

        public void setMessageOnRequired(String messageOnRequired) {
            this.messageOnRequired = messageOnRequired;
        }

        public String getMessageOnExpression() {
            return messageOnExpression;
        }

        public void setMessageOnExpression(String messageOnExpression) {
            this.messageOnExpression = messageOnExpression;
        }

        private static Rule generate(final String rawRule) throws ValidationException {

            String[] slices = rawRule.split(",");

            if (slices.length < 1 || slices.length > 5) {
                throw new ValidationException(Code.SYS_SERVICE_ERROR, "Invalid rule " + rawRule);
            }

            Rule rule = new Rule();

            if (slices[0].equalsIgnoreCase("login-required")) {
                rule.setLogined(true);

                if (slices.length >= 2) {
                    rule.setMessageOnUnlogined(slices[1]);
                }

                return rule;
            }

            rule.setJsonPath(slices[0]);

            if (slices[1].equalsIgnoreCase("required")) {
                rule.setRequired(true);

                if (slices.length >= 3) {
                    rule.setMessageOnRequired(slices[2]);
                }
            } else {
                rule.setExpression(slices[1]);

                if (slices.length >= 3) {
                    rule.setMessageOnExpression(slices[2]);
                }
            }

            if (slices.length >= 4) {
                rule.setExpression(slices[3]);

                if (slices.length >= 5) {
                    rule.setMessageOnExpression(slices[4]);
                }
            }

            return rule;
        }

        public static Rule[] generate(final String[] rawRules) throws ValidationException {

            List<Rule> rules = new ArrayList<Rule>();

            for (String rawRule : rawRules) {

                Rule rule = rulesCache.get(rawRule);

                if (rule == null) {

                    rule = Rule.generate(rawRule);
                    rulesCache.put(rawRule, rule);
                }

                rules.add(rule);
            }

            return rules.toArray(new Rule[0]);
        }
    }
}
