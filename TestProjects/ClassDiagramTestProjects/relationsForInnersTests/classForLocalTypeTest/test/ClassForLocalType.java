package test;

import java.util.Map;

/**
 * 通常クラスとローカル型の関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ClassForLocalType {
    /**
     * ローカル型を定義するメソッド
     */
    void method() {
        /**
         * メソッド内のローカルクラス
         */
        class LocalClassInMethod {
        }

        /**
         * メソッド内のファイナルローカルクラス
         */
        final class FinalLocalClassInMethod {
        }

        /**
         * メソッド内の抽象ローカルクラス
         */
        abstract class AbstractLocalClassInMethod {
        }

        /**
         * メソッド内のローカルインタフェース
         */
        interface LocalInterfaceInMethod {
        }

        /**
         * メソッド内の抽象ローカルインタフェース
         */
        abstract interface AbstractLocalInterfaceInMethod {
        }

        // /**
        // * メソッド内のローカル列挙型
        // */
        // enum LocalEnumInMethod {
        // }

        /**
         * メソッド内のローカルレコード
         */
        record LocalRecordInMethod() {
        }

        /**
         * メソッド内のファイナルローカルレコード
         */
        final record FinalLocalRecordInMethod() {
        }
    }

    /**
     * オーバーロードメソッド
     */
    void method(String aString) {
        /**
         * オーバーロードメソッド内の同名クラス
         */
        class LocalClassInMethod {
            /**
             * 異なる型として扱われているかを確認するためのフィールド
             */
            String aString;
        }
    }

    /**
     * ローカル型を定義するためのコンストラクタ
     */
    ClassForLocalType() {
        /**
         * コンストラクタ内のローカルクラス
         */
        class LocalClassInConstructor {
        }
    }

    /**
     * オーバーロードコンストラクタ
     */
    ClassForLocalType(String aString) {
        /**
         * オーバーロードコンストラクタ内の同名クラス
         */
        class LocalClassInConstructor {
            /**
             * 異なる型として扱われているかを確認するためのフィールド
             */
            String aString;
        }
    }

    /**
     * ローカル型を定義するための初期化ブロック
     */
    {
        /**
         * 初期化ブロック内のローカルクラス
         */
        class LocalClassInInitBlock {
        }
    }

    /**
     * 同名ローカル型でも異なる型として扱われるか確認するための初期化ブロック
     */
    {
        /**
         * 初期化ブロック内のローカルクラス
         */
        class LocalClassInInitBlock {
            String aString;
        }
    }

    /**
     * ローカル型を定義するためのスタティック初期化ブロック
     */
    static {
        /**
         * スタティックブロック内のローカルクラス
         */
        class LocalClassInStaticInitBlock {
        }
    }

    /**
     * 同名ローカル型でも異なる方として扱われるか確認するためのスタティック初期化ブロック
     */
    static {
        /**
         * スタティックブロック内のローカルクラス
         */
        class LocalClassInStaticInitBlock {
            String aString;
        }
    }
}
