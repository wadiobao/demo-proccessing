package com.example.demo.modules.identity.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.modules.identity.domain.model.Admin;
import com.example.demo.modules.identity.domain.model.InvalidatedToken;
import com.example.demo.modules.identity.domain.model.NormalUser;
import com.example.demo.modules.identity.domain.model.Permission;
import com.example.demo.modules.identity.domain.model.Role;
import com.example.demo.modules.identity.domain.model.Tier;
import com.example.demo.modules.identity.domain.model.User;
import com.example.demo.modules.identity.infrastructure.persistence.entity.AdminEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.InvalidatedTokenEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.NormalUserEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.PermissionEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.RoleEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.TierEntity;
import com.example.demo.modules.identity.infrastructure.persistence.entity.UserEntity;

/**
 * MapStruct mapper bridging JPA Infrastructure Entities and pure Domain Models.
 * Keeps the domain layer free from any persistence-related dependencies.
 */
@Mapper(componentModel = "spring")
public interface IdentityEntityMapper {

    Permission toDomain(PermissionEntity entity);
    PermissionEntity toEntity(Permission domain);

    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role domain);

    Tier toDomain(TierEntity entity);
    TierEntity toEntity(Tier domain);

    default User toDomain(UserEntity entity) {
        if (entity == null) return null;
        if (entity instanceof AdminEntity) return toDomain((AdminEntity) entity);
        if (entity instanceof NormalUserEntity) return toDomain((NormalUserEntity) entity);
        return toBaseDomain(entity);
    }

    User toBaseDomain(UserEntity entity);

    default UserEntity toEntity(User domain) {
        if (domain == null) return null;
        if (domain instanceof Admin) return toEntity((Admin) domain);
        if (domain instanceof NormalUser) return toEntity((NormalUser) domain);
        return toBaseEntity(domain);
    }

    UserEntity toBaseEntity(User domain);

    Admin toDomain(AdminEntity entity);
    AdminEntity toEntity(Admin domain);

    NormalUser toDomain(NormalUserEntity entity);
    NormalUserEntity toEntity(NormalUser domain);

    InvalidatedToken toDomain(InvalidatedTokenEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "expiryTime", source = "expiryTime")
    InvalidatedTokenEntity toEntity(InvalidatedToken domain);
}
