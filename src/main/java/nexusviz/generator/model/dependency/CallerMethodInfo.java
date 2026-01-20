package nexusviz.generator.model.dependency;

import java.util.Map;

/**
 * メソッド情報を表すレコード。
 * 
 * @param methodName     メソッド名
 * @param parameters     パラメータ名と型名のマップ
 * @param returnTypeName 戻り値の型
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record CallerMethodInfo(String methodName, Map<String, String> parameters, String returnTypeName) {
}
