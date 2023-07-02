package pt.up.fe.comp2023.ollir;

public class LabelGenerator {

    private int counter = 1;

    public IfStatement newIf() {
        return new IfStatement(counter++);
    }

    public WhileStatement newWhile() {
        return new WhileStatement(counter++);
    }
    
    public static class IfStatement {
        
        private final int id;

        private IfStatement(int id) {
            this.id = id;
        }
        
        public String getIf() {
            return "if" + id;
        }
        
        public String getElse() {
            return "else" + id;
        }
        
        public String getEndIf() {
            return "endif" + id;
        }
    }
    
    public class WhileStatement {
        
        private final int id;

        public WhileStatement(int id) {
            this.id = id;
        }

        public String getWhileCond() {
            return "whileCond" + id;
        }

        public String getWhileLoop() {
            return "whileLoop" + id;
        }

        public String getWhileEnd() {
            return "whileEnd" + id;
        }


    }
}
