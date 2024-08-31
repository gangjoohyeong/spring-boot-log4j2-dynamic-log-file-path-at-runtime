package gangjoo.logging.controller;


import gangjoo.logging.config.LoggingConfig;
import gangjoo.logging.dto.LoggingRequestDto;
import gangjoo.logging.dto.LoggingResponseDto;
import gangjoo.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/logging")
@Slf4j
public class LoggingController {

    private final LoggingConfig loggingConfig;
    private final LoggingService loggingService;

    @PostMapping("")
    public ResponseEntity<LoggingResponseDto> setLog(@RequestBody LoggingRequestDto loggingRequestDto) {

        LoggingResponseDto loggingResponseDto = loggingService.setupLogging(loggingRequestDto);
        loggingConfig.configure(loggingRequestDto.getPath(), loggingRequestDto.getLevel());
        return ResponseEntity.ok().body(loggingResponseDto);
    }

    @GetMapping("")
    public ResponseEntity<LoggingResponseDto> getLogSettings() {
        LoggingResponseDto loggingResponseDto = loggingService.getLoggingConfig();
        return ResponseEntity.ok().body(loggingResponseDto);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testLog() {
        log.debug("DEBUG message");
        log.info("INFO message");
        log.warn("WARN message");
        log.error("ERROR message");
        return ResponseEntity.ok().body("Logging Test");
    }
}
