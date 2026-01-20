package nexusviz.generator.analyzer;

import static nexusviz.generator.model.structure.RelationType.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import nexusviz.generator.model.structure.TypeInfo;
import nexusviz.generator.model.structure.TypeRelationInfo;
import nexusviz.generator.result.StructuralAnalysisResult;
import nexusviz.generator.util.TypeUtils;

/**
 * プロジェクトの構造情報の解析を行うクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class StructuralAnalyzer extends AbstractAnalyzer<StructuralAnalysisResult> {
    /**
     * 型情報を保持するSetを束縛する。
     */
    private Set<TypeInfo> typeInfos;

    /**
     * 型同士の関係情報を保持するSetを束縛する。
     */
    private Set<TypeRelationInfo> typeRelations;

    /**
     * デフォルトコンストラクタ。
     */
    public StructuralAnalyzer() {
    }

    /**
     * ファイル単位の解析を行う。
     * 
     * @param aCompilationUnit コンパイルユニット
     * @param filePath         解析するファイルのパス
     */
    @Override
    protected void analyzeUnit(CompilationUnit aCompilationUnit, Path filePath) {
        try {
            // 型情報を収集する
            aCompilationUnit.findAll(TypeDeclaration.class).forEach(aType -> {
                this.typeInfos.add(createTypeInfo(aType));
            });
        } catch (ParseProblemException e) {
            System.err.println("構文エラー: " + filePath + " - " + e.getMessage());
            handleError();
        } catch (Exception e) {
            System.err.println("予期しないエラー: " + filePath + " - " + e.getMessage());
            handleError();
        }
        return;
    }

    /**
     * 最終結果を生成して応答する。
     * 
     * @return 解析結果
     */
    @Override
    protected StructuralAnalysisResult buildResult() {
        // 型情報が一つも見つからなかった場合、nullを応答する
        if (this.typeInfos.isEmpty()) {
            System.out.println("型情報が見つかりませんでした。");
            return null;
        }

        // 型同士の関係情報を解析する
        this.typeRelations.addAll(TypeRelationAnalyzer.analyze(this.typeInfos));

        // 関係情報に含まれるtoTypeのうち、登録されていない型情報を追加する
        addTypeInfosFromRelations(this.typeInfos, this.typeRelations);

        // this.hasErrorがtrueならその旨を通知
        if (this.hasError) {
            System.out.println("解析中にエラーが発生しました。解析結果の一部が不完全になっています。");
        }

        // 結果を返す
        return new StructuralAnalysisResult(this.typeInfos, this.typeRelations);
    }

    /**
     * 解析結果の要素を初期化する。
     */
    @Override
    protected void initializeResultElements() {
        this.typeInfos = new LinkedHashSet<>();
        this.typeRelations = new LinkedHashSet<>();
        return;
    }

    /**
     * 型関係情報の集合のtoTypeのうち、登録されていない型情報を追加する。
     * 
     * @param typeInfos     既存の型情報の集合
     * @param typeRelations 関係情報の集合
     */
    private void addTypeInfosFromRelations(Set<TypeInfo> typeInfos,
            Set<TypeRelationInfo> typeRelations) {
        // 既に登録されている完全修飾型名の集合を作成
        Set<String> registeredTypeNames = new HashSet<>();
        typeInfos.forEach(typeInfo -> {
            String fullyQualifiedTypeName = typeInfo.fullyQualifiedScope() + "." + typeInfo.typeName();
            registeredTypeNames.add(fullyQualifiedTypeName);
        });

        typeRelations.forEach(typeRelation -> {
            // toTypeの完全修飾型名を作成
            String toFullyQualifiedTypeName = typeRelation.toTypeFullyQualifiedScope().isEmpty()
                    ? typeRelation.toType().asString()
                    : typeRelation.toTypeFullyQualifiedScope() + "."
                            + typeRelation.toType().asString();

            // toTypeがインタフェースであるかどうかを判定
            Boolean isInterface = false;
            if (typeRelation.relationType() == IMPLEMENTATION) {
                // 実装関係の場合、toTypeはインタフェースであるため、isInterfaceをtrueに設定する
                isInterface = true;
            } else if (typeRelation.relationType() == INHERITANCE) {
                // 継承関係の場合、fromTypeがインタフェースであればtoTypeもインタフェースである
                TypeInfo fromTypeInfo = findTypeInfo(typeInfos, typeRelation.fromTypeFullyQualifiedScope(),
                        typeRelation.fromType().asString());
                if (fromTypeInfo != null && fromTypeInfo.isInterface()) {
                    isInterface = true;
                }
            }

            // toTypeが未登録の場合、型情報を生成して追加
            if (!registeredTypeNames.contains(toFullyQualifiedTypeName)) {
                typeInfos.add(
                        new TypeInfo(typeRelation.toTypeFullyQualifiedScope(), typeRelation.toType().asString(), null,
                                isInterface, typeRelation.isLocalType()));
                registeredTypeNames.add(toFullyQualifiedTypeName);
            }
        });
        return;
    }

    /**
     * 収集した型宣言情報から型情報を生成して応答する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return 型情報
     */
    private TypeInfo createTypeInfo(TypeDeclaration<?> typeDeclaration) {
        // 型宣言情報から型情報を生成する
        String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        // インタフェースであるかどうかを判定
        Boolean isInterface = false;
        if (typeDeclaration instanceof ClassOrInterfaceDeclaration classOrInterface
                && classOrInterface.isInterface()) {
            isInterface = true;
        }

        // ローカル型であるかどうかを判定
        Boolean isLocalType = false;
        if (typeDeclaration.getParentNode().isPresent()) {
            if (typeDeclaration.getParentNode().get() instanceof Statement) {
                isLocalType = true;
            }
        }

        TypeInfo typeInfo = new TypeInfo(fullyQualifiedScope, typeDeclaration.getNameAsString(), typeDeclaration,
                isInterface, isLocalType);

        return typeInfo;
    }

    /**
     * 指定された完全修飾スコープ名と型名に一致する型情報を型情報の集合から検索して応答する。
     * 
     * @param typeInfos           型情報の集合
     * @param fullyQualifiedScope 完全修飾スコープ名
     * @param typeName            型名
     * @return 一致する型情報(存在しない場合はnull)
     */
    private TypeInfo findTypeInfo(Set<TypeInfo> typeInfos, String fullyQualifiedScope, String typeName) {
        for (TypeInfo typeInfo : typeInfos) {
            if (typeInfo.fullyQualifiedScope().equals(fullyQualifiedScope)
                    && typeInfo.typeName().equals(typeName)) {
                return typeInfo;
            }
        }
        return null;
    }
}
