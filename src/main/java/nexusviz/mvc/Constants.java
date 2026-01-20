package nexusviz.mvc;

/**
 * NexusVisualizer における定数の定義を行う定数クラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class Constants extends Object {
	/**
	 * ポップアップメニューのコマンド。
	 * 依存解析を実行する。
	 */
	public static final String ANALYZE_DEPENDENCY = "AnalyzeDependency";

	/**
	 * ポップアップメニューのコマンド。
	 * クラス図の生成を実行する。
	 */
	public static final String EXECUTE_STRUCTURAL = "ExecuteStructural";

	/**
	 * ポップアップメニューのコマンド。
	 * ファイル選択ダイアログを表示する。
	 */
	public static final String SELECT_PROJECT = "SelectProject";

	/**
	 * ポップアップメニューのコマンド。
	 * メソッド選択ダイアログを表示する。
	 */
	public static final String SELECT_METHOD = "SelectMethod";

	/**
	 * ポップアップメニューのコマンド。
	 * ライブラリを選択する。
	 */
	public static final String SET_LIBRARY = "SetLibrary";

	/**
	 * ポップアップメニューのコマンド。
	 * メソッド依存図を表示する。
	 */
	public static final String GENERATE_DEPENDENCY = "GenerateDependency";

	/**
	 * プロジェクトが選択されていない場合の戻り値。
	 */
	public static final int NO_PROJECT_ERROR = 1;

	/**
	 * ライブラリが選択されていない場合の戻り値。
	 */
	public static final int NO_LIBRARY_WARNING = 2;

	/**
	 * Javaプロジェクトでない場合の戻り値。
	 */
	public static final int NOT_JAVA_PROJECT_ERROR = 3;

	/**
	 * メソッドが選択されていない場合の戻り値。
	 */
	public static final int NO_METHOD_SELECTED_ERROR = 4;

	/**
	 * 選択されたノードがメソッドでない場合の戻り値。
	 */
	public static final int SELECTED_NOT_METHOD_ERROR = 5;

	/**
	 * 依存解析が実行されていない場合の戻り値。
	 */
	public static final int NO_DEPENDENCY_ANALYSIS_ERROR = 6;

	/**
	 * 依存解析中にエラーが発生した場合の戻り値。
	 */
	public static final int DEPENDENCY_ANALYSIS_ERROR = 7;

	/**
	 * 依存解析が正常に完了した場合の戻り値。
	 */
	public static final int DEPENDENCY_ANALYSIS_SUCCESS_INFORMATION = 8;

	/**
	 * プロジェクトが選択された場合の戻り値。
	 */
	public static final int PROJECT_SELECTED_INFORMATION = 9;

	/**
	 * ライブラリがセットの戻り値。
	 */
	public static final int LIBRARY_SET_INFORMATION = 10;

	/**
	 * メソッドが選択された場合の戻り値。
	 */
	public static final int METHOD_SELECTED_INFORMATION = 11;

	/**
	 * インスタンス化を防止するためのコンストラクタ。
	 */
	private Constants() {
	}
}
