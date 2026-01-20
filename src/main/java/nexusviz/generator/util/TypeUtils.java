package nexusviz.generator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * 型に関する汎用的な操作を行うユーティリティクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class TypeUtils extends Object {
    /**
     * インスタンス化を防止するためのコンストラクタ。
     */
    private TypeUtils() {
    }

    /**
     * 引数で受け取ったTypeの完全修飾スコープ名を応答する。
     * 
     * @param type 型情報
     * @return 完全修飾スコープ名
     */
    public static String getFullyQualifiedScope(Type type) {
        try {
            ResolvedType resolvedType = type.resolve();

            // 参照型の場合
            if (resolvedType.isReferenceType()) {
                ResolvedReferenceType resolvedReferenceType = resolvedType.asReferenceType();

                Optional<ResolvedReferenceTypeDeclaration> typeDeclarationOpt = resolvedReferenceType
                        .getTypeDeclaration();
                if (typeDeclarationOpt.isPresent()) {
                    Optional<Node> nodeOpt = typeDeclarationOpt.get().toAst();

                    if (nodeOpt.isPresent() && nodeOpt.get() instanceof TypeDeclaration<?> typeDeclaration) {
                        return getFullyQualifiedScope(typeDeclaration);
                    }
                }

                String qualifiedName = resolvedReferenceType.getQualifiedName();
                Integer lastDotIndex = qualifiedName.lastIndexOf(".");
                String fullyQualifiedScope = lastDotIndex == -1 ? "" : qualifiedName.substring(0, lastDotIndex);
                return fullyQualifiedScope;
            }
        } catch (Exception e) {
            System.err.println("型の完全修飾スコープ名の取得に失敗: " + type.asString());
        }
        return "";
    }

    /**
     * 引数で受け取ったTypeDeclarationの完全修飾スコープ名を応答する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return 完全修飾スコープ名
     */
    public static String getFullyQualifiedScope(TypeDeclaration<?> typeDeclaration) {
        List<String> scopeParts = new ArrayList<>();

        Node parent = typeDeclaration.getParentNode().orElse(null);

        while (parent != null) {
            // 親が型宣言の場合
            if (parent instanceof TypeDeclaration<?> parentType) {
                scopeParts.add(parentType.getNameAsString());
            }

            // 親がメソッド宣言の場合
            else if (parent instanceof MethodDeclaration parentMethod) {
                scopeParts.add(createSignature(parentMethod));
            }

            // 親がコンストラクタ宣言の場合
            else if (parent instanceof ConstructorDeclaration parentConstructor) {
                scopeParts.add(createSignature(parentConstructor));
            }

            // 親がコンパクトコンストラクタ宣言の場合
            else if (parent instanceof CompactConstructorDeclaration) {
                scopeParts.add("CompactConstructor");
            }

            // 親が初期化ブロックの場合
            else if (parent instanceof InitializerDeclaration parentInitBlock) {
                String name = parentInitBlock.isStatic() ? "StaticInitBlock" : "InitBlock";
                Integer lineNum = parentInitBlock.getBegin().map(position -> position.line).orElse(0);
                scopeParts.add(name + "-L" + lineNum);
            }

            // 親がコンパイルユニットの場合
            else if (parent instanceof CompilationUnit compilationUnit) {
                compilationUnit.getPackageDeclaration().ifPresent(pkg -> scopeParts.add(pkg.getNameAsString()));
            }

            parent = parent.getParentNode().orElse(null);
        }

        // 逆順にして結合
        Collections.reverse(scopeParts);
        String fullyQualifiedScope = String.join(".", scopeParts);

        return fullyQualifiedScope;
    }

    /**
     * ジェネリクス型の外側の型を応答する。
     * 
     * @param type 型情報
     * @return 外側の型(存在しない場合は引数の型をそのまま応答する)
     */
    public static Type getOuterGenericType(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();
            if (classType.getTypeArguments().isPresent()) {
                return new ClassOrInterfaceType(null, classType.getNameAsString());
            }
        }
        return type;
    }

    /**
     * 型がジェネリクス型かどうかを判定する。
     * 
     * @param type 型情報
     * @return ジェネリクス型であればtrue、そうでなければfalse
     */
    public static Boolean isGenerics(Type type) {
        return type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getTypeArguments().isPresent();
    }

    /**
     * 引数の呼び出し可能宣言からシグネチャを作成して応答する。
     * 
     * @param callableDeclaration 呼び出し可能宣言
     * @return シグネチャ
     */
    private static String createSignature(CallableDeclaration<?> callableDeclaration) {
        StringBuilder signature = new StringBuilder();
        signature.append(callableDeclaration.getNameAsString());
        signature.append("(");

        String delimiter = "";
        for (Parameter param : callableDeclaration.getParameters()) {
            signature.append(delimiter);
            signature.append(param.getType());
            delimiter = ",";
        }
        signature.append(")");
        return signature.toString();
    }
}
