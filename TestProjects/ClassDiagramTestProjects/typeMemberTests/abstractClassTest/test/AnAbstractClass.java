package test;

import java.util.Map;
import java.util.Set;

/**
 * フィールド、メソッド、コンストラクタが正しく出力されるかのテスト
 * 抽象クラス特有の要素のみ
 * 
 * @author Ishiguro
 * @version 1.0
 */
public abstract class AnAbstractClass<T> {
    /**
     * フィールド
     * パブリック・初期化なし
     */
    public String aPublicStringField;

    /**
     * コンストラクタ
     * パブリック・引数なし
     */
    public AnAbstractClass() {
        return;
    }

    /**
     * 抽象メソッド
     * パブリック
     * 
     * @return Integer型
     */
    public abstract Integer aPublicAbstractMethod();

    /**
     * 抽象メソッド
     * プロテクテッド
     * 
     * @param aString String型
     */
    protected abstract void aProtectedAbstractMethodWithASingleArgument(String aString);

    /**
     * 抽象メソッド
     * 複数引数・深くネストしたジェネリクス型引数あり・深くネストしたジェネリクス型戻り値
     * 
     * @param aDeeplyNestedMap 三重ネストのジェネリクス
     * @param anInt            int型
     * @param aString          String型
     * @return 三重ネストのジェネリクス
     */
    abstract Map<String, Map<String, Set<Integer>>> anAbstractMethodWithMultipleArguments(
            Map<String, Map<String, Set<Integer>>> aDeeplyNestedMap, int anInt, String aString);

    /**
     * 抽象メソッド
     * 
     * @return 型変数
     */
    abstract T aGenericAbstractMethod();

    /**
     * 通常メソッド
     */
    void aMethodWithoutArguments() {
        return;
    }
}
