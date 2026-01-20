package nexusviz.generator.result;

import java.util.Map;

import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.model.dependency.DependencyInfo;

/**
 * 依存解析の結果を表すレコード。
 * 
 * @param dependencyInfoMap 依存情報のマップ（キー：型名，バリュー：(キー：メソッドシグネチャ，バリュー：メソッドの依存情報)の二重マップ）
 * @param hasError          解析中にエラーが発生したかどうか
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record DependencyAnalysisResult(Map<String, Map<CallerMethodInfo, DependencyInfo>> dependencyInfoMap,
        Boolean hasError) {
}
