class SetInline {

    int a;
    int[] b;

    SetInline lmao(int bruh) {
        return this;
    }

    SetInline set(int newA) {
        a = newA;
        this.lmao(a);
        b[2] = 3;
        this.lmao(b[1]);
        return this;
    }

    public static void main(String[] args) {
        SetInline si;

        args[0] = args[1];
        si = new SetInline();
        si.set(1).set(2).set(3).set(4).set(5);
    }
}
