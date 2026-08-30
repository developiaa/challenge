package pro.developia._2026_08.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 소스(엔티티)의 gradeLevel을 대상(DTO)의 gradeGroup으로 매핑하되,
    // "gradeToGroup"이라는 이름이 붙은 커스텀 메서드를 통과시키도록 설정합니다.
    @Mapping(source = "grade", target = "gradeGroup", qualifiedByName = "gradeToGroup")
    UserDto toDto(User user);

    // 2. 만약 필드명이 다르다면 @Mapping 어노테이션으로 명시해 줍니다.
    // @Mapping(source = "name", target = "userName")
    // UserDto toDto(User user);


    // 커스텀 변환 로직 (default 메서드 활용)
    @Named("gradeToGroup")
    default String mapGradeToGroup(int gradeLevel) {
        // 특정 등급(예: 3등급) 이상인 경우 모두 "VIP"로 동일하게 취급
        if (gradeLevel >= 3) {
            return "VIP";
        }
        return "NORMAL";
    }
}
