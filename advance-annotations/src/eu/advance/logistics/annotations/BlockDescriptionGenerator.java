/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.annotations;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author szmarcell
 */
@SupportedAnnotationTypes(value = {"eu.advance.logistics.annotations.*"})
//@SupportedAnnotationTypes(value= {"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BlockDescriptionGenerator extends AbstractProcessor {

    public BlockDescriptionGenerator() {
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.getElementsAnnotatedWith(Block.class).isEmpty()) {
            return true;
        }
        try {
            FileObject f = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "block-registry.xml", (Element) null);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Opening " + f.toUri());
            Writer w = f.openWriter();
            try {
                PrintWriter pw = new PrintWriter(w);
                pw.println("<?xml version='1.0' encoding='UTF-8'?>");
                pw.println("<block-registry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"block-registry.xsd\">");
                pw.println();
                for (Element e : roundEnv.getElementsAnnotatedWith(Block.class)) {
                    if (e.getKind() != ElementKind.CLASS) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.WARNING,
                                "Not a class", e);
                        continue;
                    }
                    TypeElement clazz = (TypeElement) e;
                    Block block = e.getAnnotation(Block.class);
                    pw.println(indent("<!-- " + block.description() + " -->"));
                    String id = block.id();
                    if (id.isEmpty()) {
                        id = clazz.getSimpleName().toString();
                    }
                    pw.println(indent("<block-description class=\"" + clazz.getQualifiedName() + "\" id=\"" + id + "\" scheduler=\"" + block.scheduler() + "\">"));
                    HashSet<String> parameters = Sets.newHashSet();
                    for (String parameter : block.parameters()) {
                        if (hasBounds(parameter)) {
                            String name = getParameterName(parameter);
                            parameters.add(name);
                            pw.println(indent("<type-variable name=\"" + name + "\">", 2));
                            for (String bound : getBounds(parameter)) {
                                String type;
                                if (bound.startsWith("-")) {
                                    type = getTypeRepresentation(bound.substring(1), "lower-bound");
                                } else if (bound.startsWith("+")) {
                                    type = getTypeRepresentation(bound.substring(1), "upper-bound");
                                } else {
                                    type = getTypeRepresentation(bound, "upper-bound");
                                }
                                pw.println(indent(type, 3));
                            }
                            pw.println(indent("</type-variable>", 2));
                        } else {
                            String name = parameter;
                            parameters.add(name);
                            pw.println(indent("<type-variable name=\"" + name + "\"/>", 2));
                        }
                    }
                    for (Element enclosed : e.getEnclosedElements()) {
                        if (enclosed.getKind() == ElementKind.FIELD) {
                            VariableElement field = (VariableElement) enclosed;
                            Input input = field.getAnnotation(Input.class);
                            if (input != null) {
                                String name = (String) field.getConstantValue();
                                String inputType = getNamedTypeRepresentation(input.value(), "input", name);
                                pw.println(indent(inputType, 2));
                            }
                            Output output = field.getAnnotation(Output.class);
                            if (output != null) {
                                String name = (String) field.getConstantValue();
                                String outputType = getNamedTypeRepresentation(output.value(), "output", name);
                                pw.println(indent(outputType, 2));
                            }
                        }
                    }
                    pw.println(indent("</block-description>"));
                    pw.println();
                }
                pw.println("</block-registry>");
                pw.flush();
            } finally {
                w.close();
            }
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    x.toString());
        }

        return true;
    }
    private String getTypeRepresentation(String type, String tagName) {
        return getNamedTypeRepresentation(type, tagName, null);
    }
    private String getNamedTypeRepresentation(String type, String tagName, @javax.annotation.Nullable String name) {
        String result = "<" + tagName + " " + (name == null ? "" : "id=\"" + name + "\" ");
        if (hasTypeParameters(type)) {
            result += "type=\"" + getRootType(type) + "\">\n";
            result += indent(getTypeArgumentsRepresentation(type)) + "\n";
            result += "</" + tagName + ">";
        } else if (isParameter(type)) {
            result += "type-variable=\"" + type.substring(1) + "\"/>";
        } else {
            result += "type=\"" + type + "\"/>";
        }
        return result;
    }
    private String getTypeArgumentsRepresentation(String type) {
        String result = "";
        for (String argument : getTypeArguments(type)) {
            if (hasTypeParameters(argument)) {
                result += "<type-argument type=\"" + getRootType(argument) + "\">\n";
                result += indent(getTypeArgumentsRepresentation(argument)) + "\n";
                result += "</type-argument>";
            } else if (isParameter(argument)) {
                result += "<type-argument type-variable=\"" + argument.substring(1) + "\"/>";
            } else {
                result += "<type-argument type=\"" + argument + "\"/>";
            }
        }
        return result;
    }
    private String indent(String string) {
        return indent(string, 1);
    }
    private String indent(String string, int n) {
        String indent = "";
        for (int i = 0; i < n; i++) {
            indent += "    ";
        }
        return indent + string.replace("\n", "\n" + indent);
    }
    private String[] getTypesFromCSV(String typesList) {
        int depth = 0;
        ArrayList<String> arguments = new ArrayList<String>();
        StringBuilder builder = new StringBuilder(typesList.length());
        for (int i = 0; i < typesList.length(); i++) {
            char c = typesList.charAt(i);
            switch (c) {
                case ',':
                    if (depth == 0) {
                        arguments.add(builder.toString());
                        builder = new StringBuilder(typesList.length());
                    } else {
                        builder.append(c);
                    }
                    break;
                case '<':
                    depth++;
                    builder.append(c);
                    break;
                case '>':
                    depth--;
                    builder.append(c);
                    break;
                case ' ':
                    break;
                default:
                    builder.append(c);
            }
        }
        arguments.add(builder.toString());
        return arguments.toArray(new String[0]);
    }
    private boolean hasTypeParameters(String type) {
        return type.endsWith(">");
    }
    private boolean isParameter(String type) {
        return type.startsWith("?");
    }
    private String getRootType(String type) {
        if (type.endsWith(">")) {
            return type.substring(0, type.indexOf('<'));
        } else {
            return type;
        }
    }
    private String getParameterName(String parameter) {
        return parameter.substring(parameter.indexOf(' '));
    }
    private boolean hasBounds(String parameter) {
        return parameter.contains(" ");
    }
    private String[] getBounds(String parameter) {
        String bounds = parameter.substring(parameter.indexOf(' ') + 1);
        return getTypesFromCSV(bounds);
    }
    private String[] getTypeArguments(String type) {
        if (type.endsWith(">")) {
            String parameters = type.substring(type.indexOf('<') + 1, type.length() - 1);
            return getTypesFromCSV(parameters);
        } else {
            return new String[0];
        }
    }
    private static final Logger LOG = Logger.getLogger(BlockDescriptionGenerator.class.getName());
    public static void main(String[] args) {
        // for test purposes

        testRootType("advance:real");
        testRootType("?T");
        testRootType("advance:collection<?T>");
        testRootType("advance:collection<advance:real>");
        testRootType("advance:collection<?T, advance:real>");
        testRootType("advance:collection<advance:collection<advance:collection<advance:real, ?T>, ?U>, ?V, advance:real>");

    }
    private static void testRootType(String string) {
        BlockDescriptionGenerator generator = new BlockDescriptionGenerator();
        System.out.println(string + " : ");
        System.out.println("\t" + generator.getRootType(string));
        for (String s : generator.getTypeArguments(string)) {
            System.out.println("\t\t" + s);
        }
        System.out.println(generator.getNamedTypeRepresentation(string, "IO", "xxx"));
    }
}
