package io.github.alexisTrejo11.construction.company.modules.home.features.gethome;

public record GetHomeResponse(
    String name,
    String description,
    String version,
    String apiBasePath,
    Links links
) {
    public record Links(
        String documentation,
        String openApi,
        String health
    ) {
    }
}
