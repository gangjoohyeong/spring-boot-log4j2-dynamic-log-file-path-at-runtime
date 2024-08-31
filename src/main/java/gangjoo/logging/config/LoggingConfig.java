package gangjoo.logging.config;


import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;


@Configuration
public class LoggingConfig {

    public static final String LOGGING_CONFIG_PATH = "config/logging.yaml";
    private static final String DEFAULT_LOG_PATH = "log/project-name.log";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final String DEFAULT_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    public static final Set<String> VALID_LEVELS = Set.of("DEBUG", "INFO", "WARN", "ERROR");

    // RollingFileAppender
    public static final String ROLLING_FILE_PATTERN = ".%d{yyyy-MM-dd}.%i.gz";
    public static final String ROLLING_FILE_POLICY_SIZE = "20MB";
    public static final int ROLLING_FILE_POLICY_INTERVAL = 1;
    public static final String ROLLING_FILE_STRATEGY_MAX = "7";

    @PostConstruct
    public void init() {
        Map<String, Map<String, Object>> config = loadConfig();
        String logPath = (String) config.get("log").get("path");
        String logLevel = (String) config.get("log").get("level");
        configure(logPath, logLevel);
    }

    private Map<String, Map<String, Object>> loadConfig() {
        File configFile = new File(LOGGING_CONFIG_PATH);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Map<String, Map<String, Object>> config = yaml.load(fis);
                if (isValidConfig(config)) {
                    return config;
                }
            } catch (IOException e) {
                System.out.println("Failed to read logging config file: " + e.getMessage());
            }
        }
        return createDefaultConfig(configFile);
    }

    private boolean isValidConfig(Map<String, Map<String, Object>> config) {
        if (config.containsKey("log") && config.get("log") != null) {
            Map<String, Object> logConfig = config.get("log");
            return logConfig.containsKey("path") && logConfig.containsKey("level") &&
                    VALID_LEVELS.contains(logConfig.get("level"));
        }
        return false;
    }

    private Map<String, Map<String, Object>> createDefaultConfig(File configFile) {
        Map<String, Map<String, Object>> defaultConfig = Map.of("log", Map.of("path", DEFAULT_LOG_PATH, "level", DEFAULT_LOG_LEVEL));

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            yaml.dump(defaultConfig, writer);
        } catch (IOException e) {
            System.out.println("Failed to write logging config file: " + e.getMessage());
        }
        return defaultConfig;
    }

    public void configure(String filePath, String logLevel) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(DEFAULT_LOG_PATTERN)
                .withDisableAnsi(false)
                .withCharset(StandardCharsets.UTF_8)
                .build();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.getAppenders().forEach((name, appender) -> {
            loggerConfig.removeAppender(name);
            appender.stop();
        });

        ConsoleAppender consoleAppender = ConsoleAppender.newBuilder()
                .setName("ConsoleAppender")
                .setLayout(layout)
                .build();
        consoleAppender.start();

        RollingFileAppender rollingFileAppender = RollingFileAppender.newBuilder()
                .withFileName(filePath)
                .withFilePattern(filePath + ROLLING_FILE_PATTERN)
                .withPolicy(SizeBasedTriggeringPolicy.createPolicy(ROLLING_FILE_POLICY_SIZE))
                .withPolicy(TimeBasedTriggeringPolicy.newBuilder().withInterval(ROLLING_FILE_POLICY_INTERVAL).build())
                .withStrategy(DefaultRolloverStrategy.newBuilder().withMax(ROLLING_FILE_STRATEGY_MAX).build())
                .setName("RollingFileAppender")
                .setLayout(layout)
                .withAppend(true)
                .build();
        rollingFileAppender.start();


        loggerConfig.setLevel(Level.toLevel(logLevel));
        loggerConfig.addAppender(consoleAppender, null, null);
        loggerConfig.addAppender(rollingFileAppender, null, null);

        context.updateLoggers();
    }


}
