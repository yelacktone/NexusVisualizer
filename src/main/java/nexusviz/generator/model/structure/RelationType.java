package nexusviz.generator.model.structure;

/**
 * 関係の種類を表す列挙型。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public enum RelationType {
    /** 継承 */ INHERITANCE,
    /** 実装 */ IMPLEMENTATION,
    /** 片方向関連 */ UNIDIRECTIONAL_ASSOCIATION,
    /** 双方向関連 */ BIDIRECTIONAL_ASSOCIATION,
    /** 多重片方向関連 */ MULTIPLICITY_UNIDIRECTIONAL_ASSOCIATION,
    /** 依存 */ DEPENDENCY,
    /** 集約 */ AGGREGATION,
    /** 合成 */ COMPOSITION,
    /** 包含 */ CONTAINMENT
}
