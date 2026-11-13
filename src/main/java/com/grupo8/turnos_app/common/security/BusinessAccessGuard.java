package com.grupo8.turnos_app.common.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.users.entities.User;

import lombok.RequiredArgsConstructor;

// the routes that manage a business (its data, services, templates, days, employees and appointments)
// are only for the owner of that business. SecurityConfig only checks the role, so without this any
// OWNER could manage any business. admins keep the access they already had.
@Component
@RequiredArgsConstructor
public class BusinessAccessGuard {

    private final BusinessRepository businessRepository;

    public void checkCanManage(Business business) {
        User currentUser = currentUser();
        if (isAdmin(currentUser))
            return;
        if (business.getOwner() == null || !business.getOwner().getId().equals(currentUser.getId()))
            throw new ForbiddenOperationException("No tenés permiso para gestionar este negocio");
        if (Boolean.TRUE.equals(business.getDeleted()))
            throw new BusinessNotFoundException("Negocio no encontrado");
    }

    public Business checkCanManage(UUID businessPublicId) {
        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
        checkCanManage(business);
        return business;
    }

    // an employee is managed by the owner of any business it works for
    public void checkCanManageEmployee(User employee) {
        User currentUser = currentUser();
        if (isAdmin(currentUser))
            return;
        boolean worksForCurrentUser = businessRepository.findByEmployees_Id(employee.getId()).stream()
                .anyMatch(business -> business.getOwner() != null
                        && business.getOwner().getId().equals(currentUser.getId()));
        if (!worksForCurrentUser)
            throw new ForbiddenOperationException("No tenés permiso para gestionar este empleado");
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user))
            throw new ForbiddenOperationException("Tenés que iniciar sesión");
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);
    }
}
