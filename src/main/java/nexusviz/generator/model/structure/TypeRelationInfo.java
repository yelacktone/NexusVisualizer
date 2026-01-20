package nexusviz.generator.model.structure;

import com.github.javaparser.ast.type.Type;

/**
 * 関係情報を表すレコード。
 * 
 * @param fromType                    参照元の型
 * @param fromTypeFullyQualifiedScope 参照元の型の完全修飾スコープ
 * @param toType                      参照先の型
 * @param toTypeFullyQualifiedScope   参照先の型の完全修飾スコープ
 * @param relationType                関係の種類
 * @param isLocalType                 ローカル型であるかどうか
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record TypeRelationInfo(Type fromType, String fromTypeFullyQualifiedScope, Type toType,
        String toTypeFullyQualifiedScope, RelationType relationType, Boolean isLocalType) {
}
