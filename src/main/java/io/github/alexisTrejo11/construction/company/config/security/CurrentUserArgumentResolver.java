package io.github.alexisTrejo11.construction.company.config.security;

import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.shared.authorization.RolePermissionCatalog;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.CurrentUser;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;
    private final RolePermissionCatalog rolePermissionCatalog;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && parameter.getParameterType().equals(UserContext.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
            .map(user -> new UserContext(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                rolePermissionCatalog.permissionsFor(user.getRoles())
            ))
            .orElse(null);
    }
}
