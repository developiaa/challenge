package pro.developia._2026_08.mapping;

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
        User user = new User(1L, "ace.j", "ace.j@kakaobank.com");

        // when
        UserDto dto = userMapper.toDto(user);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("ace.j");
        assertThat(dto.email()).isEqualTo("ace.j@kakaobank.com");

        System.out.println("매핑 성공: " + dto);
    }
}
