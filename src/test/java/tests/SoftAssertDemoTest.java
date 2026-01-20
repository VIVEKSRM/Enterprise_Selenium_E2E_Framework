package tests;

import core.base.BaseTest;
import core.assertions.SoftAssertUtil;
import org.testng.annotations.Test;

public class SoftAssertDemoTest extends BaseTest {

    @Test
    public void softAssertDemo() {
        SoftAssertUtil.verify(true, "Step passed", "Step failed");
    }
}
