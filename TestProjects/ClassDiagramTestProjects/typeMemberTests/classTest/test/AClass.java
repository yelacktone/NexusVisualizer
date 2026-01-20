package test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * フィールド、メソッド、コンストラクタが正しく出力されるかのテスト
 * 型に共通する要素すべて
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class AClass<T> {
    /**
     * 通常型フィールド
     * パブリック・スタティック・ファイナル・初期化あり
     */
    public static final Integer aPublicStaticFinalIntegerField = 0;

    /**
     * フィールド
     * 複数同時宣言
     */
    public int a, b = 0, c = 1;

    /**
     * ジェネリクス型フィールド
     * プロテクテッド・スタティック・初期化あり
     */
    protected static Map<String, Integer> aProtectedStaticMapField = null;

    /**
     * ネストしたジェネリクス型フィールド
     * プライベート・ファイナル・初期化あり
     */
    private final Map<String, Map<String, Integer>> aPrivateFinalNestedMapField = new HashMap<>();

    /**
     * 深くネストしたジェネリクス型フィールド
     * 初期化なし
     */
    Map<String, Map<String, Set<Integer>>> aDeeplyNestedMapField;

    /**
     * プリミティブ型フィールド
     * 初期化あり
     */
    int anIntField = 0;

    /**
     * 配列型フィールド
     * 初期化なし
     */
    String[] aStringArrayField;

    /**
     * 多次元配列型フィールド
     * 初期化なし
     */
    Double[][] aDoubleMultiArrayField;

    /**
     * ジェネリクス型変数を持つ配列型フィールド
     * 初期化なし
     */
    Set<String>[] aSetArrayField;

    /**
     * 深くネストしたジェネリクス型変数を持つ多次元配列型フィールド
     * 初期化なし
     */
    Map<String, Map<String, Set<Integer>>>[][] aDeeplyNestedSetMultiArrayField;

    /**
     * 型変数を使用したフィールド
     */
    T aClassTypeField;

    /**
     * 非推奨アノテーション付きフィールド
     */
    @Deprecated
    int aFieldWithAnnotation;

    /**
     * コンストラクタ
     * パブリック
     */
    public AClass() {
        return;
    }

    /**
     * コンストラクタ
     * プロテクティッド
     * 
     * @param arg int型
     */
    protected AClass(int arg) {
        return;
    }

    /**
     * コンストラクタ
     * プライベート
     * 
     * @param anInteger  Integer型
     * @param aNestedMap 二重ネストジェネリクス
     */
    private AClass(Integer anInteger, Map<String, Set<Integer>> aNestedMap) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param aDeeplyNestedMap 三重ネストジェネリクス
     * @param anInt            int型
     * @param aString          String型
     */
    AClass(Map<String, Map<String, Set<Integer>>> aDeeplyNestedMap, int anInt, String aString) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param integerArray Integer型配列
     */
    AClass(Integer[] integerArray) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param integerMultiArray Integer型二次元配列
     */
    AClass(Integer[][] integerMultiArray) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param setArray ジェネリクス配列
     */
    AClass(Set<String>[] setArray) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param aDeeplyNestedSetMultiArray 三重ネストジェネリクスの二次元配列
     */
    AClass(Map<String, Map<String, Set<Integer>>>[][] aDeeplyNestedSetMultiArray) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param varArgStrings String型の可変長引数
     */
    AClass(String... varArgStrings) {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param input     コンストラクタレベルの型変数
     * @param anInteger 整数型
     */
    <U> AClass(U input, Integer anInteger) {
        return;
    }

    /**
     * コンストラクタ
     * 非推奨アノテーション付きコンストラクタ
     * 
     * @param string 文字列
     */
    @Deprecated
    AClass(String string) {
        return;
    }

    /**
     * コンストラクタ
     * 例外を投げるコンストラクタ
     * 
     * @param integer 整数型
     * @throws Exception 例外
     */
    AClass(Integer integer) throws Exception {
        return;
    }

    /**
     * コンストラクタ
     * 
     * @param genericParam 型変数
     */
    AClass(T genericParam) {
        return;
    }

    /**
     * メソッド
     * パブリック・スタティック・ファイナル
     */
    public static final void aPublicStaticFinalMethodWithoutArguments() {
        return;
    }

    /**
     * メソッド
     * プロテクティッド・スタティック
     * 
     * @param aString String型
     * @return int型
     */
    protected static int aProtectedStaticMethodWithASingleArgument(String aString) {
        return 0;
    }

    /**
     * メソッド
     * プライベート・ファイナル
     * 
     * @param anInteger Integer型
     * @param aMap      通常マップ
     * @return 通常マップ
     */
    private final Map<String, Integer> aPrivateFinalMethodWithMultipleArguments(
            Integer anInteger, Map<String, Integer> aMap) {
        return null;
    }

    /**
     * メソッド
     * 
     * @param aDeeplyNestedMap 三重ネストジェネリクス
     * @param anInt            int型
     * @param aString          String型
     * @return 三重ネストジェネリクス
     */
    Map<String, Map<String, Set<Integer>>> aMethodWithMultipleArguments(
            Map<String, Map<String, Set<Integer>>> aDeeplyNestedMap, int anInt, String aString) {
        return aDeeplyNestedMap;
    }

    /**
     * メソッド
     * 
     * @param integerArray Integer型配列
     * @return Integer型配列
     */
    Integer[] aMethodWithArrayArgument(Integer[] integerArray) {
        return integerArray;
    }

    /**
     * メソッド
     * 
     * @param integerMultiArray Integer型二次元配列
     * @return Integer型二次元配列
     */
    Integer[][] aMethodWithMultiArrayArgument(Integer[][] integerMultiArray) {
        return integerMultiArray;
    }

    /**
     * メソッド
     * 
     * @param setArray ジェネリクス配列
     * @return ジェネリクス配列
     */
    Set<String>[] aMethodWithGenericArrayArgument(Set<String>[] setArray) {
        return setArray;
    }

    /**
     * メソッド
     * 
     * @param aDeeplyNestedMultiArray 三重ネストジェネリクスの二次元配列
     * @return 三重ネストジェネリクスの二次元配列
     */
    Map<String, Map<String, Set<Integer>>>[][] aMethodWithDeeplyNestedMultiMapArrayArgument(
            Map<String, Map<String, Set<Integer>>>[][] aDeeplyNestedMultiMapArray) {
        return aDeeplyNestedMultiMapArray;
    }

    /**
     * メソッド
     * 
     * @param varArgString String型の可変長引数
     * @return String型配列
     */
    String[] aMethodWithVarargsArgument(String... varArgStrings) {
        return varArgStrings;
    }

    /**
     * メソッド
     * 
     * @param input メソッドレベルの型変数
     * @return メソッドレベルの型変数
     */
    <U> U aMethodWithMethodLevelGeneric(U input) {
        return input;
    }

    /**
     * メソッド
     * 例外を投げるメソッド
     * 
     * @throws Exception 例外
     */
    void aMethodThrowsException() throws Exception {
        throw new Exception();
    }

    /**
     * 非推奨アノテーション付きメソッド
     */
    @Deprecated
    void aMethodWithAnnotation() {
        return;
    }

    /**
     * メソッド
     * 
     * @param genericParam クラスの型変数
     * @return クラスの型変数
     */
    T aMethodReturningClassTypeParameter(T genericParam) {
        return genericParam;
    }
}
