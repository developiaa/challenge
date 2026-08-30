package pro.developia._2026_08.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void entityToDtoMappingTest() {
        // given
        User user = new User(1L, "ace.j", "ace.j@kakaobank.com", 1);

        // when
        UserDto dto = userMapper.toDto(user);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("ace.j");
        assertThat(dto.email()).isEqualTo("ace.j@kakaobank.com");

        System.out.println("매핑 성공: " + dto);
    }


    @Test
    @DisplayName("등급 레벨이 3 이상(3, 4)인 경우 gradeGroup이 'VIP'로 동일하게 매핑된다")
    void vipGradeMappingTest() {
        // given: 3등급(경계값)과 4등급 유저 생성
        User userLevel3 = new User(1L, "Ace", "ace@example.com", 3);
        User userLevel4 = new User(2L, "King", "king@example.com", 4);

        // when
        UserDto dto3 = userMapper.toDto(userLevel3);
        UserDto dto4 = userMapper.toDto(userLevel4);

        // then
        assertThat(dto3.gradeGroup()).isEqualTo("VIP");
        assertThat(dto4.gradeGroup()).isEqualTo("VIP");

        // 나머지 필드 정상 매핑 확인
        assertThat(dto3.name()).isEqualTo("Ace");
    }

    @Test
    @DisplayName("등급 레벨이 3 미만(1, 2)인 경우 gradeGroup이 'NORMAL'로 매핑된다")
    void normalGradeMappingTest() {
        // given: 1등급과 2등급 유저 생성
        User userLevel1 = new User(3L, "Jack", "jack@example.com", 1);
        User userLevel2 = new User(4L, "Queen", "queen@example.com", 2);

        // when
        UserDto dto1 = userMapper.toDto(userLevel1);
        UserDto dto2 = userMapper.toDto(userLevel2);

        // then
        assertThat(dto1.gradeGroup()).isEqualTo("NORMAL");
        assertThat(dto2.gradeGroup()).isEqualTo("NORMAL");
    }
}
