package com.zeroarst.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;

final class CodeGenerator {

    private static final String CLASS_SUFFIX = "Launcher";

    private final String annotatedClassName;
    private final String generatedClassName;

    CodeGenerator(TypeElement annotatedClass) {
        this.annotatedClassName = annotatedClass.getSimpleName().toString();
        this.generatedClassName = annotatedClassName + CLASS_SUFFIX;
    }

    TypeSpec generateClass() {
        return classBuilder(generatedClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(getIntent())
            .addMethod(startActivity())
            .build();
    }

    private MethodSpec getIntent() {
        ClassName intentClass = get("android.content", "Intent");
        return MethodSpec.methodBuilder("getIntent")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(get("android.content", "Context"), "context")
            .addStatement("return new $T(context, $L.class)", intentClass, annotatedClassName)
            .returns(intentClass)
            .build();
    }

    private MethodSpec startActivity() {

        return MethodSpec.methodBuilder("startActivity")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(get("android.content", "Context"), "context")
            .addStatement("context.startActivity(getIntent(context))")
            .build();
    }
}