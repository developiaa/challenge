package study.developia._2025_11.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilsTest {

    @Test
    void getFieldNames() {
        List<String> fieldNames = ReflectionUtils.getFieldNames(TestClass.class);
        assertAll(
                () -> assertThat(fieldNames.size()).isEqualTo(2),
                () -> assertThat(fieldNames).containsExactly("fieldName", "fieldNumber")
        );
    }

    private static class TestClass {
        public static final int num = 0;
        private String fieldName;
        private int fieldNumber;
    }
}
