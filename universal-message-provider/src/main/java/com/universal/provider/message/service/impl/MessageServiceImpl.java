package com.universal.provider.message.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.universal.api.message.bean.SmsBean;
import com.universal.api.message.service.MessageService;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.sms.handler.SmsHandler;

@NoCache
@Path("/")
@Service("messageService")
public class MessageServiceImpl implements MessageService {

    private final static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private SmsHandler smsHandler;

    private final Map<String, String> smsTemplateCache = new ConcurrentHashMap<>();

    @Override
    public void send(String channel, String ip, SmsBean... smses) {

        for (SmsBean smsBean : smses) {

            Sms sms = new Sms();
            sms.setChannel(channel);
            sms.setContent(smsBean.getContent());
            sms.setTo(smsBean.getPhone());
            sms.setCreateTime(DateTime.now().toDate());
            sms.setIp(ip);
            sms.setFrom(StringUtils.EMPTY);

            try {
                smsHandler.send(sms);
            } catch (Exception e) {
                logger.error("Sending failed: {}", sms);
            }
        }
    }

    @Override
    @POST
    @Path("/sms.shtml")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8" })
    public String sms(@FormParam("channel") final String channel, @FormParam("ip") final String ip, @FormParam("phone") final String phone, @FormParam("content") String content,
            @FormParam("template-code") final String templateCode, @FormParam("json-parameters") final String jsonParameters) {

        if (StringUtils.isBlank(templateCode)) {

            send(channel, ip, new SmsBean[] { new SmsBean(phone, content) });
        } else {

            Map<String, String> parameters = null;

            if (StringUtils.isNotBlank(jsonParameters)) {
                final ObjectMapper objectMapper = new ObjectMapper();
                try {
                    parameters = objectMapper.readValue(jsonParameters, new TypeReference<Map<String, String>>() {
                    });
                } catch (IOException e) {
                    logger.error("Parsing json failed: " + jsonParameters, e);
                    return "FAL";
                }
            }

            try {
                send(channel, ip, phone, templateCode, parameters);
            } catch (Exception e) {
                logger.error(String.format("Sending failed: %s, %s, %s", channel, phone, templateCode), e);
                return "FAL";
            }
        }

        return "OK";
    }

    @Override
    @GET
    @Path("/")
    public String welcome() {

        return "OK";
    }

    @Override
    public void send(String channel, String ip, String phone, String templateCode, Map<String, String> parameters) throws Exception {

        Sms sms = new Sms();
        sms.setChannel(channel);
        sms.setIp(ip);
        sms.setTo(phone);
        sms.setTemplateCode(templateCode);
        sms.setParameters(parameters);
        sms.setContent(generateSmsContent(templateCode, parameters));

        smsHandler.send(sms);

    }

    private String generateSmsContent(String templateCode, Map<String, String> parameters) throws Exception {

        String smsContent = StringUtils.EMPTY;

        if (smsTemplateCache.containsKey(templateCode)) {
            smsContent = smsTemplateCache.get(templateCode);
        } else {

            final Properties properties = this.findPropertiesFile("sms.properties");
            smsContent = properties.getProperty("sms.template." + templateCode);

            if (StringUtils.isBlank(smsContent)) {
                throw new Exception("Template " + templateCode + " is empty");
            }

            smsTemplateCache.put(templateCode, smsContent);
        }

        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                smsContent = smsContent.replace("#{" + parameter.getKey() + "}", parameter.getValue());
            }
        }

        return smsContent;
    }

    private Properties findPropertiesFile(String fileName) throws Exception {

        final Properties properties = new Properties();
        final ClassLoader classLoader = this.getClass().getClassLoader();

        properties.load(new InputStreamReader(classLoader.getResourceAsStream(fileName), "UTF-8"));

        return properties;
    }
}
