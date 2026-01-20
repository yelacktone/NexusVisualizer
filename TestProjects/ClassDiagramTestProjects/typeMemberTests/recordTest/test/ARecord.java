package test;

import java.util.Map;
import java.util.Set;

/**
 * フィールド、メソッド、コンストラクタが正しく出力されるかのテスト
 * レコード特有の要素のみ
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public record ARecord<T>(int intComponent, String stringComponent, T genericComponent, Map<String, Integer> mapComponent,
        Map<String, Map<String, Set<Integer>>> deeplyNestedMapComponent, String[] stringArrayComponent,
        Double[][] doubleMultiArrayComponent,
        Set<String>[] setArrayComponent,
        Map<String, Map<String, Set<Integer>>>[][] deeplyNestedSetMultiArrayComponent) {
    /**
     * フィールド
     * スタティック・初期化なし
     */
    static Integer integerField;

    /**
     * フィールド
     * プライベート・スタティック・ファイナル・初期化あり
     */
    private static final String STRING_FIELD = "StringField";

    /**
     * コンストラクタ
     * パブリック・コンパクトコンストラクタ
     */
    public ARecord {
        ARecord.integerField = 0;
    }

    /**
     * コンストラクタ
     * パブリック
     * 
     * @param intComponent             int型
     * @param deeplyNestedMapComponent 三重ネストジェネリクス
     */
    public ARecord(int intComponent, Map<String, Map<String, Set<Integer>>> deeplyNestedMapComponent) {
        this(intComponent, null, null, null, deeplyNestedMapComponent, null, null, null, null);
    }

    /**
     * コンストラクタ
     * パブリック
     * 
     * @param intComponent    int型
     * @param stringComponent String型
     */
    public ARecord(int intComponent, String stringComponent) {
        this(intComponent, stringComponent, null, null, null, null, null, null, null);
    }

    /**
     * メソッド
     * プライベート
     * 
     * @return Integer型
     */
    private Integer aPrivateMethod() {
        return ARecord.integerField;
    }

    /**
     * メソッド
     * パブリック・スタティック・ファイナル
     * 
     * @param anInt int型
     * @return String型
     */
    public static final String aPublicStaticFinalMethod(int anInt) {
        return ARecord.STRING_FIELD;
    }

    /**
     * メソッド
     */
    void aMethod() {
        return;
    }

    /**
     * 明示的なアクセサメソッド
     * 
     * @return intComponent
     */
    public int intComponent() {
        return this.intComponent;
    }
}
