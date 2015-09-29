package com.ofg.infrastructure.web.logging;

import com.ofg.infrastructure.web.logging.config.LogsConfig;
import com.ofg.infrastructure.web.logging.obfuscation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import javax.servlet.Filter;
import java.util.List;

/**
 * Configuration that registers a bean that will automatically if DEBUG level of logging is set on
 * {@link RequestBodyLoggingContextFilter}
 * print request body in logs - you can limit its length by setting a property
 *
 * @see RequestBodyLoggingContextFilter
 */
@Configuration
public class RequestLoggingConfiguration {

    @Bean
    public Filter requestBodyLoggingContextFilter(@Value("${request.payload.logging.maxlength:2000}") int maxPayloadLength) {
        return new RequestBodyLoggingContextFilter(maxPayloadLength);
    }

    @Bean
    ClientHttpRequestInterceptor createHttpClientCallLogger() {
        return new HttpClientCallLogger();
    }

    @Bean
    Filter createHttpControllerCallLogger(LogsConfig props, PayloadObfuscationProcessor obfuscator) {
        return new HttpControllerCallLogger(props, obfuscator);
    }

    @Bean
    ObfuscationFieldStrategy createFieldReplacementStrategy() {
        return new FieldReplacementStrategy();
    }

    @Bean
    AbstractPayloadObfusctator createJsonObfuscator(ObfuscationFieldStrategy obfuscationFieldStrategy){
        return new JsonPayloadObfuscator(obfuscationFieldStrategy);
    }

    @Bean
    AbstractPayloadObfusctator createXmlObfuscator(ObfuscationFieldStrategy obfuscationFieldStrategy){
        return new XmlPayloadObfuscator(obfuscationFieldStrategy);
    }

    @Bean
    PayloadObfuscationProcessor createObfuscationProcessor(List<AbstractPayloadObfusctator> obfusctatorList){
        return new PayloadObfuscationProcessor(obfusctatorList);
    }

    @Bean
    LogsConfig createLogsConfig(){
        return new LogsConfig();
    }

}
