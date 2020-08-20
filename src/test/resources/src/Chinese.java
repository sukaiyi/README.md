/**
 * @author sukaiyi
 * @date 2020/08/13
 */
public class Chinese implements People {

    private static final long t = 123;
    private int age = 10;
    private String name = "张";

    @Override
    public long test(){
        return 1;
    }

    public void eat() {
        System.out.println("Chinese eat with chopsticks");
    }

//    public void walk() {
//        System.out.println("Chinese walk");
//    }
}
