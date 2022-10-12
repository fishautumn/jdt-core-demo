package org.demo;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import static net.bytebuddy.matcher.ElementMatchers.named;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String arg, Instrumentation inst) {
        new AgentBuilder.Default()
            .type(named("org.eclipse.jdt.internal.compiler.lookup.Scope"))
            .transform(new AgentBuilder.Transformer.ForAdvice()
                .advice(named("getMemberType"), "org.demo.Agent$SimulateFix"))
            .installOn(inst);
    }

    static class SimulateFix {
        @Advice.OnMethodExit
        public static void getMemberType(@Advice.Return(readOnly = false) ReferenceBinding ret,
                                         @Advice.This Scope self) {
            // replace return value of it's NotFound problem.
            if (ret instanceof ProblemReferenceBinding &&
                ret.problemId() == ProblemReasons.NotFound &&
                ret.closestMatch() == null) {

                ReferenceBinding closetMatch = self.environment().createMissingType(null, ret.compoundName);
                ret = new ProblemReferenceBinding(ret.compoundName, closetMatch, ProblemReasons.NotFound);
            }
        }
    }
}
