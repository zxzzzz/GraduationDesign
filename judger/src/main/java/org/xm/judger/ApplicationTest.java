package org.xm.judger;
import org.xm.xmnlp.Xmnlp;
public class ApplicationTest {
    public static void main(String[] args) {
        String text="我是三年级二班的小熊同学，最近你过得怎么样呢？";
        System.out.println(Xmnlp.segment(text));
    }
}
