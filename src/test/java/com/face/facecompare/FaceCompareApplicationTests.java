package com.face.facecompare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class FaceCompareApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(System.getProperty("user.dir").substring(0,System.getProperty("user.dir").lastIndexOf("\\")));
    }

}
