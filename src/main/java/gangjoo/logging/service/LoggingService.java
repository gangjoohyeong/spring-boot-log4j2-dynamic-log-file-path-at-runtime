package gangjoo.logging.service;

import gangjoo.logging.config.LoggingConfig;
import gangjoo.logging.dto.LoggingRequestDto;
import gangjoo.logging.dto.LoggingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class LoggingService {


    public LoggingResponseDto setupLogging(LoggingRequestDto loggingRequestDto) {
        String path = loggingRequestDto.getPath();
        String level = loggingRequestDto.getLevel();

        logLevelValidation(level);
        writeLoggingYamlFile(path, level);

        return LoggingResponseDto.builder()
                .level(level)
                .path(path)
                .build();

    }

    public void logLevelValidation(String level) {
        if (!LoggingConfig.VALID_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Invalid level: " + level + " (Choose in DEBUG | INFO | WARN | ERROR)");
        }
    }

    public void writeLoggingYamlFile(String path, String level) {

        Path configPath = Paths.get(LoggingConfig.LOGGING_CONFIG_PATH);

        Map<String, Object> root = new HashMap<>();
        Map<String, String> logConfig = new HashMap<>();
        logConfig.put("level", level);
        logConfig.put("path", path);
        root.put("log", logConfig);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            yaml.dump(root, writer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public LoggingResponseDto getLoggingConfig() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        String level = loggerConfig.getLevel().toString();

        for (Appender appender : loggerConfig.getAppenders().values()) {
            if (appender instanceof RollingFileAppender rollingFileAppender) {
                String path = rollingFileAppender.getFileName();
                return LoggingResponseDto.builder()
                        .path(path)
                        .level(level)
                        .build();
            }
        }
        throw new IllegalStateException("No logging config found");
    }


}
