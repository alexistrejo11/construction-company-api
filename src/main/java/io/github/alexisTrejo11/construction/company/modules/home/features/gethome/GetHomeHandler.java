package io.github.alexisTrejo11.construction.company.modules.home.features.gethome;

import io.github.alexisTrejo11.construction.company.shared.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GetHomeHandler {
    private final String name;
    private final String description;
    private final String version;
    private final String apiBasePath;
    private final String documentationPath;
    private final String openApiPath;
    private final String healthPath;

    public GetHomeHandler(
        @Value("${APP_NAME:Construction Company API}") String name,
        @Value("${APP_DESCRIPTION:Construction company management API for projects, budgets, expenses, evidence, contractors, and inventory.}") String description,
        @Value("${APP_VERSION:2.0.0}") String version,
        @Value("${APP_API_BASE_PATH:/v2/api}") String apiBasePath,
        @Value("${APP_DOCS_PATH:/swagger-ui/index.html}") String documentationPath,
        @Value("${APP_OPENAPI_PATH:/api-docs}") String openApiPath,
        @Value("${APP_HEALTH_PATH:/actuator/health}") String healthPath
    ) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.apiBasePath = apiBasePath;
        this.documentationPath = documentationPath;
        this.openApiPath = openApiPath;
        this.healthPath = healthPath;
    }

    public Result<GetHomeResponse> execute() {
        GetHomeResponse.Links links = new GetHomeResponse.Links(
            documentationPath,
            openApiPath,
            healthPath
        );

        GetHomeResponse response = new GetHomeResponse(
            name,
            description,
            version,
            apiBasePath,
            links
        );

        return Result.success(response);
    }
}
