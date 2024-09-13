package com.ben.identity.mappers;

import com.ben.identity.dtos.others.UserInfo;
import com.ben.identity.dtos.requests.SignUpRequest;
import com.ben.identity.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(SignUpRequest request);

    UserInfo toUserInfo(User user);

}