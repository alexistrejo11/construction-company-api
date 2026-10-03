package io.github.alexisTrejo11.construction.company.shared;

import org.springframework.stereotype.Service;

@Service
public class MessageGenerator {
    public Result<Void> generateErrorMessage(String entityName, String action) {
        return Result.error(entityName + " " + action + " failed");
    }

    public Result<Void> generateNotFoundMessage(String entityName, Long entityId) {
        return Result.error(entityName + " with ID [" + entityId + "] not found");
    }

    public Result<Void> generateNotFoundMessage(String entityName, Object parameter, Object value ) {
        return Result.error(entityName + " with " + parameter  + " ["+ value  +"] +  not found");
    }

    // Plain Messages
    public String deleted(String entityName, Long entityId) {
        return entityName + " with ID [" + entityId + "] successfully deleted";
    }

    public String notFoundPlain(String entityName, Long entityId) {
        return entityName + " with ID [" + entityId + "] not found";
    }

    public String notFoundPlain(String entityName, Object parameter, Object value ) {
        return entityName + " with " + parameter  + " ["+ value  +"] +  not found";
    }

    public String successAction(String entityName, String action) {
        return entityName + " successfully" + action ;
    }


}

