package nexusviz.generator.model.dependency;

import java.util.Map;

/**
 * メソッドの呼び出し情報を表すレコード。
 * 
 * @param declaringTypeName メソッドを定義している型の名前
 * @param methodName        メソッド名
 * @param parameters        パラメータ名と型名のマップ
 * @param returnTypeName    戻り値の型
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record CalleeMethodInfo(String declaringTypeName, String methodName, Map<String, String> parameters,
		String returnTypeName) {
}
