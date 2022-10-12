package org.demo;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class ReproTest {


    static IMethodBinding parseMethodBinding(String source) {

        ASTParser astParser = ASTParser.newParser(AST.getJLSLatest());
        astParser.setResolveBindings(true);
        astParser.setBindingsRecovery(true);
        astParser.setEnvironment(
            new String[] {"target/classes"},
            new String[] {},
            new String[] {},
            true
        );


        astParser.setUnitName("Demo.java");
        astParser.setSource(source.toCharArray());

        CompilationUnit unit = (CompilationUnit) astParser.createAST(null);
        TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
        MethodDeclaration method = type.getMethods()[0];
        return method.resolveBinding();
    }

    @Test
    void missingOuterClass() {
        String source = "package org.demo;\n" +
            "\n" +
            "public class Demo {\n" +
            "    public MissingOuterClass demo() {\n" + // it references an absent outer class.
            "    }\n" +
            "}";

        IMethodBinding binding = parseMethodBinding(source);
        assertNotNull(binding);
    }

    @Test
    void missingInnerClass() {
        String source = "package org.demo;\n" +
            "\n" +
            "public class Demo {\n" +
            "    public OuterClass.InnerClass demo() {\n" + // it references an absent inner class.
            "    }\n" +
            "}";

        IMethodBinding binding = parseMethodBinding(source);
        assertNotNull(binding);

        // it can be fixed with steps:
        // 1. run `mvn package`,
        // 2. add jvm argument: -javaagent:target/repro-1.0-SNAPSHOT-jar-with-dependencies.jar
        // see org.demo.Agent for fix detail
    }
}
