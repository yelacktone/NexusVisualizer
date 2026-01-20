package nexusviz.generator.result;

import java.util.Set;

import nexusviz.generator.model.structure.TypeInfo;
import nexusviz.generator.model.structure.TypeRelationInfo;

/**
 * クラス構造解析の結果を表すレコード。
 * 
 * @param typeInfos     型情報の集合
 * @param typeRelations 型関係の集合
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record StructuralAnalysisResult(Set<TypeInfo> typeInfos,
		Set<TypeRelationInfo> typeRelations) {
}
