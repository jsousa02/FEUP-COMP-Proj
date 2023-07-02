package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

import java.util.List;
import java.util.Optional;

public class JmmNodeUtils {

    public static Type getTypeOfFirstChild(JmmNode jmmNode) {
        JmmNode typeChild = jmmNode.getJmmChild(0);

        String typeName = typeChild.get("name");
        boolean isArray = typeChild.getObject("isArray", Boolean.class);

        return new Type(typeName, isArray);
    }

    public static void setNodeType(JmmNode jmmNode, JmmType type) {
        jmmNode.putObject("jmm.type", type);
    }

    public static Optional<JmmType> getNodeType(JmmNode jmmNode) {
        if (!jmmNode.hasAttribute("jmm.type")) return Optional.empty();

        JmmType jmmType = jmmNode.getObject("jmm.type", JmmType.class);
        return Optional.ofNullable(jmmType);
    }

    public static JmmNode deepClone(JmmNode node) {
        var newNode = new JmmNodeImpl(node.getKind(), node);

        node.getChildren()
                .stream()
                .map(JmmNodeUtils::deepClone)
                .forEachOrdered(newNode::add);

        return newNode;
    }
}
