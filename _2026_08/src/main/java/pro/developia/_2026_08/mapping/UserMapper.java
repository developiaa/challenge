package pro.developia._2026_08.mapping;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. 객체의 필드명이 모두 동일하다면 별도의 설정 없이 바로 매핑됩니다.
    UserDto toDto(User user);

    // 2. 만약 필드명이 다르다면 @Mapping 어노테이션으로 명시해 줍니다.
    // @Mapping(source = "name", target = "userName")
    // UserDto toDto(User user);
}
