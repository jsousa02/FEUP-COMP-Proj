package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

import java.util.Optional;

public class OllirExpression {

    private final String identifier;
    private final JmmType type;
    private final String code;

    public OllirExpression(String identifier, JmmType type, String code) {
        this.identifier = identifier;
        this.type = type;
        this.code = code == null || code.isBlank() ? null : code;
    }

    public String getReference(OllirGenerator ollir) {
        if (type == null) return identifier;
        return ollir.withType(identifier, type);
    }

    public String getIdentifier() {
        return identifier;
    }

    public JmmType getType() {
        return type;
    }

    public Optional<String> getCode() {
        return Optional.ofNullable(code);
    }
}
