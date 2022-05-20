package io.jenkins.plugins.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides JUnit 5 compatibility for {@link JenkinsRule}.
 */
class JUnit5JenkinsRule extends JenkinsRule {
    private final ParameterContext context;

    JUnit5JenkinsRule(@NonNull final ParameterContext context, @NonNull final ExtensionContext extensionContext) {
        super();

        this.context = context;
        this.testDescription = Description.createTestDescription(
                extensionContext.getTestClass().map(Class::getName).orElse(null),
                extensionContext.getTestMethod().map(Method::getName).orElse(null));
    }

    @Override
    public void recipe() {
        JenkinsRecipe jenkinsRecipe = context.findAnnotation(JenkinsRecipe.class).orElse(null);
        if (jenkinsRecipe == null) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            final JenkinsRecipe.Runner<JenkinsRecipe> runner = (JenkinsRecipe.Runner<JenkinsRecipe>) jenkinsRecipe.value().getDeclaredConstructor().newInstance();
            recipes.add(runner);
            tearDowns.add(() -> runner.tearDown(this, jenkinsRecipe));
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new AssertionError(exception);
        }
    }
}
