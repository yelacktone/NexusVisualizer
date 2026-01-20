package nexusviz.generator.analyzer;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import nexusviz.generator.util.TypeUtils;

/**
 * 多重度の解析を行うクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class MultiplicityAnalyzer extends Object {
    /**
     * インスタンス化を防止するためのコンストラクタ。
     */
    private MultiplicityAnalyzer() {
    }

    /**
     * 引数の型を解析して多重度情報を返す。
     * 
     * @param type 型情報
     * @return 型と多重度情報のマップ
     */
    public static Map<Type, Boolean> analyze(Type type) {
        Map<Type, Boolean> result = new LinkedHashMap<>();

        if (type.isArrayType()) { // 配列型の場合
            Type baseType = getInnermostComponentType(type);

            // 配列の最も内側の要素型を多重度ありで登録
            result.put(baseType, true);

            // 配列の中身がジェネリクスなら再帰的に解析する
            if (TypeUtils.isGenerics(baseType)) { // ジェネリクス型の場合
                // 最も外側の型を多重度ありで登録
                result.putAll(getGenericsTypeRecursive(baseType, result, true));
            }
        } else if (TypeUtils.isGenerics(type)) { // ジェネリクス型の場合
            // 最も外側の型を多重度なしで登録
            result.putAll(getGenericsTypeRecursive(type, result, false));
        } else if (!type.isPrimitiveType() && !type.isVarType() && type.isClassOrInterfaceType()) { // 通常型の場合
            result.put(type, false);
        }
        return result;
    }

    /**
     * ジェネリクス型を再帰的に解析して多重度情報を応答する。
     * 
     * @param type        型情報
     * @param types       型と多重度情報のマップ
     * @param isInnerType 内部型であればtrue、最外部型であればfalse
     * @return 型と多重度情報のマップ
     */
    public static Map<Type, Boolean> getGenericsTypeRecursive(Type type, Map<Type, Boolean> types, Boolean isInnerType) {
        if (TypeUtils.isGenerics(type)) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();

            // ジェネリクス型そのものを登録
            // 最も外側なら多重度なし，配列型・ジェネリクス型の中身なら多重度あり
            types.put(type, isInnerType);

            // 中身を再帰的に解析
            classType.getTypeArguments().ifPresent(typeArgs -> {
                typeArgs.forEach(typeArg -> {
                    types.putAll(getGenericsTypeRecursive(typeArg, types, true));
                });
            });
        } else if (!type.isPrimitiveType() && !type.isVarType() && type.isClassOrInterfaceType()) { // 通常型の場合
            types.put(type, isInnerType);
        }
        return types;
    }

    /**
     * 配列型の最も内側の要素型を応答する。
     * 
     * @param type 型情報
     * @return 最も内側の要素型
     */
    public static Type getInnermostComponentType(Type type) {
        while (type.isArrayType()) {
            type = type.asArrayType().getComponentType();
        }
        return type;
    }
}
