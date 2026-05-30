package com.cuupe.shinkou.util;

import com.cuupe.shinkou.dto.UserDTO;
import com.cuupe.shinkou.eneity.User;

public class Data2DTO {
    public static UserDTO user2UserDTO(User user){
        return new UserDTO()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setPosition(user.getPosition())
                .setDepartment(user.getDepartment())
                .setStatus(user.getStatus())
                .setAvatarUrl(user.getAvatarUrl());
    }

}
