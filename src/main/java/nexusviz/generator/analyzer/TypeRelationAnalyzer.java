package nexusviz.generator.analyzer;

import static nexusviz.generator.model.structure.RelationType.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import nexusviz.generator.model.structure.TypeInfo;
import nexusviz.generator.model.structure.TypeRelationInfo;
import nexusviz.generator.util.TypeUtils;

/**
 * 型同士の関係情報の解析を行うクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class TypeRelationAnalyzer extends Object {
    /**
     * java.langパッケージの型の集合を束縛する。
     */
    private static final Set<String> FREQUENT_JAVA_LANG_TYPES = Set.of("Byte", "Short", "Integer", "Long", "Float",
            "Double", "Character", "String", "Boolean");

    /**
     * ローカル型であることを示す定数。
     */
    private static final Boolean LOCAL_TYPE = true;

    /**
     * ローカル型でないことを示す定数。
     */
    private static final Boolean NOT_LOCAL_TYPE = false;

    /**
     * インスタンス化を防止するためのコンストラクタ。
     */
    private TypeRelationAnalyzer() {
    }

    /**
     * 引数で受け取った型情報の集合をもとに、関係情報を解析する。
     * 
     * @param typeInfos 型情報の集合
     * @return 関係情報の集合
     */
    public static Set<TypeRelationInfo> analyze(Set<TypeInfo> typeInfos) {
        Set<TypeRelationInfo> typeRelations = new LinkedHashSet<>();

        // 各型宣言情報に対して関係情報を解析する
        typeInfos.forEach(typeInfo -> {
            TypeDeclaration<?> type = typeInfo.typeDeclaration();
            if (type instanceof ClassOrInterfaceDeclaration) { // クラスもしくはインタフェースの場合
                typeRelations.addAll(analyzeClassOrInterfaceRelations(type));
            } else if (type instanceof EnumDeclaration) { // 列挙型の場合
                typeRelations.addAll(analyzeEnumRelations(type));
            } else if (type instanceof RecordDeclaration) { // レコードの場合
                typeRelations.addAll(analyzeRecordRelations(type));
            }
        });

        // 同一クラスに対する片方向関連と多重片方向関連を検出し、片方向関連を除外する
        typeRelations.removeAll(detectMultiplicityUnidirectionalRelations(typeRelations));

        // 継承関係と実装関係において、TypeRelationInfoのtoTypeの名前とTypeInfoのtypeNameが一致する場合、同一型とみなし、TypeRelationInfoのfullyQualifiedScopeをTypeInfoのfullyQualifiedScopeと一致させる
        Set<TypeRelationInfo> replacedTypeRelations = detectDuplicatedToTypeTypeRelationInfo(typeInfos, typeRelations);

        // 双方向関連を検出・統合したものを応答する
        return mergeToBidirectionalRelations(replacedTypeRelations);
    }

    /**
     * 型宣言情報に基づいて参照関係を解析する。
     * 
     * @param fromTypeFullyQualifiedScope 参照元の型の完全修飾スコープ名
     * @param fromType                    参照元の型情報
     * @param toType                      参照先の型情報
     * @return 参照関係の集合
     */
    private static Set<TypeRelationInfo> analyzeAssociations(String fromTypeFullyQualifiedScope,
            Type fromType,
            Type toType) {
        Set<TypeRelationInfo> typeRelations = new LinkedHashSet<>();
        Map<Type, Boolean> multiplicities = MultiplicityAnalyzer.analyze(toType);

        multiplicities.forEach((aType, isMultiple) -> {
            if (!aType.isPrimitiveType()) { // プリミティブ型以外の場合
                if (FREQUENT_JAVA_LANG_TYPES.contains(aType.asString())) { // java.langパッケージの型の場合
                    String toTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(aType);
                    typeRelations.add(
                            new TypeRelationInfo(null, null, aType, toTypeFullyQualifiedScope, null, NOT_LOCAL_TYPE));
                } else if (TypeUtils.isGenerics(aType)) { // ジェネリクス型の場合
                    Type outerType = TypeUtils.getOuterGenericType(aType);
                    String outerTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(aType);
                    if (isMultiple) { // 多重度ありの場合
                        typeRelations.add(
                                new TypeRelationInfo(fromType, fromTypeFullyQualifiedScope, outerType,
                                        outerTypeFullyQualifiedScope,
                                        MULTIPLICITY_UNIDIRECTIONAL_ASSOCIATION, NOT_LOCAL_TYPE));
                    } else { // 多重度なしの場合
                        typeRelations.add(new TypeRelationInfo(fromType, fromTypeFullyQualifiedScope, outerType,
                                outerTypeFullyQualifiedScope, UNIDIRECTIONAL_ASSOCIATION, NOT_LOCAL_TYPE));
                    }
                } else { // それ以外の方の場合
                    String toTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(aType);
                    if (isMultiple) { // 多重度ありの場合
                        typeRelations
                                .add(new TypeRelationInfo(fromType, fromTypeFullyQualifiedScope, aType,
                                        toTypeFullyQualifiedScope,
                                        MULTIPLICITY_UNIDIRECTIONAL_ASSOCIATION, NOT_LOCAL_TYPE));
                    } else { // 多重度なしの場合
                        typeRelations.add(new TypeRelationInfo(fromType, fromTypeFullyQualifiedScope, aType,
                                toTypeFullyQualifiedScope,
                                UNIDIRECTIONAL_ASSOCIATION, NOT_LOCAL_TYPE));
                    }
                }
            }
        });
        return typeRelations;
    }

    /**
     * クラスもしくはインタフェース型の関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return クラスの関係情報の集合
     */
    private static Set<TypeRelationInfo> analyzeClassOrInterfaceRelations(TypeDeclaration<?> typeDeclaration) {
        Set<TypeRelationInfo> classRelations = new LinkedHashSet<>();
        ClassOrInterfaceDeclaration aClass = typeDeclaration.asClassOrInterfaceDeclaration();

        // 継承
        classRelations.addAll(analyzeInheritance(typeDeclaration, aClass.getExtendedTypes()));

        // 実装
        classRelations.addAll(analyzeImplementations(typeDeclaration, aClass.getImplementedTypes()));

        // フィールド参照(片方向関連)
        classRelations.addAll(analyzeFieldRelations(typeDeclaration, aClass.getFields()));

        // インナークラスの関係（合成・集約・包含）
        classRelations.addAll(analyzeInnerRelation(typeDeclaration));

        return classRelations;
    }

    /**
     * 列挙型の関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return 列挙型の関係情報の集合
     */
    private static Set<TypeRelationInfo> analyzeEnumRelations(TypeDeclaration<?> typeDeclaration) {
        Set<TypeRelationInfo> enumRelations = new LinkedHashSet<>();
        EnumDeclaration anEnum = typeDeclaration.asEnumDeclaration();

        // 実装
        enumRelations.addAll(analyzeImplementations(typeDeclaration, anEnum.getImplementedTypes()));

        // フィールド参照(片方向関連)
        enumRelations.addAll(analyzeFieldRelations(typeDeclaration, anEnum.getFields()));

        // インナークラスの関係（合成・集約・包含）
        enumRelations.addAll(analyzeInnerRelation(typeDeclaration));

        return enumRelations;
    }

    /**
     * フィールド参照関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @param fields          フィールド情報のリスト
     * @return フィールド参照関係の集合
     */
    private static Set<TypeRelationInfo> analyzeFieldRelations(TypeDeclaration<?> typeDeclaration,
            List<FieldDeclaration> fields) {
        Set<TypeRelationInfo> fieldRelations = new LinkedHashSet<>();
        Type aType = StaticJavaParser.parseType(typeDeclaration.getNameAsString());
        String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        fields.forEach(field -> {
            field.getVariables().forEach(variable -> {
                Type fieldType = variable.getType();
                fieldRelations.addAll(analyzeAssociations(fullyQualifiedScope, aType, fieldType));
            });
        });
        return fieldRelations;
    }

    /**
     * 実装関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @param interfaces      インタフェース情報のリスト
     * @return 実装関係の集合
     */
    private static Set<TypeRelationInfo> analyzeImplementations(TypeDeclaration<?> typeDeclaration,
            List<ClassOrInterfaceType> interfaces) {
        Set<TypeRelationInfo> implementations = new LinkedHashSet<>();
        Type aType = StaticJavaParser.parseType(typeDeclaration.getNameAsString());
        String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        interfaces.forEach(anInterface -> {
            String interfaceFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(anInterface);
            implementations
                    .add(new TypeRelationInfo(aType, fullyQualifiedScope, anInterface, interfaceFullyQualifiedScope,
                            IMPLEMENTATION, NOT_LOCAL_TYPE));

            if (TypeUtils.isGenerics(anInterface)) { // ジェネリクス型の場合
                Type outerGenericType = TypeUtils.getOuterGenericType(anInterface);
                String outerGenericTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(anInterface);
                implementations.add(new TypeRelationInfo(anInterface, interfaceFullyQualifiedScope, outerGenericType,
                        outerGenericTypeFullyQualifiedScope, IMPLEMENTATION, NOT_LOCAL_TYPE));
            }
        });
        return implementations;
    }

    /**
     * 継承関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @param superClasses    親クラス情報のリスト
     * @return 継承関係の集合
     */
    private static Set<TypeRelationInfo> analyzeInheritance(TypeDeclaration<?> typeDeclaration,
            List<ClassOrInterfaceType> superClasses) {
        Set<TypeRelationInfo> inheritances = new LinkedHashSet<>();
        Type aType = StaticJavaParser.parseType(typeDeclaration.getNameAsString());
        String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        superClasses.forEach(superClass -> {
            String superClassFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(superClass);
            inheritances.add(
                    new TypeRelationInfo(aType, fullyQualifiedScope, superClass, superClassFullyQualifiedScope,
                            INHERITANCE, NOT_LOCAL_TYPE));

            if (TypeUtils.isGenerics(superClass)) { // ジェネリクス型の場合
                Type outerGenericType = TypeUtils.getOuterGenericType(superClass);
                String outerGenericTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(superClass);
                inheritances.add(new TypeRelationInfo(superClass, superClassFullyQualifiedScope, outerGenericType,
                        outerGenericTypeFullyQualifiedScope, INHERITANCE, NOT_LOCAL_TYPE));
            }
        });
        return inheritances;
    }

    /**
     * インナークラスの関係（合成・集約・包含）を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return インナークラスの関係情報の集合
     */
    private static Set<TypeRelationInfo> analyzeInnerRelation(TypeDeclaration<?> typeDeclaration) {
        Set<TypeRelationInfo> innerRelations = new LinkedHashSet<>();
        Type innerType = StaticJavaParser.parseType(typeDeclaration.getNameAsString());
        String innerTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        // 親型が存在し、かつそれが型宣言情報の場合、インナークラスの関係（合成・集約・包含）を追加
        typeDeclaration.getParentNode().ifPresent(parent -> {
            // 親がコンパイルユニット、つまり、トップレベルクラスの場合は無視
            if (parent instanceof CompilationUnit) {
                return;
            }

            // 親が型宣言情報の場合、インナークラスの関係を追加
            else if (parent instanceof TypeDeclaration<?> outerTypeDeclaration) {
                innerRelations.add(createInnerTypeRelationInfo(typeDeclaration, innerType,
                        innerTypeFullyQualifiedScope, outerTypeDeclaration, NOT_LOCAL_TYPE));
                return;
            }

            // 親がステートメントの場合、型宣言情報が来るまで遡り、インナークラスの関係を追加
            else if (parent instanceof Statement) {
                Node currentNode = parent;
                TypeDeclaration<?> outerTypeDeclaration = null;

                while (currentNode.getParentNode().isPresent()) {
                    currentNode = currentNode.getParentNode().get();

                    if (currentNode instanceof TypeDeclaration<?> typeDecl) {
                        outerTypeDeclaration = typeDecl;
                        break;
                    }
                }

                // トップレベルクラスまで遡っても型宣言情報が見つからなかった場合は無視（現状はありえないはず）
                if (outerTypeDeclaration == null) {
                    return;
                }

                innerRelations.add(createInnerTypeRelationInfo(typeDeclaration, innerType,
                        innerTypeFullyQualifiedScope, outerTypeDeclaration, LOCAL_TYPE));
                return;
            }
        });
        return innerRelations;
    }

    /**
     * レコードパラメータの参照関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @param params          レコードパラメータ情報のリスト
     * @return レコードパラメータ参照関係の集合
     */
    private static Set<TypeRelationInfo> analyzeRecordParameterRelations(TypeDeclaration<?> typeDeclaration,
            NodeList<Parameter> params) {
        Set<TypeRelationInfo> paramRelations = new LinkedHashSet<>();
        Type aType = StaticJavaParser.parseType(typeDeclaration.getNameAsString());
        String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);

        params.forEach(param -> {
            Type paramType = param.getType();
            paramRelations.addAll(analyzeAssociations(fullyQualifiedScope, aType, paramType));
        });
        return paramRelations;
    }

    /**
     * レコードの関係を解析する。
     * 
     * @param typeDeclaration 型宣言情報
     * @return レコードの関係情報の集合
     */
    private static Set<TypeRelationInfo> analyzeRecordRelations(TypeDeclaration<?> typeDeclaration) {
        Set<TypeRelationInfo> recordRelations = new LinkedHashSet<>();
        RecordDeclaration aRecord = typeDeclaration.asRecordDeclaration();

        // 実装
        recordRelations.addAll(analyzeImplementations(typeDeclaration, aRecord.getImplementedTypes()));

        // レコードパラメータ参照(片方向関連)
        recordRelations.addAll(analyzeRecordParameterRelations(typeDeclaration, aRecord.getParameters()));

        // フィールド参照(片方向関連)
        recordRelations.addAll(analyzeFieldRelations(typeDeclaration, aRecord.getFields()));

        // インナークラスの関係（合成・集約・包含）
        recordRelations.addAll(analyzeInnerRelation(typeDeclaration));

        return recordRelations;
    }

    /**
     * インナークラスの関係情報を生成する。
     * 
     * @param typeDeclaration              インナークラスの型宣言情報
     * @param innerType                    インナークラスの型情報
     * @param innerTypeFullyQualifiedScope インナークラスの完全修飾スコープ名
     * @param outerTypeDeclaration         アウタークラスの型宣言情報
     * @param isLocalType                  ローカル型であるかどうか
     * @return インナークラスの関係情報
     */
    private static TypeRelationInfo createInnerTypeRelationInfo(TypeDeclaration<?> typeDeclaration, Type innerType,
            String innerTypeFullyQualifiedScope, TypeDeclaration<?> outerTypeDeclaration, Boolean isLocalType) {
        Type outerType = StaticJavaParser.parseType(outerTypeDeclaration.getNameAsString());

        // インナークラスがstaticであるかどうかを判定
        Boolean isStaticInner = isStaticInner(typeDeclaration, outerTypeDeclaration);

        // アウタータイプの完全修飾スコープ名を取得
        String outerTypeFullyQualifiedScope = TypeUtils.getFullyQualifiedScope(outerTypeDeclaration);

        // インナークラスがローカルクラスの場合は包含、staticの場合は集約、非staticの場合は合成とする
        if (isLocalType) {
            return new TypeRelationInfo(
                    innerType, innerTypeFullyQualifiedScope, outerType, outerTypeFullyQualifiedScope,
                    CONTAINMENT, isLocalType);
        } else if (isStaticInner) {
            return new TypeRelationInfo(
                    innerType, innerTypeFullyQualifiedScope, outerType, outerTypeFullyQualifiedScope,
                    AGGREGATION, isLocalType);
        } else {
            return new TypeRelationInfo(
                    innerType, innerTypeFullyQualifiedScope, outerType, outerTypeFullyQualifiedScope,
                    COMPOSITION, isLocalType);
        }
    }

    /**
     * 継承と実装に関して同名型を検出し、置換済みの関係情報の集合を応答する。
     * 継承関係と実装関係において、
     * TypeRelationInfoのtoTypeの名前と
     * TypeInfoのtypeNameが一致する場合、同一型と見なす。
     * 
     * @param typeInfos     型情報の集合
     * @param typeRelations 関係情報の集合
     * @return 除外済みの関係情報の集合
     */
    private static Set<TypeRelationInfo> detectDuplicatedToTypeTypeRelationInfo(Set<TypeInfo> typeInfos,
            Set<TypeRelationInfo> typeRelations) {
        Set<TypeRelationInfo> toAdd = new LinkedHashSet<>();
        Set<TypeRelationInfo> toRemove = new HashSet<>();

        for (TypeRelationInfo typeRelation : typeRelations) {
            Boolean isInheritanceOrImplementation = (typeRelation.relationType() == INHERITANCE
                    || typeRelation.relationType() == IMPLEMENTATION);
            for (TypeInfo typeInfo : typeInfos) {
                // 継承関係または実装関係でかつ、
                // 関係情報の完全修飾スコープ名が空でかつ、
                // 関係情報の参照先型の名前と型情報の型名が同一の場合、
                // 完全修飾スコープ名にしたものを追加し、完全修飾スコープ名が空のものを削除する
                if (isInheritanceOrImplementation && typeRelation.toTypeFullyQualifiedScope().isEmpty()
                        && typeRelation.toType().toString().equals(typeInfo.typeName())) {
                    TypeRelationInfo newTypeRelationInfo = new TypeRelationInfo(typeRelation.fromType(),
                            typeRelation.fromTypeFullyQualifiedScope(), typeRelation.toType(),
                            typeInfo.fullyQualifiedScope(), typeRelation.relationType(),
                            typeRelation.isLocalType());
                    toAdd.add(newTypeRelationInfo);
                    toRemove.add(typeRelation);
                    break;
                }
            }
        }

        typeRelations.removeAll(toRemove);
        typeRelations.addAll(toAdd);

        return typeRelations;
    }

    /**
     * 引数の片方向関連から同一クラスに対する多重片方向関連を検出し、除外する片方向関連を応答する。
     * 
     * @param typeRelations 関係情報の集合
     * @return 除外する関係情報の集合
     */
    private static Set<TypeRelationInfo> detectMultiplicityUnidirectionalRelations(
            Set<TypeRelationInfo> typeRelations) {
        // 同一クラスに対する多重片方向関連の検出と統合
        Set<TypeRelationInfo> toRemove = new HashSet<>();
        typeRelations.forEach(aRelationInfo -> {
            if (aRelationInfo.relationType() == UNIDIRECTIONAL_ASSOCIATION) { // 関係タイプが片方向関連の場合
                TypeRelationInfo multiplicityRelationInfo = new TypeRelationInfo(aRelationInfo.fromType(),
                        aRelationInfo.fromTypeFullyQualifiedScope(),
                        aRelationInfo.toType(), aRelationInfo.toTypeFullyQualifiedScope(),
                        MULTIPLICITY_UNIDIRECTIONAL_ASSOCIATION, aRelationInfo.isLocalType());
                if (typeRelations.contains(multiplicityRelationInfo)
                        && !toRemove.contains(multiplicityRelationInfo)) {
                    toRemove.add(aRelationInfo);
                }
            }
        });
        return toRemove;
    }

    /**
     * インナークラスがstaticであるかどうかを判定する。
     * 
     * @param innerTypeDeclaration インナークラスの型宣言情報
     * @param outerTypeDeclaration アウタークラスの型宣言情報
     * @return staticかどうか
     */
    private static Boolean isStaticInner(TypeDeclaration<?> innerTypeDeclaration,
            TypeDeclaration<?> outerTypeDeclaration) {
        // static修飾子が付与されている場合はtrueを応答
        if (innerTypeDeclaration.isStatic()) {
            return true;
        }

        // インナー型がクラスでない場合はtrueを応答
        // インナー型がインタフェース、列挙型、レコードの場合は暗黙的にstaticであるとみなす
        if (!(innerTypeDeclaration instanceof ClassOrInterfaceDeclaration innerClass && !innerClass.isInterface())) {
            return true;
        }

        // 以下、メンバクラスの場合の判定

        // アウター型がインタフェースの場合はtrueを応答
        // 暗黙的にstaticであるとみなす
        if (outerTypeDeclaration instanceof ClassOrInterfaceDeclaration parent && parent.isInterface()) {
            return true;
        }

        // それ以外の場合はfalseを応答
        // static修飾子がない、アウター型がインタフェースでないメンバクラスの場合
        return false;
    }

    /**
     * 引数の片方向関連から双方向関連を検出・統合したものを応答する。
     * 
     * @param typeRelations 関係情報の集合
     * @return 最終的な関係情報の集合
     */
    private static Set<TypeRelationInfo> mergeToBidirectionalRelations(
            Set<TypeRelationInfo> typeRelations) {
        // 双方向関連の検出と統合
        Set<TypeRelationInfo> toAdd = new HashSet<>();
        Set<TypeRelationInfo> toRemove = new HashSet<>();
        typeRelations.forEach(aRelationInfo -> {
            // 関係タイプが片方向関連以外の場合スキップ
            if (aRelationInfo.relationType() != UNIDIRECTIONAL_ASSOCIATION) {
                return;
            }

            // toTypeFullyQualifiedScopeとfromTypeFullyQualifiedScopeが同じで、かつtoType.toString()とfromType.toString()が同じ場合はスキップ
            if (aRelationInfo.toTypeFullyQualifiedScope().equals(aRelationInfo.fromTypeFullyQualifiedScope())
                    && aRelationInfo.toType().toString().equals(aRelationInfo.fromType().toString())) {
                return;
            }

            TypeRelationInfo reversedRelationInfo = new TypeRelationInfo(aRelationInfo.toType(),
                    aRelationInfo.toTypeFullyQualifiedScope(), aRelationInfo.fromType(),
                    aRelationInfo.fromTypeFullyQualifiedScope(), aRelationInfo.relationType(),
                    aRelationInfo.isLocalType());
            if (typeRelations.contains(reversedRelationInfo)
                    && !toRemove.contains(reversedRelationInfo)) {
                toAdd.add(
                        new TypeRelationInfo(aRelationInfo.fromType(), aRelationInfo.fromTypeFullyQualifiedScope(),
                                aRelationInfo.toType(), aRelationInfo.toTypeFullyQualifiedScope(),
                                BIDIRECTIONAL_ASSOCIATION, aRelationInfo.isLocalType()));
                toRemove.add(aRelationInfo);
                toRemove.add(reversedRelationInfo);
            }
        });
        typeRelations.addAll(toAdd);
        typeRelations.removeAll(toRemove);
        return typeRelations;
    }
}
