class ConstProp {

    public static void main(String[] args) {
        int a;
        a = 2;
        a = 3;

        if (a + args.length < 5) {
            a = 7;
        }
    }
}