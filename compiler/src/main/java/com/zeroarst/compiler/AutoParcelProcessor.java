package com.zeroarst.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.zeroarst.library.AutoParcel;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static com.sun.org.apache.xerces.internal.xs.XSConstants.ANNOTATION;
import static com.zeroarst.compiler.Utils.getPackageName;

@SupportedAnnotationTypes("com.zeroarst.library.AutoParcel")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class AutoParcelProcessor extends AbstractProcessor {

    private final Messager messager = new Messager();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoParcel.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            if (!isValidClass(annotatedClass)) {
                return true;
            }

            try {
                generateCode(annotatedClass);
            } catch (UnnamedPackageException | IOException e) {
                messager.error(annotatedElement, "Couldn't generate class for %s: %s", annotatedClass,
                        e.getMessage());
            }
        }

        return true;
    }

    private boolean isValidClass(TypeElement annotatedClass) {
        ClassValidator classValidator = new ClassValidator(annotatedClass);

        if (!classValidator.isPublic()) {
            messager.error(annotatedClass, "Classes annotated with %s must be public.", ANNOTATION);
            return false;
        }

        if (classValidator.isAbstract()) {
            messager.error(annotatedClass, "Classes annotated with %s must not be abstract.", ANNOTATION);
            return false;
        }

        return true;
    }

    private void generateCode(TypeElement annotatedClass)
            throws UnnamedPackageException, IOException {
        String packageName = getPackageName(processingEnv.getElementUtils(), annotatedClass);

        CodeGenerator codeGenerator = new CodeGenerator(annotatedClass);
        TypeSpec generatedClass = codeGenerator.generateClass();

        JavaFile javaFile = JavaFile.builder(packageName, generatedClass).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
}