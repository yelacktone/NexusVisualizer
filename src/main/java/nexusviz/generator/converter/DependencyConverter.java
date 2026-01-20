package nexusviz.generator.converter;

import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.model.dependency.DependencyInfo;
import nexusviz.generator.renderer.DependencyRenderer;

/**
 * 解析結果から PlantUML への変換の指示を出す。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class DependencyConverter extends Object {
	/**
	 * メソッド依存図用のPlantUMLを書き込むためのクラス。
	 */
	private DependencyRenderer dependencyRenderer;

	/**
	 * インスタンスを生成して応答するコンストラクタ。
	 */
	public DependencyConverter() {
		this.dependencyRenderer = new DependencyRenderer();
		return;
	}

	/**
	 * メソッドの依存情報をPlantUMLに変換する。
	 * 
	 * @param declaringTypeName 呼び出し元メソッドの宣言型名
	 * @param methodInfo        呼び出し元メソッドの情報
	 * @param dependencyInfo    メソッドの依存情報
	 * @return 生成した画像ファイルのパス文字列
	 */
	public String executeConversion(String declaringTypeName, CallerMethodInfo methodInfo,
			DependencyInfo dependencyInfo) {
		try {
			// 呼び出し元メソッドをPlantUMLに追加
			this.dependencyRenderer.addCallerMethod(declaringTypeName, methodInfo);

			// 呼び出し先メソッド群をPlantUMLに追加
			this.dependencyRenderer.addAllCalleeMethods(dependencyInfo.calleeMethods());

			// アクセスフィールド群をPlantUMLに追加
			this.dependencyRenderer.addAllAccessedFields(declaringTypeName, dependencyInfo.accessedFields());

			// メソッド呼び出し関係をPlantUMLに追加
			this.dependencyRenderer.addCallRelations(declaringTypeName, methodInfo, dependencyInfo.calleeMethods());

			// フィールドアクセス関係をPlantUMLに追加
			this.dependencyRenderer.addAccessRelations(declaringTypeName, methodInfo, dependencyInfo.accessedFields());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.dependencyRenderer.render(declaringTypeName, methodInfo.methodName(), methodInfo.parameters());
	}
}
