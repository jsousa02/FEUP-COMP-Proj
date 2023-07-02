package pt.up.fe.comp2023.jasmin.utils;

import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.LiteralElement;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.OptionalInt;

public class ElementUtils {

    public static Optional<Integer> castLiteralToInt(LiteralElement literal) {
        return Optional.of(literal)
                .filter(el -> el.getType().getTypeOfElement() == ElementType.INT32)
                .map(LiteralElement::getLiteral)
                .map(Integer::parseInt);
    }

    public static Optional<Boolean> castLiteralToBoolean(LiteralElement literal) {
        return Optional.of(literal)
                .filter(el -> el.getType().getTypeOfElement() == ElementType.BOOLEAN)
                .map(LiteralElement::getLiteral)
                .map(s -> s.equals("1"));
    }

    public static Optional<String> castLiteralToString(LiteralElement literal) {
        return Optional.of(literal)
                .filter(el -> el.getType().getTypeOfElement() == ElementType.STRING)
                .map(LiteralElement::getLiteral)
                .map(value -> value.substring(1, value.length() - 1));
    }
}
