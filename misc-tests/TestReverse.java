public class TestReverse {

    int a;

    public void add() {

        int b;
        int c;

        a = 4; // aload_0 (dá push para a stack do endereço do this); iconst_4; putfield TestReverse/a I
        b = 3; // iconst_3; istore_1
        c = a + b; // aload_0; getfield TestReverse/a I; iload_1; iadd; istore_2
        io.println(a); // aload_0; getfield TestReverse/a I; invokestatic io/println(I)V
        io.println(b); // iload_1; invokestatic io/println(I)V
        io.println(c); // iload_2; invokestatic io/println(I)V
    }

    public static void main(String[] args ){
        new TestReverse().add();
    }
}
