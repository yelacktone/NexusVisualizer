package nexusviz.generator.analyzer;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;

import nexusviz.generator.model.dependency.AccessType;
import nexusviz.generator.model.dependency.AccessedFieldInfo;
import nexusviz.generator.model.dependency.CalleeMethodInfo;
import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.model.dependency.DependencyInfo;
import nexusviz.generator.result.DependencyAnalysisResult;
import nexusviz.generator.util.TypeUtils;

/**
 * メソッドの依存情報の解析を行うクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class DependencyAnalyzer extends AbstractAnalyzer<DependencyAnalysisResult> {
    /**
     * 宣言されている型、メソッド情報、依存情報を保持するMapを束縛する。
     */
    private Map<String, Map<CallerMethodInfo, DependencyInfo>> dependencyInfoMap;

    /**
     * デフォルトコンストラクタ。
     */
    public DependencyAnalyzer() {
    }

    /**
     * ファイル単位の解析を行う。
     * 
     * @param aCompilationUnit コンパイルユニット
     * @param filePath         解析するファイルのパス
     */
    @Override
    protected void analyzeUnit(CompilationUnit aCompilationUnit, Path filePath) {
        try {
            // 型を解析
            aCompilationUnit.findAll(TypeDeclaration.class).forEach(aType -> {
                TypeDeclaration<?> typeDeclaration = aType;
                Map<CallerMethodInfo, DependencyInfo> dependencyInfo = new LinkedHashMap<>();
                String fullyQualifiedScope = TypeUtils.getFullyQualifiedScope(typeDeclaration);
                String typeName = typeDeclaration.getNameAsString();
                StringBuilder declaringTypeName = new StringBuilder();
                if (!fullyQualifiedScope.isEmpty()) {
                    declaringTypeName.append(fullyQualifiedScope).append(".");
                }
                declaringTypeName.append(typeName);

                // レコードの場合、コンパクトコンストラクタを解析
                if (typeDeclaration.isRecordDeclaration()) {
                    dependencyInfo.putAll(
                            analyzeCompactConstructorDependency(typeDeclaration.asRecordDeclaration()));
                }

                // コンストラクタを解析
                dependencyInfo.putAll(analyzeConstructorDependency(typeDeclaration));

                // メソッドを解析
                dependencyInfo.putAll(analyzeMethodDependency(typeDeclaration));

                // 型名をキー，依存情報のマップを値として保存
                this.dependencyInfoMap.put(declaringTypeName.toString(), dependencyInfo);
            });
        } catch (ParseProblemException e) {
            System.err.println("構文エラー: " + filePath + " - " + e.getMessage());
            handleError();
        } catch (Exception e) {
            System.err.println("予期しないエラー: " + filePath + " - " + e.getMessage());
            handleError();
        }
        return;
    }

    /**
     * 最終結果を生成して応答する。
     * 
     * @return 解析結果
     */
    @Override
    protected DependencyAnalysisResult buildResult() {
        return new DependencyAnalysisResult(this.dependencyInfoMap, this.hasError);
    }

    /**
     * 解析結果の要素を初期化する。
     */
    @Override
    protected void initializeResultElements() {
        this.dependencyInfoMap = new LinkedHashMap<>();
        return;
    }

    /**
     * 解決されたコンストラクタ呼び出し情報を追加する。
     * 
     * @param calleeConstructors      コンストラクタ呼び出し情報と呼び出し回数のマップ
     * @param resolvedConstructorDecl 解決されたコンストラクタ宣言
     * @param newExpr                 インスタンス生成式
     */
    private void addCalleeConstructorInfo(Map<CalleeMethodInfo, Integer> calleeConstructors,
            ResolvedConstructorDeclaration resolvedConstructorDecl,
            ObjectCreationExpr newExpr) {
        // 生成されている型名を取得
        String packageName = resolvedConstructorDecl.getPackageName() != null
                ? resolvedConstructorDecl.getPackageName() + "."
                : "UnknownPackage.";
        String declaringTypeName;
        try {
            declaringTypeName = resolvedConstructorDecl.declaringType().getQualifiedName();
        } catch (Exception e) {
            declaringTypeName = packageName + newExpr.getTypeAsString();
        }
        String calledConstructorName = newExpr.getTypeAsString();

        // 引数の情報を取得
        Map<String, String> parameters = new LinkedHashMap<>();
        for (Integer index = 0; index < resolvedConstructorDecl.getNumberOfParams(); index++) {
            String paramName = resolvedConstructorDecl.getParam(index).getName() != null
                    ? resolvedConstructorDecl.getParam(index).getName()
                    : "arg" + index;

            // 型名が取得できなかった場合は、呼び出し式から型名の解決を試みる
            String paramTypeName;
            try {
                paramTypeName = resolvedConstructorDecl.getParam(index).getType().describe();
            } catch (Exception e) {
                paramTypeName = resolveFallbackParameterType(newExpr.getArgument(index));
            }
            parameters.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String returnTypeName = null;

        // CalleeMethodInfoを作成
        CalleeMethodInfo calleeMethodInfo = new CalleeMethodInfo(declaringTypeName, calledConstructorName, parameters,
                returnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        calleeConstructors.put(calleeMethodInfo, calleeConstructors.getOrDefault(calleeMethodInfo, 0) + 1);

        return;
    }

    /**
     * 解決されたメソッド呼び出し情報を追加する。
     * 
     * @param calleeMethods      メソッド呼び出し情報と呼び出し回数のマップ
     * @param resolvedMethodDecl 解決されたメソッド宣言
     * @param callExpr           メソッド呼び出し式
     */
    private void addCalleeMethodInfo(Map<CalleeMethodInfo, Integer> calleeMethods,
            ResolvedMethodDeclaration resolvedMethodDecl, MethodCallExpr callExpr) {
        String declaringTypeName;
        try {
            declaringTypeName = resolvedMethodDecl.declaringType().getQualifiedName();
        } catch (Exception e) {
            String packageName = "UnknownPackage.";
            declaringTypeName = packageName + callExpr.getScope().map(Object::toString).orElse("UnknownType");
        }
        String calledMethodName = callExpr.getNameAsString();

        // 引数の情報を取得
        Map<String, String> parameters = new LinkedHashMap<>();
        for (Integer index = 0; index < resolvedMethodDecl.getNumberOfParams(); index++) {
            String paramName = resolvedMethodDecl.getParam(index).getName() != null
                    ? resolvedMethodDecl.getParam(index).getName()
                    : "arg" + index;

            // 型名が取得できなかった場合は、呼び出し式から型名の解決を試みる
            String paramTypeName;
            try {
                paramTypeName = resolvedMethodDecl.getParam(index).getType().describe();
            } catch (Exception e) {
                paramTypeName = resolveFallbackParameterType(callExpr.getArgument(index));
            }
            parameters.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String returnTypeName;
        try {
            returnTypeName = resolvedMethodDecl.getReturnType().describe();
        } catch (Exception e) {
            returnTypeName = "UnknownReturnType";
        }

        // CalleeMethodInfoを作成
        CalleeMethodInfo calleeMethodInfo = new CalleeMethodInfo(declaringTypeName, calledMethodName, parameters,
                returnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        calleeMethods.put(calleeMethodInfo, calleeMethods.getOrDefault(calleeMethodInfo, 0) + 1);

        return;
    }

    /**
     * 明示的なフィールドアクセス式からフィールドアクセス情報を追加する。
     * 
     * @param accessedFields  フィールドアクセス情報とアクセス回数のマップ
     * @param fieldAccessExpr フィールドアクセス式
     * @param accessType      アクセス種別
     */
    private void addExplicitFieldAccesses(Map<AccessedFieldInfo, Integer> accessedFields,
            FieldAccessExpr fieldAccessExpr,
            AccessType accessType) {
        Expression scope = unwrap(fieldAccessExpr.getScope());
        String declaringTypeName = resolveScopeType(scope);
        String fieldName = fieldAccessExpr.getNameAsString();

        AccessedFieldInfo accessedFieldInfo = new AccessedFieldInfo(declaringTypeName, fieldName, accessType);

        // アクセス回数をカウント
        // 初出のアクセスは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        accessedFields.put(accessedFieldInfo, accessedFields.getOrDefault(accessedFieldInfo, 0) + 1);

        return;
    }

    /**
     * コンストラクタ呼び出しの解決に失敗した場合に、呼び出し式から情報を取得してCalleeMethodInfoを作成する。
     * 
     * @param calleeConstructors コンストラクタ呼び出し情報と呼び出し回数のマップ
     * @param newExpr            インスタンス生成式
     */
    private void addFallbackCalleeConstructorInfo(Map<CalleeMethodInfo, Integer> calleeConstructors,
            ObjectCreationExpr newExpr) {
        // 型名を取得
        String fallbackDeclaringTypeName = newExpr.getType().asString();

        // コンストラクタ名を取得
        String fallbackConstructorName = fallbackDeclaringTypeName;

        // パラメータの情報を取得
        Map<String, String> fallbackParams = new LinkedHashMap<>();
        for (Integer index = 0; index < newExpr.getArguments().size(); index++) {
            Expression argExpr = newExpr.getArgument(index);
            String paramName = "arg" + index;
            String paramTypeName = resolveFallbackParameterType(argExpr);
            fallbackParams.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String fallbackReturnTypeName = null;

        // CalleeMethodInfoを作成
        CalleeMethodInfo fallbackCalleeMethodInfo = new CalleeMethodInfo(fallbackDeclaringTypeName,
                fallbackConstructorName,
                fallbackParams, fallbackReturnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        calleeConstructors.put(fallbackCalleeMethodInfo,
                calleeConstructors.getOrDefault(fallbackCalleeMethodInfo, 0) + 1);

        // エラーが発生したことを示すフラグを設定
        handleError();

        return;
    }

    /**
     * メソッド呼び出しの解決に失敗した場合に、呼び出し式から情報を取得してCalleeMethodInfoを作成する。
     * 
     * @param calleeMethods メソッド呼び出し情報と呼び出し回数のマップ
     * @param call          メソッド呼び出し式
     */
    private void addFallbackCalleeMethodInfo(Map<CalleeMethodInfo, Integer> calleeMethods, MethodCallExpr call) {
        // 型名を取得
        String fallbackDeclaringTypeName = call.getScope().map(Object::toString).orElse("UnknownType");

        // メソッド名を取得
        String fallbackMethodName = call.getNameAsString();

        // パラメータの情報を取得
        Map<String, String> fallbackParams = new LinkedHashMap<>();
        for (Integer index = 0; index < call.getArguments().size(); index++) {
            Expression argExpr = call.getArgument(index);
            String paramName = "arg" + index;
            String paramTypeName = resolveFallbackParameterType(argExpr);
            fallbackParams.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String fallbackReturnTypeName = "UnknownReturnType";

        // CalleeMethodInfoを作成
        CalleeMethodInfo fallbackCalleeMethodInfo = new CalleeMethodInfo(fallbackDeclaringTypeName, fallbackMethodName,
                fallbackParams, fallbackReturnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        calleeMethods.put(fallbackCalleeMethodInfo, calleeMethods.getOrDefault(fallbackCalleeMethodInfo, 0) + 1);

        // エラーが発生したことを示すフラグを設定
        handleError();

        return;
    }

    /**
     * 明示的な(thisやsuperを使った)コンストラクタ呼び出しの解決に失敗した場合に、呼び出し式から情報を取得してCalleeMethodInfoを作成する。
     * 
     * @param pseudoConstructors 型情報と呼び出し回数のマップ
     * @param aType              型宣言
     * @param invocation         明示的なコンストラクタ呼び出し式
     */
    private void addFallbackPseudoConstructorInfo(Map<CalleeMethodInfo, Integer> pseudoConstructors,
            TypeDeclaration<?> aType, ExplicitConstructorInvocationStmt invocation) {
        // 型名を取得
        String fallbackDeclaringTypeName = aType.getFullyQualifiedName().orElse(aType.getNameAsString());

        // コンストラクタ名を取得
        String fallbackConstructorName = invocation.isThis() ? "this" : "super";

        // パラメータの情報を取得
        Map<String, String> fallbackParams = new LinkedHashMap<>();
        for (Integer index = 0; index < invocation.getArguments().size(); index++) {
            Expression argExpr = invocation.getArgument(index);
            String paramName = "arg" + index;
            String paramTypeName = resolveFallbackParameterType(argExpr);
            fallbackParams.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String fallbackReturnTypeName = null;

        // CalleeMethodInfoを作成
        CalleeMethodInfo fallbackCalleeMethodInfo = new CalleeMethodInfo(fallbackDeclaringTypeName,
                fallbackConstructorName,
                fallbackParams, fallbackReturnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        pseudoConstructors.put(fallbackCalleeMethodInfo,
                pseudoConstructors.getOrDefault(fallbackCalleeMethodInfo, 0) + 1);

        // エラーが発生したことを示すフラグを設定
        handleError();

        return;
    }

    /**
     * 暗黙的なフィールドアクセス式からフィールドアクセス情報を追加する。
     * 
     * @param accessedFields フィールドアクセス情報とアクセス回数のマップ
     * @param nameExpr       名前式
     * @param accessType     アクセス種別
     */
    private void addImplicitFieldAccesses(Map<AccessedFieldInfo, Integer> accessedFields, NameExpr nameExpr,
            AccessType accessType) {
        try {
            ResolvedValueDeclaration resolvedValueDeclaration = nameExpr.resolve();

            // フィールドでない場合はスキップ
            if (!resolvedValueDeclaration.isField()) {
                return;
            }

            String declaringTypeName = resolvedValueDeclaration.asField().declaringType().getQualifiedName();
            String fieldName = resolvedValueDeclaration.getName();

            AccessedFieldInfo accessedFieldInfo = new AccessedFieldInfo(declaringTypeName, fieldName, accessType);

            // アクセス回数をカウント
            // 初出のアクセスは1を追加，それ以降は既存のカウントに1を加算したもので上書き
            accessedFields.put(accessedFieldInfo, accessedFields.getOrDefault(accessedFieldInfo, 0) + 1);
        } catch (Exception e) {
            // 解決できなかった場合は無視
            System.err.println("名前式解決に失敗: " + nameExpr.getNameAsString() + " - " + e.getMessage());
        }
        return;
    }

    /**
     * 解決された明示的な(thisやsuperを使った)コンストラクタ呼び出し情報を追加する。
     * 
     * @param pseudoConstructors      型情報と呼び出し回数のマップ
     * @param aType                   型宣言
     * @param resolvedConstructorDecl 解決されたコンストラクタ宣言
     * @param invocation              明示的なコンストラクタ呼び出し式
     */
    private void addPseudoConstructorInfo(Map<CalleeMethodInfo, Integer> pseudoConstructors,
            TypeDeclaration<?> aType, ResolvedConstructorDeclaration resolvedConstructorDecl,
            ExplicitConstructorInvocationStmt invocation) {
        // 型名を取得
        String declaringTypeName = aType.getFullyQualifiedName().orElse(aType.getNameAsString());

        // コンストラクタ名を取得
        String constructorName = invocation.isThis() ? "this" : "super";

        // パラメータの情報を取得
        Map<String, String> parameters = new LinkedHashMap<>();
        for (Integer index = 0; index < resolvedConstructorDecl.getNumberOfParams(); index++) {
            String paramName = resolvedConstructorDecl.getParam(index).getName() != null
                    ? resolvedConstructorDecl.getParam(index).getName()
                    : "arg" + index;

            // 型名が取得できなかった場合は、呼び出し式から型名の解決を試みる
            String paramTypeName;
            try {
                paramTypeName = resolvedConstructorDecl.getParam(index).getType().describe();
            } catch (Exception e) {
                paramTypeName = resolveFallbackParameterType(invocation.getArgument(index));
            }
            parameters.put(paramName, paramTypeName);
        }

        // 戻り値の型名を取得
        String returnTypeName = null;

        // CalleeMethodInfoを作成
        CalleeMethodInfo calleeMethodInfo = new CalleeMethodInfo(declaringTypeName, constructorName, parameters,
                returnTypeName);

        // 呼び出し回数をカウント
        // 初出の呼び出しは1を追加，それ以降は既存のカウントに1を加算したもので上書き
        pseudoConstructors.put(calleeMethodInfo, pseudoConstructors.getOrDefault(calleeMethodInfo, 0) + 1);

        return;
    }

    /**
     * 引数で受け取ったレコード内のコンパクトコンストラクタの依存情報を解析する。
     * 
     * @param aRecord レコード宣言
     * @return コンパクトコンストラクタの依存情報のマップ
     */
    private Map<CallerMethodInfo, DependencyInfo> analyzeCompactConstructorDependency(RecordDeclaration aRecord) {
        Map<CallerMethodInfo, DependencyInfo> compactConstructorDependencyInfo = new LinkedHashMap<>();
        aRecord.getCompactConstructors().forEach(aCompactConstructor -> {
            // 解析するコンストラクタの情報を取得
            String compactConstructorName = aCompactConstructor.getNameAsString();
            Map<String, String> parameters = new LinkedHashMap<>();
            aRecord.getParameters().forEach(param -> {
                String paramName = param.getNameAsString();
                StringBuilder paramType = new StringBuilder();
                paramType.append(param.getTypeAsString().replaceAll(",", ", "));
                if (param.isVarArgs()) {
                    paramType.append("...");
                }
                parameters.put(paramName, paramType.toString());
            });
            CallerMethodInfo aMethodInfo = new CallerMethodInfo(compactConstructorName, parameters, null);

            Map<CalleeMethodInfo, Integer> callingMethods = new LinkedHashMap<>();

            // メソッド呼び出しを解析
            callingMethods.putAll(analyzeMethodCalls(aCompactConstructor));

            // コンストラクタ呼び出しを解析
            callingMethods.putAll(analyzeConstructorCalls(aRecord, aCompactConstructor));

            Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

            // フィールドアクセスを解析
            accessedFields.putAll(analyzeFieldAccesses(aCompactConstructor));

            // メソッドの依存情報を作成して追加
            compactConstructorDependencyInfo.put(aMethodInfo, new DependencyInfo(callingMethods, accessedFields));
        });
        return compactConstructorDependencyInfo;
    }

    /**
     * コンパクトコンストラクタ内のコンストラクタ呼び出しを解析する。
     * 
     * @param aRecord            レコード宣言
     * @param compactConstructor コンパクトコンストラクタ宣言
     * @return コンストラクタ呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeConstructorCalls(RecordDeclaration aRecord,
            CompactConstructorDeclaration compactConstructor) {
        Map<CalleeMethodInfo, Integer> callingConstructors = new LinkedHashMap<>();

        // コンストラクタ内のコンストラクタ呼び出し式(オブジェクト生成式)を解析
        callingConstructors.putAll(analyzeObjectCreations(compactConstructor));

        // 明示的な(thisやsuperを使った)コンストラクタ呼び出しを解析
        callingConstructors.putAll(analyzeExplicitConstructorInvocation(aRecord, compactConstructor));

        return callingConstructors;
    }

    /**
     * コンストラクタ内のコンストラクタ呼び出しを解析する。
     * 
     * @param aType       型宣言
     * @param constructor コンストラクタ宣言
     * @return コンストラクタ呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeConstructorCalls(TypeDeclaration<?> aType,
            ConstructorDeclaration constructor) {
        Map<CalleeMethodInfo, Integer> callingConstructors = new LinkedHashMap<>();

        // コンストラクタ内のコンストラクタ呼び出し式(オブジェクト生成式)を解析
        callingConstructors.putAll(analyzeObjectCreations(constructor));

        // 明示的な(thisやsuperを使った)コンストラクタ呼び出しを解析
        callingConstructors.putAll(analyzeExplicitConstructorInvocation(aType, constructor));

        return callingConstructors;
    }

    /**
     * メソッド内のコンストラクタ呼び出しを解析する。
     * 
     * @param method メソッド宣言
     * @return コンストラクタ呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeConstructorCalls(MethodDeclaration method) {
        Map<CalleeMethodInfo, Integer> callingConstructors = new LinkedHashMap<>();

        // メソッド内のコンストラクタ呼び出し式(オブジェクト生成式)を解析
        callingConstructors.putAll(analyzeObjectCreations(method));

        return callingConstructors;
    }

    /**
     * 引数で受け取った型内のコンストラクタの依存情報を解析する。
     * 
     * @param aType 型宣言
     * @return コンストラクタの依存情報のマップ
     */
    private Map<CallerMethodInfo, DependencyInfo> analyzeConstructorDependency(TypeDeclaration<?> aType) {
        Map<CallerMethodInfo, DependencyInfo> constructorDependencyInfo = new LinkedHashMap<>();
        aType.getConstructors().forEach(aConstructor -> {
            // 解析するコンストラクタの情報を取得
            String constructorName = aConstructor.getNameAsString();
            Map<String, String> parameters = new LinkedHashMap<>();
            aConstructor.getParameters().forEach(param -> {
                String paramName = param.getNameAsString();
                StringBuilder paramType = new StringBuilder();
                paramType.append(param.getTypeAsString().replaceAll(",", ", "));
                if (param.isVarArgs()) {
                    paramType.append("...");
                }
                parameters.put(paramName, paramType.toString());
            });
            CallerMethodInfo aMethodInfo = new CallerMethodInfo(constructorName, parameters, null);

            Map<CalleeMethodInfo, Integer> callingMethods = new LinkedHashMap<>();

            // メソッド呼び出しを解析
            callingMethods.putAll(analyzeMethodCalls(aConstructor));

            // コンストラクタ呼び出しを解析
            callingMethods.putAll(analyzeConstructorCalls(aType, aConstructor));

            Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

            // フィールドアクセスを解析
            accessedFields.putAll(analyzeFieldAccesses(aConstructor));

            // メソッドの依存情報を作成して追加
            constructorDependencyInfo.put(aMethodInfo, new DependencyInfo(callingMethods, accessedFields));
        });
        return constructorDependencyInfo;
    }

    /**
     * 引数で受け取ったコンパクトコンストラクタコンストラクタ内の明示的な(thisやsuperを使った)コンストラクタ呼び出しを解析する。
     * 
     * @param aRecord            レコード宣言
     * @param compactConstructor コンパクトコンストラクタ宣言
     * @return コンストラクタ呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeExplicitConstructorInvocation(
            RecordDeclaration aRecord, CompactConstructorDeclaration compactConstructor) {
        Map<CalleeMethodInfo, Integer> pseudoConstructors = new LinkedHashMap<>();

        compactConstructor.findAll(ExplicitConstructorInvocationStmt.class).forEach(invocation -> {
            ResolvedConstructorDeclaration resolvedConstructorDecl = null;
            try {
                // 明示的なコンストラクタ呼び出しを解決
                resolvedConstructorDecl = invocation.resolve();
            } catch (Exception e) {
                System.err.println(
                        "明示コンストラクタ呼び出し解析失敗: " + compactConstructor.getNameAsString() + " - " + e.getMessage());
                handleError();

                System.err.println("メソッド呼び出し解決失敗: " + e.getMessage() + " in " + invocation.toString());

                // 解決できなかった場合は，呼び出し式から情報を取得してCalleeMethodInfoを作成
                addFallbackPseudoConstructorInfo(pseudoConstructors, aRecord, invocation);

                return;
            }

            addPseudoConstructorInfo(pseudoConstructors, aRecord, resolvedConstructorDecl, invocation);
        });

        return pseudoConstructors;
    }

    /**
     * 引数で受け取ったコンストラクタ内の明示的な(thisやsuperを使った)コンストラクタ呼び出しを解析する。
     * 
     * @param aType       型宣言
     * @param constructor コンストラクタ宣言
     * @return コンストラクタ呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeExplicitConstructorInvocation(
            TypeDeclaration<?> aType, ConstructorDeclaration constructor) {
        Map<CalleeMethodInfo, Integer> pseudoConstructors = new LinkedHashMap<>();

        constructor.findAll(ExplicitConstructorInvocationStmt.class).forEach(invocation -> {
            ResolvedConstructorDeclaration resolvedConstructorDecl = null;
            try {
                // 明示的なコンストラクタ呼び出しを解決
                resolvedConstructorDecl = invocation.resolve();
            } catch (Exception e) {
                System.err.println("明示的なコンストラクタ呼び出し解析失敗: " + constructor.getNameAsString() + " - " + e.getMessage());
                handleError();

                System.err.println("メソッド呼び出し解決失敗: " + e.getMessage() + " in " + invocation.toString());

                // 解決できなかった場合は，呼び出し式から情報を取得してCalleeMethodInfoを作成
                addFallbackPseudoConstructorInfo(pseudoConstructors, aType, invocation);

                return;
            }

            addPseudoConstructorInfo(pseudoConstructors, aType, resolvedConstructorDecl, invocation);
        });

        return pseudoConstructors;
    }

    /**
     * 引数で受け取ったコンパクトコンストラクタ内のフィールドアクセスを解析する。
     * 
     * @param compactConstructor コンパクトコンストラクタ宣言
     * @return コンパクトコンストラクタ内のフィールドアクセス情報と呼び出し回数のマップ
     */
    private Map<AccessedFieldInfo, Integer> analyzeFieldAccesses(
            CompactConstructorDeclaration compactConstructor) {
        Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

        // 明示的なフィールドアクセス式を解析してフィールドアクセスを特定
        compactConstructor.findAll(FieldAccessExpr.class).forEach(fieldAccessExpr -> {
            AccessType accessType = resolveAccessType(fieldAccessExpr);
            addExplicitFieldAccesses(accessedFields, fieldAccessExpr, accessType);
        });

        // 暗黙的なフィールドアクセス式を解析してフィールドアクセスを特定
        compactConstructor.findAll(NameExpr.class).forEach(nameExpr -> {
            AccessType accessType = resolveAccessType(nameExpr);
            addImplicitFieldAccesses(accessedFields, nameExpr, accessType);
        });
        return accessedFields;
    }

    /**
     * 引数で受け取ったコンストラクタ内のフィールドアクセスを解析する。
     * 
     * @param constructor コンストラクタ宣言
     * @return コンストラクタ内のフィールドアクセス情報と呼び出し回数のマップ
     */
    private Map<AccessedFieldInfo, Integer> analyzeFieldAccesses(ConstructorDeclaration constructor) {
        Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

        // 明示的なフィールドアクセス式を解析してフィールドアクセスを特定
        constructor.findAll(FieldAccessExpr.class).forEach(fieldAccessExpr -> {
            AccessType accessType = resolveAccessType(fieldAccessExpr);
            addExplicitFieldAccesses(accessedFields, fieldAccessExpr, accessType);
        });

        // 暗黙的なフィールドアクセス式を解析してフィールドアクセスを特定
        constructor.findAll(NameExpr.class).forEach(nameExpr -> {
            AccessType accessType = resolveAccessType(nameExpr);
            addImplicitFieldAccesses(accessedFields, nameExpr, accessType);
        });
        return accessedFields;
    }

    /**
     * 引数で受け取ったメソッド内のフィールドアクセスを解析する。
     * 
     * @param method メソッド宣言
     * @return メソッド内のフィールドアクセス情報と呼び出し回数のマップ
     */
    private Map<AccessedFieldInfo, Integer> analyzeFieldAccesses(MethodDeclaration method) {
        Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

        // 明示的なフィールドアクセス式を解析してフィールドアクセスを特定
        method.findAll(FieldAccessExpr.class).forEach(fieldAccessExpr -> {
            AccessType accessType = resolveAccessType(fieldAccessExpr);
            addExplicitFieldAccesses(accessedFields, fieldAccessExpr, accessType);
        });

        // 暗黙的なフィールドアクセス式を解析してフィールドアクセスを特定
        method.findAll(NameExpr.class).forEach(nameExpr -> {
            AccessType accessType = resolveAccessType(nameExpr);
            addImplicitFieldAccesses(accessedFields, nameExpr, accessType);
        });
        return accessedFields;
    }

    /**
     * コンパクトコンストラクタ内のメソッド呼び出しを解析する。
     * 
     * @param compactConstructor コンパクトコンストラクタ宣言
     * @return メソッド呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeMethodCalls(CompactConstructorDeclaration compactConstructor) {
        Map<CalleeMethodInfo, Integer> calleeMethods = new LinkedHashMap<>();

        compactConstructor.findAll(MethodCallExpr.class).forEach(callExpr -> {
            ResolvedMethodDeclaration resolvedMethodDecl = null;
            try {
                // メソッド呼び出しを解決
                resolvedMethodDecl = callExpr.resolve();
            } catch (Exception e) {
                System.err.println("メソッド呼び出し解決失敗: " + e.getMessage() + " in " + callExpr.toString());

                // 解決できなかった場合は，呼び出し式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeMethodInfo(calleeMethods, callExpr);

                return;
            }

            addCalleeMethodInfo(calleeMethods, resolvedMethodDecl, callExpr);
        });
        return calleeMethods;
    }

    /**
     * コンストラクタ内のメソッド呼び出しを解析する。
     * 
     * @param constructor コンストラクタ宣言
     * @return メソッド呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeMethodCalls(ConstructorDeclaration constructor) {
        Map<CalleeMethodInfo, Integer> calleeMethods = new LinkedHashMap<>();

        constructor.findAll(MethodCallExpr.class).forEach(callExpr -> {
            ResolvedMethodDeclaration resolvedMethodDecl = null;
            try {
                // メソッド呼び出しを解決
                resolvedMethodDecl = callExpr.resolve();
            } catch (Exception e) {
                System.err.println("メソッド呼び出し解決失敗: " + e.getMessage() + " in " + callExpr.toString());

                // 解決できなかった場合は，呼び出し式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeMethodInfo(calleeMethods, callExpr);

                return;
            }

            addCalleeMethodInfo(calleeMethods, resolvedMethodDecl, callExpr);
        });
        return calleeMethods;
    }

    /**
     * メソッド内のメソッド呼び出しを解析する。
     * 
     * @param method メソッド宣言
     * @return メソッド呼び出し情報と呼び出し回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeMethodCalls(MethodDeclaration method) {
        Map<CalleeMethodInfo, Integer> calleeMethods = new LinkedHashMap<>();

        method.findAll(MethodCallExpr.class).forEach(callExpr -> {
            ResolvedMethodDeclaration resolvedMethodDecl = null;
            try {
                // メソッド呼び出しを解決
                resolvedMethodDecl = callExpr.resolve();
            } catch (Exception e) {
                System.err.println("メソッド呼び出し解決失敗: " + e.getMessage() + " in " + callExpr.toString());

                // 解決できなかった場合は，呼び出し式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeMethodInfo(calleeMethods, callExpr);

                return;
            }

            addCalleeMethodInfo(calleeMethods, resolvedMethodDecl, callExpr);
        });
        return calleeMethods;
    }

    /**
     * 引数で受け取った型内のメソッドの依存情報を解析する。
     * 
     * @param aType 型宣言
     * @return メソッドの依存情報のマップ
     */
    private Map<CallerMethodInfo, DependencyInfo> analyzeMethodDependency(TypeDeclaration<?> aType) {
        Map<CallerMethodInfo, DependencyInfo> methodDependencyInfo = new LinkedHashMap<>();
        aType.getMethods().forEach(aMethod -> {
            // 解析するメソッドの情報を取得
            String methodName = aMethod.getNameAsString();
            String returnTypeName = aMethod.getTypeAsString().replaceAll(",", ", ");
            Map<String, String> parameters = new LinkedHashMap<>();
            aMethod.getParameters().forEach(param -> {
                String paramName = param.getNameAsString();
                StringBuilder paramType = new StringBuilder();
                paramType.append(param.getTypeAsString().replaceAll(",", ", "));
                if (param.isVarArgs()) {
                    paramType.append("...");
                }
                parameters.put(paramName, paramType.toString());
            });
            CallerMethodInfo aMethodInfo = new CallerMethodInfo(methodName, parameters, returnTypeName);

            Map<CalleeMethodInfo, Integer> callingMethods = new LinkedHashMap<>();

            // メソッド呼び出しを解析
            callingMethods.putAll(analyzeMethodCalls(aMethod));

            // コンストラクタ呼び出しを解析
            callingMethods.putAll(analyzeConstructorCalls(aMethod));

            Map<AccessedFieldInfo, Integer> accessedFields = new LinkedHashMap<>();

            // フィールドアクセスを解析
            accessedFields.putAll(analyzeFieldAccesses(aMethod));

            // メソッドの依存情報を作成して追加
            methodDependencyInfo.put(aMethodInfo, new DependencyInfo(callingMethods, accessedFields));
        });
        return methodDependencyInfo;
    }

    /**
     * コンパクトコンストラクタ内のインスタンス生成式を解析する。
     * 
     * @param compactConstructor コンパクトコンストラクタ宣言
     * @return インスタンス生成情報と生成回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeObjectCreations(
            CompactConstructorDeclaration compactConstructor) {
        Map<CalleeMethodInfo, Integer> calleeConstructors = new LinkedHashMap<>();

        compactConstructor.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
            ResolvedConstructorDeclaration resolvedConstructor = null;
            try {
                // インスタンス生成を解決
                resolvedConstructor = newExpr.resolve();
            } catch (Exception e) {
                System.err.println("インスタンス生成解決失敗: " + e.getMessage() + " in " + newExpr.toString());

                // 解決できなかった場合は，インスタンス生成式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeConstructorInfo(calleeConstructors, newExpr);

                return;
            }

            addCalleeConstructorInfo(calleeConstructors, resolvedConstructor, newExpr);
        });
        return calleeConstructors;
    }

    /**
     * コンストラクタ内のインスタンス生成式を解析する。
     * 
     * @param constructor コンストラクタ宣言
     * @return インスタンス生成情報と生成回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeObjectCreations(ConstructorDeclaration constructor) {
        Map<CalleeMethodInfo, Integer> calleeConstructors = new LinkedHashMap<>();

        constructor.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
            ResolvedConstructorDeclaration resolvedConstructor = null;
            try {
                // インスタンス生成を解決
                resolvedConstructor = newExpr.resolve();
            } catch (Exception e) {
                System.err.println("インスタンス生成解決失敗: " + e.getMessage() + " in " + newExpr.toString());

                // 解決できなかった場合は，インスタンス生成式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeConstructorInfo(calleeConstructors, newExpr);

                return;
            }

            addCalleeConstructorInfo(calleeConstructors, resolvedConstructor, newExpr);
        });
        return calleeConstructors;
    }

    /**
     * メソッド内のインスタンス生成式を解析する。
     * 
     * @param method メソッド宣言
     * @return インスタンス生成情報と生成回数のマップ
     */
    private Map<CalleeMethodInfo, Integer> analyzeObjectCreations(MethodDeclaration method) {
        Map<CalleeMethodInfo, Integer> calleeConstructors = new LinkedHashMap<>();

        method.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
            ResolvedConstructorDeclaration resolvedConstructor = null;
            try {
                // インスタンス生成を解決
                resolvedConstructor = newExpr.resolve();
            } catch (Exception e) {
                System.err.println("インスタンス生成解決失敗: " + e.getMessage() + " in " + newExpr.toString());

                // 解決できなかった場合は，インスタンス生成式から情報を取得してCalleeMethodInfoを作成
                addFallbackCalleeConstructorInfo(calleeConstructors, newExpr);

                return;
            }

            addCalleeConstructorInfo(calleeConstructors, resolvedConstructor, newExpr);
        });
        return calleeConstructors;
    }

    /**
     * フィールドアクセス式のアクセス種別を解決する。
     * 
     * @param fieldAccessExpr フィールドアクセス式
     * @return アクセス種別
     */
    private AccessType resolveAccessType(FieldAccessExpr fieldAccessExpr) {
        Node parentNode = fieldAccessExpr.getParentNode().orElse(null);

        // 親ノードが括弧でなくなるまで親ノードを遡る
        while (parentNode instanceof EnclosedExpr) {
            parentNode = parentNode.getParentNode().orElse(null);
        }

        // 親ノードが存在しない場合はその他のアクセスとする
        if (parentNode == null) {
            return AccessType.OTHER;
        }

        // 親ノードが代入式の場合
        if (parentNode instanceof AssignExpr assignExpr) {
            // 代入式の左辺の場合は書き込みアクセスとする
            if (unwrap(assignExpr.getTarget()).equals(fieldAccessExpr)) {
                return AccessType.WRITE;
            }

            // 代入式の右辺の場合は読み取りアクセスとする
            if (unwrap(assignExpr.getValue()).equals(fieldAccessExpr)) {
                return AccessType.READ;
            }

            // それ以外の場合は読み取りアクセスとする(念のため)
            return AccessType.READ;
        }

        // 親ノードがメソッド呼び出し式の場合
        if (parentNode instanceof MethodCallExpr) {
            // メソッド呼び出し式のスコープの場合はその他のアクセスとする
            Optional<Expression> scopeOpt = ((MethodCallExpr) parentNode).getScope();
            if (scopeOpt.isPresent() && unwrap(scopeOpt.get()).equals(fieldAccessExpr)) {
                return AccessType.OTHER;
            }

            // 引数に含まれている場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 親ノードがインスタンス生成式の場合
        if (parentNode instanceof ObjectCreationExpr) {
            // インスタンス生成式のスコープの場合はその他のアクセスとする
            Optional<Expression> scopeOpt = ((ObjectCreationExpr) parentNode).getScope();
            if (scopeOpt.isPresent() && unwrap(scopeOpt.get()).equals(fieldAccessExpr)) {
                return AccessType.OTHER;
            }

            // 引数に含まれている場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 単項演算式の場合
        if (parentNode instanceof UnaryExpr unaryExpr) {
            // インクリメント・デクリメント演算子の場合は書き込みアクセスとする
            if (unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT) {
                return AccessType.WRITE;
            }

            // それ以外の単項演算子の場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 二項演算式の場合は読み取りアクセスとする
        if (parentNode instanceof BinaryExpr) {
            return AccessType.READ;
        }

        // 三項演算式の場合は読み取りアクセスとする
        if (parentNode instanceof ConditionalExpr) {
            return AccessType.READ;
        }

        // その他の場合は読み取りアクセスとする
        return AccessType.READ;
    }

    /**
     * 名前式のアクセス種別を解決する。
     * 
     * @param nameExpr 名前式
     * @return アクセス種別
     */
    private AccessType resolveAccessType(NameExpr nameExpr) {
        Node parentNode = nameExpr.getParentNode().orElse(null);

        // 親ノードが括弧でなくなるまで親ノードを遡る
        while (parentNode instanceof EnclosedExpr) {
            parentNode = parentNode.getParentNode().orElse(null);
        }

        // 親ノードが存在しない場合はその他のアクセスとする
        if (parentNode == null) {
            return AccessType.OTHER;
        }

        // 親ノードが代入式の場合
        if (parentNode instanceof AssignExpr assignExpr) {
            // 代入式の左辺の場合は書き込みアクセスとする
            if (unwrap(assignExpr.getTarget()).equals(nameExpr)) {
                return AccessType.WRITE;
            }

            // 代入式の右辺の場合は読み取りアクセスとする
            if (unwrap(assignExpr.getValue()).equals(nameExpr)) {
                return AccessType.READ;
            }

            // それ以外の場合は読み取りアクセスとする(念のため)
            return AccessType.READ;
        }

        // 親ノードがメソッド呼び出し式の場合
        if (parentNode instanceof MethodCallExpr) {
            // メソッド呼び出し式のスコープの場合はその他のアクセスとする
            Optional<Expression> scopeOpt = ((MethodCallExpr) parentNode).getScope();
            if (scopeOpt.isPresent() && unwrap(scopeOpt.get()).equals(nameExpr)) {
                return AccessType.OTHER;
            }

            // 引数に含まれている場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 親ノードがインスタンス生成式の場合
        if (parentNode instanceof ObjectCreationExpr) {
            // インスタンス生成式のスコープの場合はその他のアクセスとする
            Optional<Expression> scopeOpt = ((ObjectCreationExpr) parentNode).getScope();
            if (scopeOpt.isPresent() && unwrap(scopeOpt.get()).equals(nameExpr)) {
                return AccessType.OTHER;
            }

            // 引数に含まれている場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 単項演算式の場合
        if (parentNode instanceof UnaryExpr unaryExpr) {
            // インクリメント・デクリメント演算子の場合は書き込みアクセスとする
            if (unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT
                    || unaryExpr.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT) {
                return AccessType.WRITE;
            }

            // それ以外の単項演算子の場合は読み取りアクセスとする
            return AccessType.READ;
        }

        // 二項演算式の場合は読み取りアクセスとする
        if (parentNode instanceof BinaryExpr) {
            return AccessType.READ;
        }

        // 三項演算式の場合は読み取りアクセスとする
        if (parentNode instanceof ConditionalExpr) {
            return AccessType.READ;
        }

        // その他の場合は読み取りアクセスとする
        return AccessType.READ;
    }

    /**
     * 引数で受け取った引数式から引数の型名を解決する。
     * 
     * @param argExpr 引数の式
     * @return 引数の型名
     */
    private String resolveFallbackParameterType(Expression argExpr) {
        argExpr = unwrap(argExpr);

        // ラムダ式の場合は特別に処理
        if (argExpr instanceof LambdaExpr) {
            return "Lambda";
        }

        // メソッド参照式の場合は特別に処理
        if (argExpr instanceof MethodReferenceExpr) {
            return "MethodReference";
        }

        // 型名が取得できなかった場合は、引数式から解決を試みる
        try {
            return argExpr.calculateResolvedType().describe();
        } catch (Exception e) {
            Integer lastDotIndex = argExpr.toString().lastIndexOf(".");
            return argExpr.toString().substring(lastDotIndex + 1);
        }
    }

    /**
     * スコープ式からスコープの型名を解決する。
     * 
     * @param scope スコープ式
     * @return スコープの型名
     */
    private String resolveScopeType(Expression scope) {
        try {
            if (scope.isThisExpr()) {
                return "this";
            }
            if (scope.isSuperExpr()) {
                return "super";
            }
            if (scope.isNameExpr()) {
                return scope.asNameExpr().resolve().getType().describe();
            }
            if (scope.isObjectCreationExpr()) {
                return scope.asObjectCreationExpr().calculateResolvedType().describe();
            }
            if (scope.isMethodCallExpr()) {
                return scope.asMethodCallExpr().calculateResolvedType().describe();
            }
        } catch (Exception e) {
            // 解決できなかった場合は無視
            System.err.println("スコープ解決に失敗: " + scope.toString() + " - " + e.getMessage());
        }
        return scope.toString();
    }

    /**
     * 括弧で囲まれた式をアンラップする。
     * 
     * @param expr 式
     * @return アンラップされた式
     */
    private Expression unwrap(Expression expr) {
        while (expr.isEnclosedExpr()) {
            expr = expr.asEnclosedExpr().getInner();
        }
        return expr;
    }
}
