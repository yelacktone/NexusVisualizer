package nexusviz.generator.renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;

import nexusviz.generator.util.PathGenerator;

/**
 * 構造解析結果をもとにPlantUML形式の記述に変換していく。
 * 主に強い関連(継承・実装・フィールド参照)を扱う。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class StructuralRenderer extends AbstractRenderer {
    /**
     * インスタンスを生成して応答するコンストラクタ。
     */
    public StructuralRenderer() {
        super();
        this.puml.append("@startuml クラス図").append(LINE_SEPARATOR);
        this.puml.append("hide circle").append(LINE_SEPARATOR);
        this.puml.append("skinparam classAttributeIconSize 0").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 集約関係の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addAggregation(Type fromType, String fromTypePackageName, Type toType, String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" o-- ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * 双方向の関連の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addBidirectionalAssociation(Type fromType, String fromTypePackageName, Type toType,
            String toTypePackageName) {
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(" <-[#blue]-> ");
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * クラス定義の記述を始める。
     * 
     * @param aClass              クラス情報
     * @param fullyQualifiedScope 完全修飾スコープ名
     * @param className           クラス名
     * @param isLocalType         ローカル型であるかどうか
     */
    public void addClassDefinition(ClassOrInterfaceDeclaration aClass, String fullyQualifiedScope, String className,
            Boolean isLocalType) {
        if (aClass.isAbstract()) {
            this.puml.append("abstract \"");
        } else {
            this.puml.append("class \"");
        }
        this.puml.append(escapeGenerics(className));
        this.puml.append("\" as ");
        addTypeIdString(fullyQualifiedScope, className);
        if (isLocalType) {
            this.puml.append(" <<LOCAL>>");
        }
        addModifiersToTypeDefinition(aClass, isLocalType);
        this.puml.append(" {").append(LINE_SEPARATOR);
        return;
    }

    /**
     * コンパクトコンストラクタの記述を行う。
     * 
     * @param compactConstructor コンパクトコンストラクタ情報
     * @param record             レコード宣言情報
     */
    public void addCompactConstructor(CompactConstructorDeclaration compactConstructor, RecordDeclaration record) {
        this.puml.append("\t{method} ");
        if (compactConstructor.isPrivate()) {
            this.puml.append("-");
        } else if (compactConstructor.isProtected()) {
            this.puml.append("#");
        } else if (compactConstructor.isPublic()) {
            this.puml.append("+");
        } else {
            if (record.isPrivate()) {
                this.puml.append("-");
            } else if (record.isProtected()) {
                this.puml.append("#");
            } else if (record.isPublic()) {
                this.puml.append("+");
            } else {
                this.puml.append("~");
            }
        }
        this.puml.append(" ");
        this.puml.append(compactConstructor.getNameAsString());
        this.puml.append("(");

        String delimiter = "";
        for (Parameter parameter : record.getParameters()) {
            this.puml.append(delimiter);
            this.puml.append(parameter.getNameAsString());
            this.puml.append(" : ");
            this.puml.append(parameter.getTypeAsString().replaceAll(",", ", "));
            if (parameter.isVarArgs()) {
                this.puml.append("...");
            }
            delimiter = ", ";
        }
        this.puml.append(")").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 合成関係の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addComposition(Type fromType, String fromTypePackageName, Type toType, String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" *-- ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * コンストラクタの記述を行う。
     * 
     * @param constructor コンストラクタ情報
     * @param type        型宣言情報
     */
    public void addConstructor(ConstructorDeclaration constructor, TypeDeclaration<?> type) {
        this.puml.append("\t{method} ");
        if (type.isEnumDeclaration() || constructor.isPrivate()) {
            this.puml.append("-");
        } else if (constructor.isProtected()) {
            this.puml.append("#");
        } else if (constructor.isPublic()) {
            this.puml.append("+");
        } else {
            this.puml.append("~");
        }
        this.puml.append(" ");
        this.puml.append(constructor.getNameAsString());
        this.puml.append("(");

        String delimiter = "";
        for (Parameter parameter : constructor.getParameters()) {
            this.puml.append(delimiter);
            this.puml.append(parameter.getNameAsString());
            this.puml.append(" : ");
            this.puml.append(parameter.getTypeAsString().replaceAll(",", ", "));
            if (parameter.isVarArgs()) {
                this.puml.append("...");
            }
            delimiter = ", ";
        }
        this.puml.append(")").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 包含関係の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addContainment(Type fromType, String fromTypePackageName, Type toType, String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" +-- ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * 中身が空のクラスを記述する。
     * 
     * @param packageName パッケージ名
     * @param className   クラス名
     */
    public void addEmptyClass(String packageName, String className) {
        this.puml.append("class \"");
        this.puml.append(escapeGenerics(className));
        this.puml.append("\" as ");
        addTypeIdString(packageName, className);
        this.puml.append(" {").append(LINE_SEPARATOR);
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 中身が空のインタフェースを記述する。
     * 
     * @param packageName   パッケージ名
     * @param interfaceName インタフェース名
     */
    public void addEmptyInterface(String packageName, String interfaceName) {
        this.puml.append("interface \"");
        this.puml.append(escapeGenerics(interfaceName));
        this.puml.append("\" as ");
        addTypeIdString(packageName, interfaceName);
        this.puml.append(" <<interface>> {").append(LINE_SEPARATOR);
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 列挙型定義の記述を始める。
     * 
     * @param anEnum              列挙型情報
     * @param fullyQualifiedScope 完全修飾スコープ名
     * @param enumName            列挙型名
     * @param isLocalType         ローカル型であるかどうか
     */
    public void addEnumDefinition(EnumDeclaration anEnum, String fullyQualifiedScope, String enumName,
            Boolean isLocalType) {
        this.puml.append("enum \"");
        this.puml.append(enumName);
        this.puml.append("\" as ");
        addTypeIdString(fullyQualifiedScope, enumName);
        if (isLocalType) {
            this.puml.append(" <<LOCAL>>");
        }
        addModifiersToTypeDefinition(anEnum, isLocalType);
        this.puml.append(" <<enum>> {").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 列挙子の記述を行う。
     * 
     * @param enumName 列挙型名
     * @param member   列挙子名
     */
    public void addEnumMember(String enumName, EnumConstantDeclaration member) {
        this.puml.append("\t{field} + <<enum constant>> ");
        this.puml.append(member.getNameAsString());
        this.puml.append(" : ");
        this.puml.append(enumName);
        if (member.getArguments().isNonEmpty()) {
            this.puml.append(" = ");
            this.puml.append(enumName);
            this.puml.append("(");
            String delimiter = "";
            for (Expression expr : member.getArguments()) {
                this.puml.append(delimiter);
                this.puml.append(expr.toString());
                delimiter = ", ";
            }
            this.puml.append(")");
        }
        this.puml.append(" {static} {readOnly}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * フィールドの記述を行う。
     * 
     * @param field       フィールド情報
     * @param isInterface インタフェースかどうか
     */
    public void addField(FieldDeclaration field, Boolean isInterface) {
        field.getVariables().forEach(variable -> {
            this.puml.append("\t{field} ");
            if (field.isPrivate()) {
                this.puml.append("-");
            } else if (field.isProtected()) {
                this.puml.append("#");
            } else if (isInterface || field.isPublic()) {
                this.puml.append("+");
            } else {
                this.puml.append("~");
            }
            this.puml.append(" ");
            this.puml.append(variable.getNameAsString());
            this.puml.append(" : ");
            this.puml.append(variable.getTypeAsString().replaceAll(",", ", "));
            if (variable.getInitializer().isPresent()) {
                this.puml.append(" = ");
                this.puml.append(variable.getInitializer().get().toString());
            }
            if (field.isStatic() || isInterface) {
                this.puml.append(" {static}");
            }
            if (field.isFinal() || isInterface) {
                this.puml.append(" {readOnly}");
            }
            this.puml.append(LINE_SEPARATOR);
        });
        return;
    }

    /**
     * 実装関係の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addImplementation(Type fromType, String fromTypePackageName, Type toType, String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" <|.[#lime]. ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * 継承関係の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addInheritance(Type fromType, String fromTypePackageName, Type toType, String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" <|-[#red]- ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * インタフェース定義の記述を始める。
     * 
     * @param anInterface         インタフェース情報
     * @param fullyQualifiedScope 完全修飾スコープ名
     * @param interfaceName       インタフェース名
     * @param isLocalType         ローカル型であるかどうか
     */
    public void addInterfaceDefinition(ClassOrInterfaceDeclaration anInterface, String fullyQualifiedScope,
            String interfaceName, Boolean isLocalType) {
        this.puml.append("interface \"");
        this.puml.append(escapeGenerics(interfaceName));
        this.puml.append("\" as ");
        addTypeIdString(fullyQualifiedScope, interfaceName);
        if (isLocalType) {
            this.puml.append(" <<LOCAL>>");
        }
        addModifiersToTypeDefinition(anInterface, isLocalType);
        this.puml.append(" <<interface>> {").append(LINE_SEPARATOR);
        return;
    }

    /**
     * メソッドの記述を行う。
     * 
     * @param method      メソッド情報
     * @param isInterface インタフェースかどうか
     */
    public void addMethod(MethodDeclaration method, Boolean isInterface) {
        this.puml.append("\t{method} ");
        if (method.isPrivate()) {
            this.puml.append("-");
        } else if (method.isProtected()) {
            this.puml.append("#");
        } else if (isInterface || method.isPublic()) {
            this.puml.append("+");
        } else {
            this.puml.append("~");
        }
        this.puml.append(" ");
        this.puml.append(method.getNameAsString());
        this.puml.append("(");

        String delimiter = "";
        for (Parameter parameter : method.getParameters()) {
            this.puml.append(delimiter);
            this.puml.append(parameter.getNameAsString());
            this.puml.append(" : ");
            this.puml.append(parameter.getTypeAsString().replaceAll(",", ", "));
            if (parameter.isVarArgs()) {
                this.puml.append("...");
            }
            delimiter = ", ";
        }
        this.puml.append(")");
        if (method.getType() != null) {
            this.puml.append(" : ");
            this.puml.append(method.getTypeAsString().replaceAll(",", ", "));
        } else {
            this.puml.append(" : void");
        }

        // 実装がない場合、抽象メソッド
        if (!method.getBody().isPresent()) {
            this.puml.append(" {abstract}");
        }
        if (method.isDefault()) {
            this.puml.append(" {default}");
        }
        if (method.isStatic()) {
            this.puml.append(" {static}");
        }
        if (method.isFinal()) {
            this.puml.append(" {final}");
        }
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * 型定義に修飾子情報を追加する。
     * 
     * @param aType       型情報
     * @param isLocalType ローカル型であるかどうか
     */
    public void addModifiersToTypeDefinition(TypeDeclaration<?> aType, Boolean isLocalType) {
        List<String> modifiers = new ArrayList<>();

        // メンバ型かどうかを取得
        Boolean isMemberType = aType.getParentNode().isPresent()
                && aType.getParentNode().get() instanceof TypeDeclaration<?>;

        // インタフェースのメンバ型かどうかを取得
        Boolean isMemberOfInterface = false;
        if (isMemberType) {
            TypeDeclaration<?> outerType = (TypeDeclaration<?>) aType.getParentNode().get();

            // インタフェースのメンバ型かどうか
            isMemberOfInterface = outerType.isClassOrInterfaceDeclaration()
                    && outerType.asClassOrInterfaceDeclaration().isInterface();
        }

        // メンバ型の場合
        if (isMemberType) {
            // アクセス修飾子を確認する
            if (isMemberOfInterface) { // インタフェースのメンバ型の場合
                modifiers.add("public");
            } else { // インタフェースのメンバ型でない場合
                if (aType.isPrivate()) {
                    modifiers.add("private");
                } else if (aType.isProtected()) {
                    modifiers.add("protected");
                } else if (aType.isPublic()) {
                    modifiers.add("public");
                } else {
                    modifiers.add("package-private");
                }
            }
        }

        // ローカル型でない（最上位型の）場合
        else if (!isLocalType) {
            // アクセス修飾子を確認する
            if (aType.isPrivate()) {
                modifiers.add("private");
            } else if (aType.isProtected()) {
                modifiers.add("protected");
            } else if (aType.isPublic()) {
                modifiers.add("public");
            } else {
                modifiers.add("package-private");
            }
        }

        // abstract修飾子を確認する
        if (aType instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isInterface()) {
                modifiers.add("abstract");
            } else if (classOrInterface.isAbstract()) {
                modifiers.add("abstract");
            }
        }

        // メンバ型のみstatic修飾子を確認する
        if (isMemberType) {
            // 明示static、インタフェースのメンバ型、自身がインタフェース・列挙型・レコードの場合
            Boolean isStatic = aType.isStatic()
                    || isMemberOfInterface
                    || aType.isClassOrInterfaceDeclaration() && aType.asClassOrInterfaceDeclaration().isInterface()
                    || aType.isEnumDeclaration()
                    || aType.isRecordDeclaration();

            if (isStatic) {
                modifiers.add("static");
            }
        }

        // final修飾子を確認する
        // 列挙型とレコードの場合は、finalを強制的に追加する
        if (aType.isEnumDeclaration()) {
            modifiers.add("final");
        } else if (aType.isRecordDeclaration()) {
            modifiers.add("final");
        }

        // クラスまたはインタフェースの場合は、明示finalの場合のみ
        else if (aType instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isFinal()) {
                modifiers.add("final");
            }
        }

        // 修飾子があればすべての修飾子を追加する
        if (!modifiers.isEmpty()) {
            this.puml.append(" <<");
            this.puml.append(String.join(" ", modifiers));
            this.puml.append(">>");
        }

        return;
    }

    /**
     * 多重度付き片方向関連の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addMultiplicityUnidirectionalAssociation(Type fromType, String fromTypePackageName, Type toType,
            String toTypePackageName) {
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(" -[#blue]-> \"*\" ");
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * レコード定義の記述を始める。
     * 
     * @param aRecord             レコード情報
     * @param fullyQualifiedScope 完全修飾スコープ名
     * @param recordName          レコード名
     * @param isLocalType         ローカル型であるかどうか
     */
    public void addRecordDefinition(RecordDeclaration aRecord, String fullyQualifiedScope, String recordName,
            Boolean isLocalType) {
        this.puml.append("class \"");
        this.puml.append(escapeGenerics(recordName));
        this.puml.append("\" as ");
        addTypeIdString(fullyQualifiedScope, recordName);
        if (isLocalType) {
            this.puml.append(" <<LOCAL>>");
        }
        addModifiersToTypeDefinition(aRecord, isLocalType);
        this.puml.append(" <<record>> {").append(LINE_SEPARATOR);
        return;
    }

    /**
     * レコードパラメータの記述を行う。
     * 
     * @param typeName  パラメータの型名
     * @param paramName パラメータ名
     */
    public void addRecordParameter(String typeName, String paramName) {
        this.puml.append("\t{field} - <<record component>> ");
        this.puml.append(paramName);
        this.puml.append(" : ");
        this.puml.append(typeName);
        this.puml.append(" {readOnly}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 片方向の関連の記述を行う。
     * 
     * @param fromType            参照元の型情報
     * @param fromTypePackageName 参照元の型のパッケージ名
     * @param toType              参照先の型情報
     * @param toTypePackageName   参照先の型のパッケージ名
     */
    public void addUnidirectionalAssociation(Type fromType, String fromTypePackageName, Type toType,
            String toTypePackageName) {
        addTypeIdString(toTypePackageName, toType.asString());
        this.puml.append(" <-[#blue]- ");
        addTypeIdString(fromTypePackageName, fromType.asString());
        this.puml.append(LINE_SEPARATOR);
        return;
    }

    /**
     * クラス定義の記述を終える。
     */
    public void closeClassDefinition() {
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 列挙型定義の記述を終える。
     */
    public void closeEnumDefinition() {
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * インタフェース定義の記述を終える。
     */
    public void closeInterfaceDefinition() {
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * レコード定義の記述を終える。
     */
    public void closeRecordDefinition() {
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * PlantUMLからクラス図の画像(png)を生成する。
     * 
     * @param filePath プロジェクトのパス
     * @return 生成した画像ファイルのパス文字列
     */
    public String render(Path filePath) {
        String fileName = filePath.getFileName().toString();

        String pumlFilePath = PathGenerator.outputClassDiagramFileString(fileName);
        String imageFilePath = PathGenerator.outputClassDiagramImageString(fileName);

        return exportToFile(pumlFilePath, imageFilePath);
    }

    /**
     * 型のID文字列を追加する。
     * 
     * @param packageName パッケージ名
     * @param typeName    型名
     */
    private void addTypeIdString(String packageName, String typeName) {
        if (!packageName.isEmpty()) {
            this.puml.append(escapeForIdString(packageName));
            this.puml.append("_");
        }
        this.puml.append(escapeForIdString(typeName));
        return;
    }
}
