package com.zeroarst.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.zeroarst.library.Callback;
import com.zeroarst.library.CallbackFragment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.zeroarst.library.CallbackFragment")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class CallbackFragmentProcessor extends AbstractProcessor {

    private javax.annotation.processing.Messager mMgr;

    private boolean mHelperClassGenerated = false;

    private static final String CLEAR_FRAGMENT_CALLBACK_FIELDS_METHOD_NAME = "clearFragmentCallbackFields";

    private static final String FRAGMENT_CALLBACK_FIELDS_NAME = "mFragmentCallbackFields";

    private static final String HELPER_CLASS_NAME = "_CallbackFragmentHelper";

    private static final String HELPER_CAST_METHOD_NAME = "castFragmentCallback";

    private static final ClassName FRAGMENT_CLASS_NAME = ClassName.get("android.support.v4.app", "Fragment");


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMgr = processingEnvironment.getMessager();


    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(CallbackFragment.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            if (!isValidClass(annotatedClass)) {
                return true;
            }

            try {
                if (!mHelperClassGenerated) {
                    generateHelperClass(annotatedClass);
                    mHelperClassGenerated = true;
                }
                generateFragmentCode(annotatedClass);
            } catch (IOException e) {
                mMgr.printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format("Couldn't generate class for %s: %s", annotatedClass, e.getMessage()),
                    annotatedElement
                );
            }
        }

        return true;
    }

    private boolean isSubClass(TypeElement typeElement, ClassName superClassName) {
        TypeElement superTypeElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();

        if (superTypeElement.toString().equals(Object.class.getName())) {
            return false;
        } else if (superTypeElement.toString().equals(superClassName.reflectionName())) {
            return true;
        }

        return isSubClass(superTypeElement, superClassName);
    }

    private boolean isValidClass(TypeElement annotatedClass) {

        // annotatedClass.getSuperclass() == ClassName.get("android.support.v4.app", "Fragment")
        ClassValidator classValidator = new ClassValidator(annotatedClass);

        if (!isSubClass(annotatedClass, FRAGMENT_CLASS_NAME)) {
            mMgr.printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Classes annotated with %s must extend %s", CallbackFragment.class.getSimpleName(), FRAGMENT_CLASS_NAME),
                annotatedClass
            );
            return false;
        }

        if (!classValidator.isPublic()) {
            mMgr.printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Classes annotated with %s must be public", CallbackFragment.class.getSimpleName()),
                annotatedClass
            );
            return false;
        }

        if (classValidator.isAbstract()) {
            mMgr.printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Classes annotated with %s must not be abstract.", CallbackFragment.class.getSimpleName()),
                annotatedClass
            );
            return false;
        }

        return true;
    }

    private void generateFragmentCode(TypeElement annotatedClass) throws IOException {

        final PackageElement pkg = processingEnv.getElementUtils().getPackageOf(annotatedClass);

        final ClassName annotatedClassName = ClassName.get(annotatedClass);
        final String packageName = pkg.getQualifiedName().toString();

        final ClassName generatedClassName = ClassName.get(packageName, annotatedClass.getSimpleName().toString() + "Callbackable");

        final MethodSpec createMs = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("$T fg = new $T()", generatedClassName, generatedClassName)
            .addStatement("fg.setArguments(new $T())", ClassName.get("android.os", "Bundle"))
            .addStatement("return fg")
            .returns(annotatedClassName)
            .build();

        final MethodSpec onAttachMs = MethodSpec.methodBuilder("onAttach")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get("android.content", "Context"), "context")
            .addStatement("super.onAttach(context)")
            .addStatement(HELPER_CLASS_NAME + "." + HELPER_CAST_METHOD_NAME + "(this, " + FRAGMENT_CALLBACK_FIELDS_NAME + ")")
            .build();


        final MethodSpec onDetachMs = MethodSpec.methodBuilder("onDetach")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("super.onDetach()")
            .addStatement(HELPER_CLASS_NAME + "." + CLEAR_FRAGMENT_CALLBACK_FIELDS_METHOD_NAME + "(this, " +
                FRAGMENT_CALLBACK_FIELDS_NAME + ")")
            .build();

        FieldSpec callbackFieldSpec = FieldSpec.builder(
            ParameterizedTypeName.get(List.class, Field.class),
            FRAGMENT_CALLBACK_FIELDS_NAME,
            Modifier.PRIVATE)
            .initializer("new $T<$T>()", ArrayList.class, Field.class)
            .build();

        final TypeSpec ts = TypeSpec.classBuilder(generatedClassName)
            .superclass(annotatedClassName)
            .addField(callbackFieldSpec)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(createMs)
            .addMethod(onAttachMs)
            .addMethod(onDetachMs)
            .build();


        // CodeGenerator codeGenerator = new CodeGenerator(annotatedClass);
        // TypeSpec generatedClass = codeGenerator.generateClass();

        final JavaFile javaFile = JavaFile.builder(packageName, ts).build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    private void generateHelperClass(TypeElement annotatedClass) throws IOException {

        final PackageElement pkg = processingEnv.getElementUtils().getPackageOf(annotatedClass);
        final String packageName = pkg.getQualifiedName().toString();


        final CodeBlock cb = CodeBlock.builder()
            .add("for (Class<?> cls : fragment.getClass().getSuperclass().getDeclaredClasses()) {\n")
            .add("    $T callback = cls.getAnnotation($T.class);\n", Callback.class, Callback.class)
            .add("    if (callback == null)\n")
            .add("        continue;\n")
            .add("    for ($T f : fragment.getClass().getSuperclass().getDeclaredFields()) {\n", Field.class)
            .add("        if (f.getType() != cls)\n")
            .add("            continue;\n")
            .add("        String formatter = \"%s must implement \" + cls.toString();\n")
            .add("        try {\n")
            .add("            if (fragment.getTargetFragment() != null) {\n")
            .add("                if (cls.isAssignableFrom(fragment.getTargetFragment().getClass())) {\n")
            .add("                    if (!f.isAccessible());\n")
            .add("                        f.setAccessible(true);\n")
            .add("                    f.set(fragment, fragment.getTargetFragment());\n")
            .add("                    fragmentCallbackFields.add(f);\n")
            .add("                } else if (callback.mandatory())\n")
            .add("                    throw new ClassCastException(String.format(formatter, fragment.getTargetFragment().getClass()));\n")
            .add("            } else if (fragment.getParentFragment() != null) {\n")
            .add("                if (cls.isAssignableFrom(fragment.getParentFragment().getClass())) {\n")
            .add("                    if (!f.isAccessible())\n")
            .add("                        f.setAccessible(true);\n")
            .add("                    f.set(fragment, fragment.getParentFragment());\n")
            .add("                    fragmentCallbackFields.add(f);\n")
            .add("                } else if (callback.mandatory())\n")
            .add("                    throw new ClassCastException(String.format(formatter, fragment.getParentFragment().getClass()));\n")
            .add("            } else {\n")
            .add("                if (cls.isAssignableFrom(fragment.getContext().getClass())) {\n")
            .add("                    if (!f.isAccessible())\n")
            .add("                        f.setAccessible(true);\n")
            .add("                    f.set(fragment, fragment.getContext());\n")
            .add("                    fragmentCallbackFields.add(f);\n")
            .add("                } else if (callback.mandatory())\n")
            .add("                    throw new ClassCastException(String.format(formatter, fragment.getContext().getClass()));\n")
            .add("            }\n")
            .add("        } catch (IllegalAccessException e) {\n")
            .add("            $T.e(TAG, e.getMessage(), e);\n", ClassName.get("android.util", "Log"))
            .add("        }\n")
            .add("    }\n")
            .add("}\n")
            .build();

        final MethodSpec castFragmentCallbackMs = MethodSpec.methodBuilder(HELPER_CAST_METHOD_NAME)
            .addModifiers(Modifier.STATIC)
            .addParameter(ClassName.get("android.support.v4.app", "Fragment"), "fragment")
            .addParameter(ParameterizedTypeName.get(List.class, Field.class), "fragmentCallbackFields")
            .addCode(cb)
            .build();

        final MethodSpec clearFragmentCallbackFieldMs = MethodSpec.methodBuilder(CLEAR_FRAGMENT_CALLBACK_FIELDS_METHOD_NAME)
            .addModifiers(Modifier.STATIC)
            .addParameter(ClassName.get("android.support.v4.app", "Fragment"), "fragment")
            .addParameter(ParameterizedTypeName.get(List.class, Field.class), "fragmentCallbackFields")
            .addCode("for ($T f : fragmentCallbackFields) {\n", Field.class)
            .addCode("    try {\n")
            .addCode("        f.set(fragment, null);\n")
            .addCode("    } catch (IllegalAccessException e) { \n")
            .addCode("        $T.e(TAG, e.getMessage(), e);\n", ClassName.get("android.util", "Log"))
            .addCode("    }\n")
            .addCode("}\n")
            .build();

        final ClassName generatedClassName = ClassName.get(packageName, HELPER_CLASS_NAME);

        final TypeSpec ts = TypeSpec.classBuilder(generatedClassName)
            .addField(FieldSpec.builder(String.class, "TAG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"$T\"", generatedClassName)
                .build())
            .addModifiers(Modifier.FINAL)
            .addMethod(castFragmentCallbackMs)
            .addMethod(clearFragmentCallbackFieldMs)
            .build();

        final JavaFile javaFile = JavaFile.builder(packageName, ts).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
}
