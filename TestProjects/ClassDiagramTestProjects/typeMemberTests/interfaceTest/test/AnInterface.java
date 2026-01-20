package test;

import java.util.Map;
import java.util.Set;

/**
 * フィールド、メソッドが正しく出力されるかのテスト
 * インタフェース特有の要素のみ
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public interface AnInterface {
    /**
     * フィールド
     * 暗黙パブリック・暗黙スタティック・暗黙ファイナル
     */
    int AN_IMPLICIT_CONSTANT_INT_FIELD = 100;

    /**
     * 抽象メソッド
     * 暗黙パブリック・暗黙アブストラクト
     * 
     * @return String型
     */
    String aImplicitMethod();

    /**
     * 通常メソッド
     * スタティック・暗黙パブリック
     * 
     * @param aDeeplyNestedMap 三重ネストジェネリクス
     * @return 三重ネストジェネリクス
     */
    static Map<String, Map<String, Set<Integer>>> aStaticMethodWithDeeplyNestedGenerics(
            Map<String, Map<String, Set<Integer>>> aDeeplyNestedMap) {
        return aDeeplyNestedMap;
    }

    /**
     * 通常メソッド
     * デフォルト・暗黙パブリック
     */
    default void aDefaultMethod() {
        return;
    }

    /**
     * 通常メソッド
     * プライベート
     */
    private void aPrivateConcreteMethod() {
        return;
    }

    /**
     * 通常メソッド
     * プライベート・スタティック
     */
    private static void aPrivateStaticMethod() {
        return;
    }
}
