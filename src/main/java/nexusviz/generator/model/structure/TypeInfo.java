package nexusviz.generator.model.structure;

import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * 型情報を表すレコード。
 * 
 * @param fullyQualifiedScope 完全修飾スコープ
 * @param typeName            型名
 * @param typeDeclaration     型宣言
 * @param isInterface         インタフェースであるかどうか
 * @param isLocalType         ローカル型であるかどうか
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record TypeInfo(String fullyQualifiedScope, String typeName, TypeDeclaration<?> typeDeclaration,
		Boolean isInterface, Boolean isLocalType) {
}
