package nexusviz.generator.model.dependency;

import java.util.Map;

/**
 * メソッドの依存情報を表すレコード。
 * 
 * @param calleeMethods  呼び出されているメソッド
 * @param accessedFields アクセスされているフィールド
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record DependencyInfo(Map<CalleeMethodInfo, Integer> calleeMethods,
        Map<AccessedFieldInfo, Integer> accessedFields) {
}
