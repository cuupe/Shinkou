package com.cuupe.shinkou.mapper;


import com.cuupe.shinkou.dto.InvitationDTO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface InvitationMapper {

    /**
     * 根据token查询邀请信息
     * @param token 邀请令牌
     * @return 邀请DTO对象，包含工作区信息
     */
    @Select("""
            SELECT
                id,
                token,
                email,
                name,
                department,
                position,
                role,
                status,
                expires_at AS expiresAt,
                workspace_id AS workspaceId
            FROM invitations
            WHERE token = #{token}
            LIMIT 1
            """)
    @Results(id = "InvitationDTOMap", value = {
            @Result(property = "token", column = "token"),
            @Result(property = "email", column = "email"),
            @Result(property = "name", column = "name"),
            @Result(property = "department", column = "department"),
            @Result(property = "position", column = "position"),
            @Result(property = "role", column = "role"),
            @Result(property = "status", column = "status"),
            @Result(property = "expiresAt", column = "expiresAt"),

            @Result(
                    property = "workspace",
                    column = "workspaceId",
                    one = @One(select = "com.cuupe.shinkou.mapper.AuthMapper.findWorkspaceById")
            )
    })
    InvitationDTO findTokenByToken(@Param("token") String token);
}
