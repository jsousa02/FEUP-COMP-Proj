package pt.up.fe.comp2023.jasmin;

public class StackLimiter {
    private int stackLimit;
    private int currentStack;

    public StackLimiter() {
        this.stackLimit = 0;
        this.currentStack = 0;
    }

    public int getStackLimit() {
        return this.stackLimit;
    }

    public void updateStack(int num) {
        this.currentStack += num;
        this.stackLimit = Math.max(this.stackLimit, this.currentStack);
    }
}
