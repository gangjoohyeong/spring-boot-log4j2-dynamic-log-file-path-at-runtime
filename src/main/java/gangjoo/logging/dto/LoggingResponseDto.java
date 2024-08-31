package gangjoo.logging.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
public class LoggingResponseDto {

    private final String path;

    private final String level;

    @Builder
    public LoggingResponseDto(final String path, final String level) {
        this.path = path;
        this.level = level;
    }

}
