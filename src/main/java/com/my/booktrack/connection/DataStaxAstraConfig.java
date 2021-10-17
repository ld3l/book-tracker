package com.my.booktrack.connection;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
@Data
@ConfigurationProperties(prefix = "datastax.astra")
public class DataStaxAstraConfig {
    private File secureConnectBundle;


}
