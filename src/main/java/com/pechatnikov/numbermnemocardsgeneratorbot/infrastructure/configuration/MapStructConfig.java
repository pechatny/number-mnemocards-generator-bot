package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MapStructConfig {
    // Общая конфигурация для всех мапперов
}
