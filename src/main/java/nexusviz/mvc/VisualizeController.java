package nexusviz.mvc;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mvc.Controller;
import mvc.View;
import nexusviz.generator.analyzer.DependencyAnalyzer;
import nexusviz.generator.analyzer.StructuralAnalyzer;
import nexusviz.generator.converter.DependencyConverter;
import nexusviz.generator.converter.StructuralConverter;
import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.model.dependency.DependencyInfo;
import nexusviz.generator.result.StructuralAnalysisResult;

/**
 * MVCモデルのコントローラに相当するクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class VisualizeController extends Controller {
	/**
	 * 情報を握っているModelのインスタンスを束縛する。
	 */
	protected VisualizeModel visualizeModel;

	/**
	 * 表示を司るViewのインスタンスを束縛する。
	 */
	protected VisualizeView visualizeView;

	/**
	 * インスタンスを生成して応答するコンストラクタ。
	 * すべてのインスタンス変数（model, view）をnull化する。
	 */
	public VisualizeController() {
		super();
		this.visualizeModel = null;
		this.visualizeView = null;
		return;
	}

	/**
	 * ポップアップメニューで押されたアイテムに対応する処理を実行する。
	 * 
	 * @param event 押されたアイテムの情報
	 */
	public void actionPerformed(ActionEvent event) {
		String aCommand = event.getActionCommand();

		switch (aCommand) {
			case Constants.EXECUTE_STRUCTURAL -> {
				System.out.println(aCommand);
				this.clickedExecuteStructural();

				break;
			}
			case Constants.SELECT_PROJECT -> {
				System.out.println(aCommand);
				this.clickedSelectProject();

				break;
			}
			case Constants.ANALYZE_DEPENDENCY -> {
				System.out.println(aCommand);
				this.clickedAnalyzeDependency();

				break;
			}
			case Constants.SET_LIBRARY -> {
				System.out.println(aCommand);
				this.clickedSetLibrary();

				break;
			}
			case Constants.SELECT_METHOD -> {
				System.out.println(aCommand);
				this.clickedSelectMethod();

				break;
			}
			case Constants.GENERATE_DEPENDENCY -> {
				System.out.println(aCommand);
				this.clickedGenerateDependency();

				break;
			}
		}
		return;
	}

	/**
	 * ウィンドウ上で右クリックが行われた際にポップアップメニューを表示する。
	 * 
	 * @param event クリックイベントの情報
	 */
	@Override
	public void mouseClicked(MouseEvent event) {
		Point aPoint = event.getPoint();
		if (SwingUtilities.isRightMouseButton(event)) {
			this.visualizeModel.popupMenu().show(event.getComponent(), aPoint.x, aPoint.y);
		}
		return;
	}

	/**
	 * 指定されたビューをインスタンス変数viewに設定する。
	 * ビューのマウスのリスナをこのコントローラにする 。
	 * 
	 * @param aView このコントローラのビュー
	 */
	@Override
	public void setView(View aView) {
		this.view = aView;
		this.view.addMouseListener(this);
		return;
	}

	/**
	 * 指定されたモデルをインスタンス変数 model に設定する。
	 * 
	 * @param model このコントローラのモデル
	 */
	public void setVisualizeModel(VisualizeModel model) {
		this.visualizeModel = model;
		return;
	}

	/**
	 * 指定されたビューをインスタンス変数 view に設定し、
	 * 
	 * @param view このコントローラのビュー
	 */
	public void setVisualizeView(VisualizeView view) {
		this.visualizeView = view;
		return;
	}

	/**
	 * AnalyzeDependencyを選択された際の処理を行う。
	 * selectedDirectoryから依存解析を実行する。
	 */
	private void clickedAnalyzeDependency() {
		if (this.visualizeModel.selectedDirectory() != null) { // ファイルが選択されている場合
			// ライブラリがセットされていない場合、警告ダイアログを表示する
			if (this.visualizeModel.selectedLibraryDirectory() == null) {
				// 続行しない場合、処理を中断する
				if (this.visualizeView.showConfirmDialog(Constants.NO_LIBRARY_WARNING) != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// 依存解析を実行する
			try {
				DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();
				// 解析結果をモデルにセットする
				this.visualizeModel
						.dependencyAnalysisResult(dependencyAnalyzer.analyze(this.visualizeModel.selectedDirectory(),
								this.visualizeModel.selectedLibraryDirectory()));

				if (this.visualizeModel.dependencyAnalysisResult().hasError()) {
					this.visualizeView.showErrorDialog(Constants.DEPENDENCY_ANALYSIS_ERROR);
				} else {
					this.visualizeView.showInformationDialog(Constants.DEPENDENCY_ANALYSIS_SUCCESS_INFORMATION);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.visualizeView.showErrorDialog(Constants.NO_PROJECT_ERROR);
		}
		return;
	}

	/**
	 * ExecuteStructuralを選択された際の処理を行う。
	 * selectedDirectoryの構造解析を行い、その結果からクラス図を生成し、ウィンドウに表示する。
	 */
	private void clickedExecuteStructural() {
		if (this.visualizeModel.selectedDirectory() != null) { // ファイルが選択されている場合
			// ライブラリがセットされていない場合、警告ダイアログを表示する
			if (this.visualizeModel.selectedLibraryDirectory() == null) {
				// 続行しない場合、処理を中断する
				if (this.visualizeView.showConfirmDialog(Constants.NO_LIBRARY_WARNING) != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// 構造解析を実行し、クラス図を生成する
			try {
				StructuralConverter structuralConverter = new StructuralConverter();
				StructuralAnalyzer structuralAnalyzer = new StructuralAnalyzer();

				// 解析結果をモデルにセットする
				StructuralAnalysisResult structuralAnalysisResult = structuralAnalyzer.analyze(
						this.visualizeModel.selectedDirectory(),
						this.visualizeModel.selectedLibraryDirectory());

				// Javaプロジェクトでない(解析結果がnull)場合、エラーダイアログを表示して処理を中断する
				if (structuralAnalysisResult == null) {
					this.visualizeView.showErrorDialog(Constants.NOT_JAVA_PROJECT_ERROR);
					return;
				}

				// クラス図を生成する
				String outputImagePath = structuralConverter.executeConversion(
						this.visualizeModel.selectedDirectory(),
						structuralAnalysisResult.typeInfos(),
						structuralAnalysisResult.typeRelations());

				BufferedImage anImage = ImageIO.read(new File(outputImagePath));
				this.visualizeModel.picture(anImage);
				this.visualizeModel.changed(anImage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.visualizeView.showErrorDialog(Constants.NO_PROJECT_ERROR);
		}
		return;
	}

	/**
	 * GenerateDependencyを選択された際の処理を行う。
	 * selectedMethodNameからメソッド依存図を生成し、ウィンドウに表示する。
	 */
	private void clickedGenerateDependency() {
		if (this.visualizeModel.selectedMethodDeclaringTypeName() != null
				&& this.visualizeModel.selectedMethodInfo() != null) { // メソッドが選択されている場合
			try {
				DependencyConverter dependencyConverter = new DependencyConverter();
				String declaringTypeName = this.visualizeModel.selectedMethodDeclaringTypeName();
				CallerMethodInfo methodInfo = this.visualizeModel.selectedMethodInfo();
				DependencyInfo dependencyInfo = this.visualizeModel.dependencyAnalysisResult().dependencyInfoMap()
						.get(declaringTypeName).get(methodInfo);

				// メソッド依存図を生成する
				String outputImagePath = dependencyConverter.executeConversion(declaringTypeName, methodInfo,
						dependencyInfo);

				BufferedImage anImage = ImageIO.read(new File(outputImagePath));
				this.visualizeView.showDependencyDiagram(anImage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.visualizeView.showErrorDialog(Constants.NO_METHOD_SELECTED_ERROR);
		}
		return;
	}

	/**
	 * SelectMethodを選択された際の処理を行う。
	 * メソッド依存図に変換したいメソッドを選択する。
	 */
	private void clickedSelectMethod() {
		if (this.visualizeModel.dependencyAnalysisResult() == null) {
			this.visualizeView.showErrorDialog(Constants.NO_DEPENDENCY_ANALYSIS_ERROR);
			return;
		}
		Map<String, CallerMethodInfo> selectedMethod = this.visualizeView.showSelectMethodDialog();
		if (selectedMethod == null) {
			return;
		}
		if (selectedMethod.isEmpty()) {
			this.visualizeView.showErrorDialog(Constants.SELECTED_NOT_METHOD_ERROR);
			return;
		}

		Map.Entry<String, CallerMethodInfo> entry = selectedMethod.entrySet().iterator().next();
		this.visualizeModel.selectedMethodDeclaringTypeName(entry.getKey());
		this.visualizeModel.selectedMethodInfo(entry.getValue());

		this.visualizeView.showInformationDialog(Constants.METHOD_SELECTED_INFORMATION);
		return;
	}

	/**
	 * SelectProjectを選択された際の処理を行う。
	 * クラス図に変換したいファイルを選択する。
	 */
	private void clickedSelectProject() {
		Path selectedFilePath = this.visualizeView.showSelectProjectDialog();
		if (selectedFilePath == null) { // ファイル選択がキャンセルされた場合
			return;
		}
		this.visualizeModel.selectedDirectory(selectedFilePath);
		this.visualizeView.showInformationDialog(Constants.PROJECT_SELECTED_INFORMATION);

		// ライブラリ、依存解析結果および選択されたメソッドに関する情報をリセットする
		this.visualizeModel.selectedLibraryDirectory(null);
		this.visualizeModel.dependencyAnalysisResult(null);
		this.visualizeModel.selectedMethodDeclaringTypeName(null);
		this.visualizeModel.selectedMethodInfo(null);
		return;
	}

	/**
	 * SetLibraryを選択された際の処理を行う。
	 * 解析したいプロジェクトで使用されているライブラリをセットする。
	 */
	private void clickedSetLibrary() {
		// ファイルが選択されていない場合、その旨を通知しキャンセルする
		if (this.visualizeModel.selectedDirectory() == null) {
			this.visualizeView.showErrorDialog(Constants.NO_PROJECT_ERROR);
			return;
		}

		Path filePath = this.visualizeView.showAddLibraryDialog();
		if (filePath == null) { // ファイル選択がキャンセルされた場合
			return;
		}
		this.visualizeModel.selectedLibraryDirectory(filePath);
		this.visualizeView.showInformationDialog(Constants.LIBRARY_SET_INFORMATION);
		return;
	}
}
