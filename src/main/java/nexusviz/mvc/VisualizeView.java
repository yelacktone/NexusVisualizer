package nexusviz.mvc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import mvc.View;
import nexusviz.generator.model.dependency.CallerMethodInfo;

/**
 * MVCモデルのビューに相当するクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("serial")
public class VisualizeView extends View {
	/**
	 * 情報を握っている Model のインスタンスを束縛する。
	 */
	protected VisualizeModel visualizeModel;

	/**
	 * 制御を司る Controller のインスタンスを束縛する。
	 */
	protected VisualizeController visualizeController;

	/**
	 * インスタンスを生成して応答するコンストラクタ。
	 * 指定されたモデルの依存物となり、コントローラを作り、モデルとビューを設定する。
	 * 
	 * @param model このビューのモデル
	 */
	@SuppressWarnings("this-escape")
	public VisualizeView(VisualizeModel model) {
		super(model);
		this.initialize(model, new VisualizeController());
		return;
	}

	/**
	 * インスタンスを生成して応答するコンストラクタ。
	 * 指定されたモデルの依存物となり、指定されたコントローラにモデルとビューを設定する。
	 * 
	 * @param model      このビューのモデル
	 * @param controller このビューのコントローラ
	 */
	@SuppressWarnings("this-escape")
	public VisualizeView(VisualizeModel model, VisualizeController controller) {
		super(model, controller);
		this.initialize(model, controller);
		return;
	}

	/**
	 * 指定されたグラフィクスに背景色（明灰色）でビュー全体を塗り、その後にモデルの内容物を描画する。
	 * 
	 * @param aGraphics グラフィックス・コンテキスト
	 */
	@Override
	public void paintComponent(Graphics aGraphics) {
		Integer width = this.getWidth();
		Integer height = this.getHeight();
		aGraphics.setColor(Color.lightGray);
		aGraphics.fillRect(0, 0, width, height);
		if (this.model == null) {
			return;
		}
		BufferedImage anImage = this.model.picture();
		if (anImage == null) {
			return;
		}
		aGraphics.drawImage(anImage, 0, 0, this);
		return;
	}

	/**
	 * クラス図にしたいJavaプロジェクトで使用しているライブラリを選択するダイアログを開き、ファイルをモデルでセットする。
	 * 
	 * @return 選択されたライブラリのディレクトリ
	 */
	public Path showAddLibraryDialog() {
		JFileChooser aChooser = new JFileChooser();

		// 選択できるファイルに制限をかける
		aChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Path selectedDirectory = null;
		StringBuilder message = new StringBuilder();
		switch (aChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			case JFileChooser.APPROVE_OPTION -> {
				message.append(aChooser.getSelectedFile().getName());
				message.append(" が選択されました。");

				selectedDirectory = aChooser.getSelectedFile().toPath();

				break;
			}
			case JFileChooser.CANCEL_OPTION -> {
				message.append("ファイルが選択されませんでした。");

				break;
			}
			case JFileChooser.ERROR_OPTION -> {
				message.append("エラーまたは取り消しが行われました。");

				break;
			}
		}
		System.out.println(message.toString());
		return selectedDirectory;
	}

	/**
	 * 引数のオプションから確認ダイアログを表示する。
	 * 
	 * @param index 確認のオプション番号
	 * @return ユーザの選択結果（YESまたはNO）
	 */
	public Integer showConfirmDialog(Integer index) {
		String message;
		switch (index) {
			case Constants.NO_LIBRARY_WARNING -> {
				message = "ライブラリがセットされていません。解析が不完全になる可能性があります。このまま実行しますか？";
				break;
			}
			default -> {
				message = "予期しない警告です。";
				break;
			}
		}
		return JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), message, "Warning",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * 引数の依存図画像を別ウィンドウを生成して表示する。
	 * 
	 * @param dependencyDiagramImage 依存図画像ファイル
	 */
	public void showDependencyDiagram(BufferedImage dependencyDiagramImage) {
		JPanel imagePanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(dependencyDiagramImage.getWidth(), dependencyDiagramImage.getHeight());
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.lightGray);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.drawImage(dependencyDiagramImage, 0, 0, this);
			}
		};

		// スクロール可能なパネルを作成
		JScrollPane scrollPane = new JScrollPane(imagePanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);

		this.generateDependencyWindow(scrollPane);
		return;
	}

	/**
	 * 引数のオプションから発生したエラーに対するダイアログを表示する。
	 * 
	 * @param index エラーのオプション番号
	 */
	public void showErrorDialog(Integer index) {
		String message;
		switch (index) {
			case Constants.NO_PROJECT_ERROR -> {
				message = "プロジェクトが選択されていません。";
				break;
			}
			case Constants.NO_METHOD_SELECTED_ERROR -> {
				message = "メソッドが選択されていません。";
				break;
			}
			case Constants.NOT_JAVA_PROJECT_ERROR -> {
				message = "選択されたディレクトリはJavaプロジェクトではありません。";
				break;
			}
			case Constants.SELECTED_NOT_METHOD_ERROR -> {
				message = "選択されたノードはメソッドではありません。";
				break;
			}
			case Constants.NO_DEPENDENCY_ANALYSIS_ERROR -> {
				message = "依存解析が実行されていません。";
				break;
			}
			case Constants.DEPENDENCY_ANALYSIS_ERROR -> {
				message = "依存解析中にエラーが発生しました。解析結果に不完全な部分が含まれています。";
				break;
			}
			default -> {
				message = "予期しないエラーです。";
				break;
			}
		}
		JOptionPane.showMessageDialog(
				SwingUtilities.getWindowAncestor(this), message, "Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	/**
	 * 引数のメッセージを情報ダイアログで表示する。
	 * 
	 * @param index 情報のオプション番号
	 */
	public void showInformationDialog(Integer index) {
		StringBuilder message = new StringBuilder();
		switch (index) {
			case Constants.DEPENDENCY_ANALYSIS_SUCCESS_INFORMATION -> {
				message.append("依存解析が正常に完了しました。");
				break;
			}
			case Constants.PROJECT_SELECTED_INFORMATION -> {
				message.append("解析対象のプロジェクトとして").append(System.lineSeparator());
				message.append(this.visualizeModel.selectedDirectory().toAbsolutePath().toString());
				message.append(System.lineSeparator());
				message.append("が選択されました。").append(System.lineSeparator());
				message.append("また、ライブラリの設定がリセットされました。");
				break;
			}
			case Constants.LIBRARY_SET_INFORMATION -> {
				message.append("ライブラリとして").append(System.lineSeparator());
				message.append(this.visualizeModel.selectedLibraryDirectory().toAbsolutePath().toString());
				message.append(System.lineSeparator());
				message.append("がセットされました。");
				break;
			}
			case Constants.METHOD_SELECTED_INFORMATION -> {
				message.append(this.visualizeModel.selectedMethodDeclaringTypeName());
				message.append(System.lineSeparator());
				message.append("の");
				message.append(System.lineSeparator());
				message.append(this.buildMethodSignatureString(this.visualizeModel.selectedMethodInfo()));
				message.append(System.lineSeparator());
				message.append("が選択されました。");
				break;
			}
			default -> {
				message.append("予期しない情報です。");
				break;
			}
		}
		JOptionPane.showMessageDialog(
				SwingUtilities.getWindowAncestor(this), message, "Information", JOptionPane.INFORMATION_MESSAGE);
		return;
	}

	/**
	 * dependencyAnalysisResultに保持されているメソッド一覧をツリー形式で別ウィンドウを生成して表示する。
	 * 
	 * @return 選択されたメソッド情報のマップ（型名をキー，MethodInfoを値とする）
	 */
	public Map<String, CallerMethodInfo> showSelectMethodDialog() {
		AtomicReference<Map<String, CallerMethodInfo>> selectedMethod = new AtomicReference<>();
		selectedMethod.set(null);
		// ダイアログの作成
		JDialog dialog = new JDialog((JFrame) null, "Method Selection Window", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLayout(new BorderLayout());
		dialog.setSize(400, 600);
		dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

		// ツリー構造の作成
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.visualizeModel.selectedDirectory().toString());

		// パッケージ階層のノードを管理するマップ
		Map<String, DefaultMutableTreeNode> packageNodeMap = new HashMap<>();

		this.visualizeModel.dependencyAnalysisResult().dependencyInfoMap()
				.forEach((fullyQualifiedTypeName, callerMethodDependencyInfoMap) -> {
					// 完全修飾型名をパッケージ名と型名に分割
					Integer lastDotIndex = fullyQualifiedTypeName.lastIndexOf('.');
					String packageName = (lastDotIndex == -1) ? "" : fullyQualifiedTypeName.substring(0, lastDotIndex);
					String typeName = fullyQualifiedTypeName.substring(lastDotIndex + 1);

					// パッケージ階層のノードを取得または生成
					DefaultMutableTreeNode parentNode = getOrCreatePackageNodes(root, packageNodeMap, packageName);

					// 型ノードを作成してパッケージノードに追加
					DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeName);
					parentNode.add(typeNode);

					// パッケージ階層のノードマップに型ノードを登録
					packageNodeMap.put(fullyQualifiedTypeName, typeNode);

					// メソッドノードを型ノードに追加
					callerMethodDependencyInfoMap.keySet().forEach(methodInfo -> {
						// メソッド情報をノードに追加
						DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(
								buildMethodSignatureString(methodInfo));

						typeNode.add(methodNode);
					});
				});

		// ツリーの作成
		JTree tree = new JTree(root);
		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);

		// スクロールペインにツリーを追加
		JScrollPane scrollPane = new JScrollPane(tree);

		// 選択と取消のボタンを作成し、初期設定を行う
		JButton selectButton = new JButton("選択");
		JButton cancelButton = new JButton("取消");
		selectButton.setEnabled(false);
		selectButton.setBackground(null);

		// ツリーの選択イベントを監視し、選択されたノードに応じてボタンの有効化を切り替える
		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode != null && selectedNode.isLeaf()) {
				selectButton.setEnabled(true);
				selectButton.setBackground(Color.BLUE);
			} else {
				selectButton.setEnabled(false);
				selectButton.setBackground(null);
			}
		});

		// ボタンにイベントリスナーを追加
		StringBuilder message = new StringBuilder();
		selectButton.addActionListener(e -> {
			Map<String, CallerMethodInfo> tempSelectedMethod = this.onConfirm(tree);
			selectedMethod.set(new HashMap<>());
			if (tempSelectedMethod == null || tempSelectedMethod.isEmpty()) {
				message.append("メソッドが選択されませんでした。");
				System.out.println(message.toString());
				dialog.dispose();
				return;
			}
			selectedMethod.set(tempSelectedMethod);
			String declaringTypeName = selectedMethod.get().keySet().iterator().next();
			CallerMethodInfo methodInfo = selectedMethod.get().get(declaringTypeName);
			message.append(declaringTypeName);
			message.append(" の ");
			message.append(methodInfo.methodName());
			message.append(" が選択されました。");
			System.out.println(message.toString());
			dialog.dispose();
		});
		cancelButton.addActionListener(e -> {
			message.append("メソッド選択が取消されました。");
			System.out.println(message.toString());
			selectedMethod.set(null);
			dialog.dispose();
		});

		// ボタンを配置するパネルを作成
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(cancelButton, BorderLayout.WEST);
		buttonPanel.add(selectButton, BorderLayout.EAST);

		// ダイアログにスクロールペインとボタンパネルを追加
		dialog.add(scrollPane, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		// ダイアログを表示
		dialog.setVisible(true);

		return selectedMethod.get();
	}

	/**
	 * クラス図にしたいJavaプロジェクトのディレクトリを選択するダイアログを開き、ファイルをモデルでセットする。
	 * 
	 * @return 選択されたディレクトリ
	 */
	public Path showSelectProjectDialog() {
		JFileChooser aChooser = new JFileChooser();

		// 選択できるファイルに制限をかける
		aChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Path selectedDirectory = null;
		StringBuilder message = new StringBuilder();
		switch (aChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			case JFileChooser.APPROVE_OPTION -> {
				message.append(aChooser.getSelectedFile().getName());
				message.append(" が選択されました");

				selectedDirectory = aChooser.getSelectedFile().toPath();

				break;
			}
			case JFileChooser.CANCEL_OPTION -> {
				message.append("ファイルが選択されませんでした");

				break;
			}
			case JFileChooser.ERROR_OPTION -> {
				message.append("エラーまたは取り消しが行われました");

				break;
			}
		}
		System.out.println(message.toString());
		return selectedDirectory;
	}

	/**
	 * ビューの全領域を再描画する。
	 */
	@Override
	public void update() {
		this.setPreferredSize(this.visualizeModel.preferredSize());
		this.revalidate();
		this.repaint(0, 0, this.getWidth(), this.getHeight());
		return;
	}

	/**
	 * 引数のノードから完全修飾クラス名を生成して応答する。
	 * 
	 * @param typeNode 型情報を含むノード
	 * @return 生成された完全修飾クラス名
	 */
	private String buildFullyQualifiedClassName(DefaultMutableTreeNode typeNode) {
		StringBuilder fullyQualifiedName = new StringBuilder();
		TreeNode[] pathNodes = typeNode.getPath();
		String delimiter = "";
		for (Integer index = 1; index < pathNodes.length; index++) { // ルートノードはスキップ
			fullyQualifiedName.append(delimiter);
			fullyQualifiedName.append(pathNodes[index].toString());
			delimiter = ".";
		}
		return fullyQualifiedName.toString();
	}

	/**
	 * 引数のメソッド情報からメソッドシグネチャ文字列を生成して応答する。
	 * 
	 * @param methodInfo メソッド情報
	 * @return 生成されたメソッドシグネチャ文字列
	 */
	private String buildMethodSignatureString(CallerMethodInfo methodInfo) {
		StringBuilder methodSignature = new StringBuilder();
		methodSignature.append(methodInfo.methodName());
		methodSignature.append("(");
		String delimiter = "";
		for (Map.Entry<String, String> parameter : methodInfo.parameters().entrySet()) {
			methodSignature.append(delimiter);
			methodSignature.append(parameter.getKey());
			methodSignature.append(" : ");
			methodSignature.append(parameter.getValue());
			delimiter = ", ";
		}
		methodSignature.append(")");
		if (methodInfo.returnTypeName() != null) {
			methodSignature.append(" : ");
			methodSignature.append(methodInfo.returnTypeName());
		}
		return methodSignature.toString();
	}

	/**
	 * 引数のノードからMethodInfoを生成して応答する。
	 * 
	 * @param node メソッド情報を含むノード
	 * @return 生成されたMethodInfo
	 */
	private CallerMethodInfo convertToMethodInfo(DefaultMutableTreeNode node) {
		// メソッド形式でなければ null を返す
		if (!node.toString().contains("(") || !node.toString().contains(")")) {
			System.out.println("convertToMethodInfo: メソッド形式でないノードを検出 → " + node.toString());
			return null;
		}

		Integer parenStart = node.toString().indexOf('(');
		Integer parenEnd = node.toString().indexOf(')');
		String methodName = node.toString().substring(0, parenStart).trim();
		String paramsPart = node.toString().substring(parenStart + 1, parenEnd).trim() + ")";
		Map<String, String> parameters = new LinkedHashMap<>();
		if (!paramsPart.isEmpty()) {
			StringBuilder currentParam = new StringBuilder();
			Integer genericsDepth = 0;
			String paramName = null;
			String paramType = null;

			for (Character currentChar : paramsPart.toCharArray()) {
				// ネストの深さを追跡
				if (currentChar == '<') { // ネストの始まり
					genericsDepth++;
				} else if (currentChar == '>') { // ネストの終わり
					genericsDepth--;
				}

				// 区切り文字の処理
				if (currentChar == ':' && genericsDepth == 0) { // パラメータ名と型名の区切り
					paramName = currentParam.toString().trim();
					currentParam.setLength(0);
				} else if ((currentChar == ',' || currentChar == ')') && genericsDepth == 0) { // パラメータの区切り（','はパラメータの区切り、')'はパラメータリストの終わりを示す）
					paramType = currentParam.toString().trim();
					if (paramName != null && paramType != null) { // パラメータ名と型名が取得できていればマップに追加
						parameters.put(paramName, paramType);
					}
					currentParam.setLength(0);
					paramName = null;
					paramType = null;
				} else { // その他の文字
					currentParam.append(currentChar);
				}
			}
		}
		String returnTypeName = null;
		String afterParen = node.toString().substring(parenEnd + 1).trim();
		if (afterParen.startsWith(":")) {
			returnTypeName = afterParen.substring(1).trim();
		}
		return new CallerMethodInfo(methodName, parameters, returnTypeName);
	}

	/**
	 * 引数のコンポーネントを使用して依存図描画ウィンドウを生成する。
	 * 
	 * @param scrollPane ウィンドウに表示したいコンポーネント
	 */
	private void generateDependencyWindow(JScrollPane scrollPane) {
		JFrame dependencyWindow = new JFrame("Dependency Diagram Window");
		Dimension aDimension = new Dimension(600, 800);

		// 高さはタイトルバーの高さを考慮してウィンドウの大きさを決定する。
		dependencyWindow.addNotify();
		Integer titleBarHeight = dependencyWindow.getInsets().top;
		Integer width = aDimension.width;
		Integer height = aDimension.height + titleBarHeight;
		dependencyWindow.setSize(width, height);

		// 最初のウィンドウの出現位置を計算する。
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Integer x = (screenSize.width / 2) - (width / 2);
		Integer y = (screenSize.height / 2) - (height / 2);

		// ウィンドウに各種の設定を行って出現させる。
		dependencyWindow.setMinimumSize(new Dimension(300, 400 + titleBarHeight));
		dependencyWindow.setResizable(true);
		dependencyWindow.setLocation(x, y);
		dependencyWindow.setVisible(true);
		dependencyWindow.toFront();
		dependencyWindow.setLayout(new BorderLayout());
		dependencyWindow.add(scrollPane, BorderLayout.CENTER);
		return;
	}

	/**
	 * パッケージ名からパッケージ階層のノードを取得または生成して応答する。
	 * 
	 * @param root           ルートノード
	 * @param packageNodeMap すでに存在するパッケージノードのマップ
	 * @param packageName    パッケージ名
	 * @return 取得または生成されたパッケージ階層のノード
	 */
	private DefaultMutableTreeNode getOrCreatePackageNodes(DefaultMutableTreeNode root,
			Map<String, DefaultMutableTreeNode> packageNodeMap, String packageName) {
		// パッケージ名が空の場合はルートノードを返す
		if (packageName.isEmpty()) {
			return root;
		}

		String[] packageParts = packageName.split("\\.");

		StringBuilder currentPackage = new StringBuilder();
		DefaultMutableTreeNode currentNode = root;

		for (String part : packageParts) {
			if (currentPackage.length() > 0) {
				currentPackage.append(".");
			}
			currentPackage.append(part);

			String packageKey = currentPackage.toString();
			DefaultMutableTreeNode nextNode = packageNodeMap.get(packageKey);

			if (nextNode == null) {
				nextNode = new DefaultMutableTreeNode(part);
				currentNode.add(nextNode);
				packageNodeMap.put(packageKey, nextNode);
			}
			currentNode = nextNode;
		}
		return currentNode;
	}

	/**
	 * ウィンドウの初期設定を行う。
	 * MVCの関係構築とポップアップメニューの設定を行う。
	 * 
	 * @param model      このビューのモデル
	 * @param controller このビューのコントローラ
	 */
	@SuppressWarnings("this-escape")
	private void initialize(VisualizeModel model, VisualizeController controller) {
		this.visualizeModel = model;
		this.visualizeController = controller;
		this.visualizeController.setVisualizeModel(model);
		this.visualizeController.setVisualizeView(this);

		JPopupMenu aMenu = new JPopupMenu();
		aMenu.add(this.setCommand(Constants.EXECUTE_STRUCTURAL));
		aMenu.add(this.setCommand(Constants.SELECT_PROJECT));
		aMenu.add(this.setCommand(Constants.SET_LIBRARY));
		aMenu.add(this.setCommand(Constants.ANALYZE_DEPENDENCY));
		aMenu.add(this.setCommand(Constants.GENERATE_DEPENDENCY));
		aMenu.add(this.setCommand(Constants.SELECT_METHOD));
		this.add(aMenu);
		this.visualizeModel.popupMenu(aMenu);
		return;
	}

	/**
	 * ツリーで選択されたメソッド情報を取得して応答する。
	 * 
	 * @param tree メソッド選択用のツリーコンポーネント
	 * @return 選択されたメソッド情報のマップ（型名をキー，MethodInfoを値とする）
	 */
	private Map<String, CallerMethodInfo> onConfirm(JTree tree) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		Map<String, CallerMethodInfo> selectedMethod = new HashMap<>();

		// メソッドノードでない（ノードがnullまたは葉ノードでない）場合は何もせずに返す
		// 実装上はここに来ることはないはずだが念のため
		if (node == null || !node.isLeaf()) {
			return selectedMethod;
		}

		// メソッドノードからMethodInfoを生成
		CallerMethodInfo methodInfo = convertToMethodInfo(node);
		if (methodInfo == null) {
			System.out.println("選択されたノードはメソッド形式ではありません。");
			return null;
		}

		// 型ノードを取得
		DefaultMutableTreeNode typeNode = (DefaultMutableTreeNode) node.getParent();
		if (typeNode == null) {
			System.out.println("選択されたノードの親ノードが存在しません。");
			return null;
		}

		// 完全修飾名を生成してメソッド情報とともにマップに追加
		String fullyQualifiedClassName = buildFullyQualifiedClassName(typeNode);
		selectedMethod.put(fullyQualifiedClassName, methodInfo);

		return selectedMethod;
	}

	/**
	 * ポップアップメニューに表示するアイテムを生成して返す。
	 * 
	 * @param command メニューアイテムの名前とコマンド。
	 * @return メニューアイテム
	 */
	private JMenuItem setCommand(String command) {
		JMenuItem anItem = new JMenuItem(command);
		anItem.setActionCommand(command);
		anItem.addActionListener((ActionEvent anEvent) -> this.visualizeController.actionPerformed(anEvent));
		return anItem;
	}
}
