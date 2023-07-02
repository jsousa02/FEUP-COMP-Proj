package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.jasmin.exception.OllirIsWeirdException;
import pt.up.fe.comp2023.jasmin.utils.ElementUtils;
import pt.up.fe.comp2023.utils.Pair;

import java.util.*;

public class JasminMethodBuilder {

    private final StringBuilder methodCode = new StringBuilder();
    private final InstructionGenerator generator = new InstructionGenerator();
    private final StackLimiter stackLimiter = new StackLimiter();

    private final Method method;

    public JasminMethodBuilder(Method method) {
        this.method = method;
    }

    public int getVirtualReg(Operand operand) {
        if (!method.isStaticMethod() && operand.getName().equals("this"))
            return 0;

        return method.getVarTable()
                .get(operand.getName())
                .getVirtualReg();
    }

    public boolean isByteSized(int value) {
        return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
    }

    private JasminMethodBuilder loadIntValueToStack(int value) {
        if (value >= -1 && value <= 5) {
            methodCode.append(generator.iconst(value));
        } else if (isByteSized(value)) {
            methodCode.append(generator.bipush(value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            methodCode.append(generator.sipush(value));
        } else {
            methodCode.append(generator.ldc(value));
        }

        stackLimiter.updateStack(1);
        return this;
    }

    private JasminMethodBuilder loadElementToStack(Element element) {
        if (element instanceof LiteralElement literal) {
            return loadElementToStack(literal);
        }

        if (element instanceof Operand operand) {
            return loadElementToStack(operand);
        }

        throw new IllegalArgumentException("Cannot load element that is neither LiteralElement nor Operand (%s)".formatted(element));
    }

    private JasminMethodBuilder loadElementToStack(LiteralElement literal) {
        return ElementUtils.castLiteralToInt(literal)
                .map(this::loadIntValueToStack)
                .or(() -> ElementUtils.castLiteralToBoolean(literal)
                        .map(val -> loadIntValueToStack(val ? -1 : 0)))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown LiteralElement type (%s)".formatted(literal.getType())));
    }

    private JasminMethodBuilder loadElementToStack(Operand operand) {
        if (operand instanceof ArrayOperand array) {
            return loadElementToStack(array);
        }

        var variant = InstructionGenerator.getInstructionVariant(operand.getType());
        methodCode.append(generator.load(variant, getVirtualReg(operand)));
        stackLimiter.updateStack(1);
        return this;
    }

    private JasminMethodBuilder loadElementToStack(ArrayOperand array) {
        var indexes = array.getIndexOperands();

        var baseArray = new Operand(array.getName(), new Type(ElementType.ARRAYREF));
        loadElementToStack(baseArray);

        if (indexes.isEmpty())
            return this;

        loadElementToStack(indexes.get(0));

        methodCode.append("\tiaload\n");
        stackLimiter.updateStack(-1); // Pops 2 adds 1
        return this;
    }

    private JasminMethodBuilder storeResultTo(Operand operand, Instruction result) {
        if (operand instanceof ArrayOperand array)
            return storeResultTo(array, result);

        this.instruction(result);

        var variant = InstructionGenerator.getInstructionVariant(operand.getType());
        methodCode.append(generator.store(variant, getVirtualReg(operand)));
        stackLimiter.updateStack(-1);

        return this;
    }

    private JasminMethodBuilder storeResultTo(ArrayOperand array, Instruction result) {
        var indexes = array.getIndexOperands();

        var baseArray = new Operand(array.getName(), new Type(ElementType.ARRAYREF));
        if (indexes.isEmpty())
            return storeResultTo(baseArray, result);

        loadElementToStack(baseArray);
        loadElementToStack(indexes.get(0));

        this.instruction(result);

        methodCode.append("\tiastore\n");
        stackLimiter.updateStack(-3); // Pops 3
        return this;
    }


    private String startMethod() {
        List<AccessSpec> accessSpecs = new ArrayList<>();

        switch (method.getMethodAccessModifier()) {
            case PUBLIC -> accessSpecs.add(AccessSpec.PUBLIC);
            case PRIVATE -> accessSpecs.add(AccessSpec.PRIVATE);
            case PROTECTED -> accessSpecs.add(AccessSpec.PROTECTED);
            case DEFAULT -> {}
        }

        if (method.isStaticMethod()) {
            accessSpecs.add(AccessSpec.STATIC);
        }

        if (method.isFinalMethod()) {
            accessSpecs.add(AccessSpec.FINAL);
        }

        String methodName = method.isConstructMethod() ? "<init>" : method.getMethodName();

        List<String> argDescriptors = method.getParams()
                .stream()
                .map(Element::getType)
                .map(type -> InstructionGenerator.getDescriptor(method.getOllirClass(), type))
                .toList();

        String returnTypeDescriptor = InstructionGenerator.getDescriptor(method.getOllirClass(), method.getReturnType());

        String headerCode = generator.startMethod(accessSpecs, methodName, argDescriptors, returnTypeDescriptor);
        return headerCode + '\n';
    }

    private String endMethod() {
        return generator.endMethod() + "\n\n";
    }

    private String limitStack(int num) {
        return generator.limitStack(num);
    }

    private String limitLocals(int num) {
        return generator.limitLocals(num);
    }

    public JasminMethodBuilder instruction(Instruction instruction) {
        var labels = method.getLabels(instruction);
        for (var label : labels) {
            methodCode.append(generator.label(label));
        }

        switch (instruction.getInstType()) {
            case ASSIGN -> assignInstruction((AssignInstruction) instruction);
            case GOTO -> gotoInstruction((GotoInstruction) instruction);
            case BRANCH -> branchInstruction((CondBranchInstruction) instruction);
            case CALL -> callInstruction((CallInstruction) instruction);
            case RETURN -> returnInstruction((ReturnInstruction) instruction);
            case PUTFIELD -> putFieldInstruction((PutFieldInstruction) instruction);
            case GETFIELD -> getFieldInstruction((GetFieldInstruction) instruction);
            case UNARYOPER -> unaryOpInstruction((UnaryOpInstruction) instruction);
            case BINARYOPER -> binaryOpInstruction((BinaryOpInstruction) instruction);
            case NOPER -> noperInstruction((SingleOpInstruction) instruction);
        }
        return this;
    }

    public JasminMethodBuilder callInstruction(CallInstruction instruction) {
        var ollirClass = method.getOllirClass();
        var callType = instruction.getInvocationType();
        var returnType = instruction.getReturnType();

        var callee = (Operand) instruction.getFirstArg();
        switch (callType) {
            case invokeinterface, invokespecial, invokevirtual, invokestatic -> {
                // Put args on stack
                if (callType != CallType.invokestatic) {
                    loadElementToStack(callee);
                }

                var argDescriptors = new ArrayList<String>();
                for (Element arg : instruction.getListOfOperands()) {
                    loadElementToStack(arg);
                    argDescriptors.add(InstructionGenerator.getDescriptor(ollirClass, arg.getType()));
                }

                // Create invocation string
                var calleeType = callType != CallType.invokestatic
                        ? callee.getType()
                        : new ClassType(ElementType.CLASS, callee.getName());

                String classPath = InstructionGenerator.getClassPath(ollirClass, calleeType);

                var methodNameArg = (LiteralElement) instruction.getSecondArg();
                String methodName = ElementUtils.castLiteralToString(methodNameArg)
                        .orElseThrow(() -> new OllirIsWeirdException("Method name is not a String literal (%s)".formatted(methodNameArg)));

                String returnDescriptor = InstructionGenerator.getDescriptor(ollirClass, returnType);

                methodCode.append(generator.invoke(callType.toString(), classPath, methodName, argDescriptors, returnDescriptor));

                stackLimiter.updateStack(-argDescriptors.size());
                if (callType != CallType.invokestatic) stackLimiter.updateStack(-1);
                if (returnType.getTypeOfElement() != ElementType.VOID) stackLimiter.updateStack(1);
            }

            case NEW -> {
                if (returnType.getTypeOfElement() == ElementType.ARRAYREF) {
                    var arraySize = instruction.getListOfOperands().get(0);
                    loadElementToStack(arraySize);

                    methodCode.append(generator.newArray("int"));
                    break;
                }

                String classPath = InstructionGenerator.getClassPath(ollirClass, callee.getType());
                methodCode.append(generator._new(classPath));
                stackLimiter.updateStack(1);
            }

            case arraylength -> {
                loadElementToStack(callee);
                methodCode.append(generator.arrayLength());
            }

            case ldc -> loadElementToStack(callee);
        }

        return this;
    }

    public Pair<Operand, Integer> getIincParams(BinaryOpInstruction boi) {
        var operation = boi.getOperation();
        var leftOperand = boi.getLeftOperand();
        var rightOperand = boi.getRightOperand();

        Pair<Operand, LiteralElement> values = switch (operation.getOpType()) {
            case ADD, SUB -> {
                if (leftOperand instanceof Operand operand && rightOperand instanceof LiteralElement literal)
                    yield Pair.of(operand, literal);
                else if (operation.getOpType() == OperationType.ADD) {
                    if (leftOperand instanceof LiteralElement literal && rightOperand instanceof Operand operand)
                        yield Pair.of(operand, literal);
                }

                yield null;
            }
            default -> null;
        };

        if (values == null)
            return null;

        return ElementUtils.castLiteralToInt(values.second())
                .map(val -> operation.getOpType() == OperationType.ADD ? val : -val)
                .filter(this::isByteSized)
                .map(val -> Pair.of(values.first(), val))
                .orElse(null);
    }

    public JasminMethodBuilder assignInstruction(AssignInstruction instruction) {
        if (!(instruction.getDest() instanceof Operand dest))
            throw new OllirIsWeirdException("Destination of assign instruction is not an Operand (%s)".formatted(instruction));

        var rhs = instruction.getRhs();
        if (rhs instanceof BinaryOpInstruction boi) {
            var iincParams = getIincParams(boi);
            if (iincParams != null) {
                var register = getVirtualReg(iincParams.first());
                if (register == getVirtualReg(dest)) {
                    methodCode.append(generator.iinc(iincParams.second(), register));
                    return this;
                }
            }
        }

        return storeResultTo(dest, rhs);
    }

    public JasminMethodBuilder returnInstruction(ReturnInstruction instruction) {
        if (instruction.hasReturnValue()) loadElementToStack(instruction.getOperand());

        var variant = InstructionGenerator.getInstructionVariant(instruction.getReturnType());
        methodCode.append(generator._return(variant));
        stackLimiter.updateStack(-1);
        return this;
    }

    public JasminMethodBuilder gotoInstruction(GotoInstruction instruction) {
        methodCode.append(generator.unconditionalGoto(instruction.getLabel()));
        return this;
    }

    public JasminMethodBuilder branchInstruction(CondBranchInstruction instruction) {
        this.instruction(instruction.getCondition());

        var label = instruction.getLabel();
        methodCode.append(generator.ifne(label));
        stackLimiter.updateStack(-1);

        return this;
    }

    public JasminMethodBuilder putFieldInstruction(PutFieldInstruction instruction) {
        var ollirClass = method.getOllirClass();

        var obj = instruction.getFirstOperand();
        var classPath = InstructionGenerator.getClassPath(ollirClass, obj.getType());

        var field = (Operand) instruction.getSecondOperand();
        var fieldName = field.getName();
        var fieldDescriptor = InstructionGenerator.getDescriptor(ollirClass, field.getType());

        var value = instruction.getThirdOperand();

        loadElementToStack(obj);
        loadElementToStack(value);

        methodCode.append(generator.putfield(classPath, fieldName, fieldDescriptor));
        stackLimiter.updateStack(-2);

        return this;
    }

    public JasminMethodBuilder getFieldInstruction(GetFieldInstruction instruction) {
        var ollirClass = method.getOllirClass();

        var obj = instruction.getFirstOperand();
        var classPath = InstructionGenerator.getClassPath(ollirClass, obj.getType());

        var field = (Operand) instruction.getSecondOperand();
        var fieldName = field.getName();
        var fieldDescriptor = InstructionGenerator.getDescriptor(ollirClass, field.getType());

        loadElementToStack(obj);

        methodCode.append(generator.getfield(classPath, fieldName, fieldDescriptor));

        return this;
    }

    public JasminMethodBuilder unaryOpInstruction(UnaryOpInstruction instruction) {
        var operand = (Operand) instruction.getOperand();

        loadElementToStack(operand);

        var operation = instruction.getOperation();
        switch (operation.getOpType()) {
            case NOT, NOTB -> {
                methodCode.append(generator.iconst(-1));
                stackLimiter.updateStack(1);

                methodCode.append("\tixor\n");
                stackLimiter.updateStack(-1);
            }
            default -> throw new OllirIsWeirdException("Unknown unary operation (%s)".formatted(instruction));
        }

        return this;
    }

    private int logicalOpCounter = 0;

    public JasminMethodBuilder binaryLogicalOp(BinaryOpInstruction instruction) {
        var operation = instruction.getOperation();

        var comparison = switch (operation.getOpType()) {
            case EQ -> "eq";
            case NEQ -> "ne";
            case GTE -> "ge";
            case GTH -> "gt";
            case LTE -> "le";
            case LTH -> "lt";
            default -> throw new IllegalArgumentException("Unknown comparison binary op (%s)".formatted(instruction));
        };

        var left = instruction.getLeftOperand();
        var right = instruction.getRightOperand();

        var baseLabel = "cmp_%s_%d".formatted(comparison, logicalOpCounter++);
        var trueLabel = baseLabel + "_true";
        var endLabel = baseLabel + "_end";

        loadElementToStack(left);
        loadElementToStack(right);

        methodCode.append("\tisub\n");
        stackLimiter.updateStack(-1);

        methodCode.append("\tif")
                .append(comparison)
                .append(' ')
                .append(trueLabel)
                .append('\n');
        stackLimiter.updateStack(-1);

        methodCode.append(generator.iconst(0))
                .append(generator.unconditionalGoto(endLabel))
                .append(generator.label(trueLabel))
                .append(generator.iconst(-1))
                .append(generator.label(endLabel));

        stackLimiter.updateStack(1); // iconst(0)
        // unconditionalGoto
        // label
        stackLimiter.updateStack(1); // iconst(-1)
        // label

        return this;
    }

    public JasminMethodBuilder binaryNumericOp(BinaryOpInstruction instruction) {
        var operation = instruction.getOperation();

        var combination = switch (operation.getOpType()) {
            case AND, ANDB -> "and";
            case OR, ORB -> "or";
            case ADD -> "add";
            case SUB -> "sub";
            case DIV -> "div";
            case MUL -> "mul";
            case SHL -> "shl";
            case SHR -> "shr";
            case SHRR -> "ushr";
            case XOR -> "xor";
            default -> throw new IllegalArgumentException("Unknown combination binary op (%s)".formatted(instruction));
        };

        var left = instruction.getLeftOperand();
        var right = instruction.getRightOperand();

        var variant = InstructionGenerator.getInstructionVariant(left.getType());

        loadElementToStack(left);
        loadElementToStack(right);

        methodCode.append('\t')
                .append(variant)
                .append(combination)
                .append('\n');

        stackLimiter.updateStack(-1);

        return this;
    }

    public JasminMethodBuilder binaryOpInstruction(BinaryOpInstruction instruction) {
        var operation = instruction.getOperation();

        return switch (operation.getOpType()) {
            case AND, ANDB, OR, ORB, ADD, SUB, DIV, MUL, SHL, SHR, SHRR, XOR -> binaryNumericOp(instruction);
            case EQ, NEQ, GTE, GTH, LTE, LTH -> binaryLogicalOp(instruction);
            default -> throw new IllegalArgumentException("Unknown binary op (%s)".formatted(instruction));
        };
    }

    public JasminMethodBuilder noperInstruction(SingleOpInstruction instruction) {
        return loadElementToStack(instruction.getSingleOperand());
    }

    public JasminMethodBuilder pop() {
        methodCode.append(generator.pop());
        stackLimiter.updateStack(-1);
        return this;
    }

    public String build() {
        int numLocals = method.isStaticMethod() ? 0 : 1;
        for (var descriptor : method.getVarTable().values()) {
            numLocals = Math.max(numLocals, descriptor.getVirtualReg() + 1);
        }

        StringBuilder method = new StringBuilder();

        method.append(startMethod())
                .append(limitStack(stackLimiter.getStackLimit()))
                .append(limitLocals(numLocals))
                .append(methodCode)
                .append(endMethod());

        return method.toString();
    }
}
