package com.itheima.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class UploadFileTest {
    @Test
    public void test1(){
        List<String> list = new ArrayList<>();
        list.add("张无忌");
        list.add("周芷若");
        list.add("赵敏");
        list.add("张强");
        list.add("张三丰");
        
        list.stream().filter(s->s.startsWith("张")).forEach(s-> System.out.println("s = " + s));
    }
}
