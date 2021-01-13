package p20;

public class A implements Runnable {

    void execute() {
        new Runnable() {
            @Override
            public void run() {
                System.out.println("hi");
            }
        }.run();
    }

    @Override
    public void run() {
        System.out.println("hello");
    }
}
