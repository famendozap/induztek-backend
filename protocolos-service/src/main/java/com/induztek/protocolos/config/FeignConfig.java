package com.induztek.protocolos.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.induztek.protocolos.client")
public class FeignConfig {
}
