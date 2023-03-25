package test.mysql.expression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestObject {
    private String title;
    private Integer total;

    public static TestObject of(String title, Integer total) {
        TestObject testObject = new TestObject();
        testObject.setTitle(title);
        testObject.setTotal(total);
        return testObject;
    }
}
