package nexusviz.generator.model.dependency;

/**
 * フィールドアクセスの種類を表す列挙型。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public enum AccessType {
    /** 読み取りアクセス */ READ,
    /** 書き込みアクセス */ WRITE,
    /** その他のアクセス */ OTHER
}
