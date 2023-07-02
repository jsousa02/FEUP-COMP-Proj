package pt.up.fe.comp2023.analysis.type.primitives;

public class JmmClassType extends JmmType {

    public static final JmmClassType OBJECT = new JmmClassType("java.lang.Object", null);
    public static final JmmClassType STRING = new JmmClassType("java.lang.String", null);

    private final String simpleName;
    private final JmmClassType superClass;

    public JmmClassType(String qualifiedName, JmmClassType superClass) {
        super(qualifiedName);

        String[] fragments = qualifiedName.split("\\.");
        this.simpleName = fragments[fragments.length - 1];
        this.superClass = superClass;
    }

    public String getSimpleName() {
        return this.simpleName;
    }

    public boolean hasSuperClass() {
        return superClass != null;
    }

    public JmmClassType getSuperClass() {
        return superClass;
    }

    public boolean hasWellKnownStructure() {
        return this.equals(OBJECT) || (superClass != null && superClass.hasWellKnownStructure());
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        // We can only assign a class to another class
        if (!(other instanceof JmmClassType)) {
            return false;
        }

        if (this.equals(other)) {
            return true;
        }

        if (this.equals(JmmClassType.OBJECT)) {
            return false;
        }

        // We want to be able to assign to any imported types
        // and to any types that our super class can be assigned to
        return superClass == null || superClass.isAssignableTo(other);
    }

    @Override
    public String toOllirTypeSuffix() {
        return simpleName;
    }

    @Override
    public String toString() {
        return "JmmClassType{" +
                "name='" + getName() + '\'' +
                ", superClass=" + superClass +
                '}';
    }
}
