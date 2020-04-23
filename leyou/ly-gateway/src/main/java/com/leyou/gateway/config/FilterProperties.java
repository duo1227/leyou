package com.leyou.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@Getter
@Setter
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {
    private List<String> allowPaths;
}