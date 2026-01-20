package nexusviz.generator.model.dependency;

/**
 * フィールドのアクセス情報を表すレコード。
 * 
 * @param declaringTypeName フィールドを定義している型の名前
 * @param fieldName         フィールド名
 * @param accessType        アクセスの種類
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record AccessedFieldInfo(String declaringTypeName, String fieldName, AccessType accessType) {
}
