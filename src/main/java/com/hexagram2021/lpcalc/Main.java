package com.hexagram2021.lpcalc;

import com.google.common.collect.Maps;
import com.hexagram2021.lpcalc.utils.ConfigHelper;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.BiConsumer;

public class Main {
	private static final boolean DEBUG = true;

	public static class TestData {
		public static final int N = 6;
		public static final int M = 3;
		public static final double[][] A = {
			{ 1, 1, 3, -1, 0, 0 },
			{ 2, 2, 5, 0, -1, 0 },
			{ 4, 1, 2, 0, 0, -1 }
		};
		public static final double[] B = { 30, 24, 36 };
		public static final double[] C = { 3, 1, 2, 0, 0, 0 };
	}

	public static void main(String[] args) {
		ConfigHelper helper = new ConfigHelper();

		JFrame frame = new JFrame();
		JPanel panel = new JPanel(null);

		frame.setTitle("饲料配比计算器");
		frame.setSize(960, 560);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		buildEditConfigTable(panel, helper);

		frame.setContentPane(panel);
		frame.setVisible(true);

		Solver solver = new Solver(TestData.N, TestData.M, TestData.A, TestData.B, TestData.C);
		solver.solve();
	}

	static final int LEFT_DIST = 20;
	static final int TOP_DIST = 60;
	static final int CELL_WIDTH = 120;
	static final int CELL_HEIGHT = 50;
	static final int CELL_PADDING_WIDTH = 10;
	static final int CELL_PADDING_HEIGHT = 10;
	static final int CELL_MARGIN_HEIGHT = 20;

	static int skipRow = 1;
	static int skipColumn = 1;

	static final Font font = new Font("宋体", Font.PLAIN, 20);
	static final Font fontTitle = new Font("黑体", Font.BOLD, 24);

	private static void refreshAll(JPanel panel, ConfigHelper helper, BiConsumer<JPanel, ConfigHelper> consumer) {
		panel.removeAll();
		consumer.accept(panel, helper);
		panel.updateUI();
		helper.commit();
	}

	private static void buildEditConfigTable(JPanel panel, ConfigHelper helper) {
		JButton refresh = new JButton("刷新");
		refresh.setSize(80, 40);
		refresh.setLocation(800, 10);
		refresh.setFont(font);
		refresh.setFocusable(false);
		refresh.addActionListener(e -> refreshAll(panel, helper, Main::buildEditConfigTable));
		panel.add(refresh);

		JSlider topBottomSlider = new JSlider(SwingConstants.HORIZONTAL, 1, helper.getMaterialCount() - 1, skipRow);
		topBottomSlider.setSize(900, 40);
		topBottomSlider.setLocation(0, 480);
		topBottomSlider.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {
				skipRow = topBottomSlider.getValue();
				refreshAll(panel, helper, Main::buildEditConfigTable);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		panel.add(topBottomSlider);
		JSlider leftRightSlider = new JSlider(SwingConstants.VERTICAL, 1, helper.getCompositionCount() - 1, skipColumn);
		leftRightSlider.setSize(40, 480);
		leftRightSlider.setLocation(900, 0);
		leftRightSlider.setInverted(true);
		leftRightSlider.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {
				skipColumn = leftRightSlider.getValue();
				refreshAll(panel, helper, Main::buildEditConfigTable);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		panel.add(leftRightSlider);

		JLabel title = new JLabel("配置修改器");
		title.setSize(360, 40);
		title.setLocation(300, 10);
		title.setFont(fontTitle);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(title);

		JLabel label = new JLabel("原料名");
		label.setLocation(LEFT_DIST, TOP_DIST);
		label.setFont(font);
		label.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
		panel.add(label);

		int cnt = 1;
		for (ConfigHelper.Composition composition : helper.getCompositions()) {
			if(cnt < skipColumn) {
				cnt += 1;
				continue;
			}
			if(cnt == skipColumn && cnt != 1) {
				JLabel compositionLabel = new JLabel("...");
				compositionLabel.setLocation(LEFT_DIST, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT));
				compositionLabel.setFont(font);
				compositionLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(compositionLabel);
				cnt += 1;

				JPopupMenu menu = new JPopupMenu();
				menu.add("添加成分").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的成分名：");
					addRow(helper, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				compositionLabel.setComponentPopupMenu(menu);
			} else {
				JLabel compositionLabel = new JLabel(composition.name());
				compositionLabel.setLocation(LEFT_DIST, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (cnt - skipColumn + 1));
				compositionLabel.setFont(font);
				compositionLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(compositionLabel);
				cnt += 1;

				JPopupMenu menu = new JPopupMenu();
				menu.add("删除成分").addActionListener(e -> {
					removeRow(helper, composition);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("修改名称").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("原名称：%s\n请输入修改后的成分名：".formatted(composition.name()));
					changeRowName(helper, composition, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("移至末行").addActionListener(e -> {
					swapToLastRow(helper, composition);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("添加成分").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的成分名：");
					addRow(helper, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				compositionLabel.setComponentPopupMenu(menu);
			}
		}
		int row = 1;
		for(ConfigHelper.Material material: helper.getMaterials()) {
			if (row < skipRow) {
				row += 1;
				continue;
			}
			if (row == skipRow && row != 1) {
				JLabel materialLabel = new JLabel("...");
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (row - skipRow + 1), TOP_DIST);
				materialLabel.setFont(font);
				materialLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(materialLabel);

				JPopupMenu menu = new JPopupMenu();
				menu.add("添加原料").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的原料名：");
					addColumn(helper, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				materialLabel.setComponentPopupMenu(menu);

				int col = 1;
				for (ConfigHelper.Composition ignored : helper.getCompositions()) {
					if(col < skipColumn) {
						col += 1;
						continue;
					}
					JLabel skipLabel = new JLabel("...");
					skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (row - skipRow + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (col - skipColumn + 1));
					skipLabel.setFont(font);
					skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
					panel.add(skipLabel);
					col += 1;
				}
			} else {
				JLabel materialLabel = new JLabel(material.name());
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (row - skipRow + 1), TOP_DIST);
				materialLabel.setFont(font);
				materialLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(materialLabel);

				JPopupMenu menu = new JPopupMenu();
				menu.add("删除原料").addActionListener(e -> {
					removeColumn(helper, material);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("修改名称").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("原名称：%s\n请输入修改后的原料名：".formatted(material.name()));
					changeColumnName(helper, material, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("移至末行").addActionListener(e -> {
					swapToLastColumn(helper, material);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				menu.add("添加原料").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的原料名：");
					addColumn(helper, name);
					refreshAll(panel, helper, Main::buildEditConfigTable);
				});
				materialLabel.setComponentPopupMenu(menu);

				int col = 1;
				for (ConfigHelper.Composition composition : helper.getCompositions()) {
					if(col < skipColumn) {
						col += 1;
						continue;
					}
					if(col == skipColumn && col != 1) {
						JLabel skipLabel = new JLabel("...");
						skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (row - skipRow + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (col - skipColumn + 1));
						skipLabel.setFont(font);
						skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
						panel.add(skipLabel);
					} else {
						JComponent jComponent;
						if (material.compositions().containsKey(composition)) {
							JTextField textField = new JTextField();
							textField.setText(String.valueOf(material.compositions().get(composition).doubleValue()));
							textField.addActionListener(e -> material.compositions().replace(composition, Double.valueOf(textField.getText())));
							jComponent = textField;

							JPopupMenu popupMenu = new JPopupMenu();
							popupMenu.add("删除").addActionListener(e -> {
								removeCell(material, composition);
								refreshAll(panel, helper, Main::buildEditConfigTable);
							});
							jComponent.setComponentPopupMenu(popupMenu);
						} else {
							JButton button = new JButton("添加成分");
							button.addActionListener(e -> {
								addCell(material, composition);
								refreshAll(panel, helper, Main::buildEditConfigTable);
							});
							jComponent = button;
						}
						jComponent.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (row - skipRow + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (col - skipColumn + 1));
						jComponent.setFont(font);
						jComponent.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
						panel.add(jComponent);
					}
					col += 1;
				}
			}
			row += 1;
		}
	}

	private static void removeCell(ConfigHelper.Material material, ConfigHelper.Composition composition) {
		material.compositions().remove(composition);
	}

	private static void addCell(ConfigHelper.Material material, ConfigHelper.Composition composition) {
		material.compositions().put(composition, 0.0);
	}

	private static void swapToLastRow(ConfigHelper helper, ConfigHelper.Composition composition) {
		try {
			helper.getCompositions().remove(composition);
			helper.getCompositions().add(composition);
		} catch(RuntimeException exception) {
			error("非法的成分“%s”".formatted(composition.name()));
		}
	}

	private static ConfigHelper.Composition addRow(ConfigHelper helper, String name) {
		if(!ConfigHelper.verifyRegistry(helper.getCompositions(), name)) {
			error("重复的成分名：%s".formatted(name));
			return null;
		}
		ConfigHelper.Composition ret = new ConfigHelper.Composition(name);
		helper.getCompositions().add(ret);
		return ret;
	}

	private static void changeRowName(ConfigHelper helper, ConfigHelper.Composition composition, String name) {
		ConfigHelper.Composition newComposition = addRow(helper, name);

		if(newComposition != null) {
			try {
				helper.getCompositions().remove(composition);
				for(ConfigHelper.Material material: helper.getMaterials()) {
					Double v = material.compositions().remove(composition);
					if(v != null) {
						material.compositions().put(newComposition, v);
					}
				}
			} catch(RuntimeException exception) {
				error("非法的成分“%s”".formatted(composition.name()));
			}
		}
	}

	private static void removeRow(ConfigHelper helper, ConfigHelper.Composition composition) {
		try {
			helper.getCompositions().remove(composition);
			for(ConfigHelper.Material material: helper.getMaterials()) {
				material.compositions().remove(composition);
			}
		} catch(RuntimeException exception) {
			error("非法的成分“%s”".formatted(composition.name()));
		}
	}

	private static void swapToLastColumn(ConfigHelper helper, ConfigHelper.Material material) {
		try {
			helper.getMaterials().remove(material);
			helper.getMaterials().add(material);
		} catch(RuntimeException exception) {
			error("非法的原料“%s”".formatted(material.name()));
		}
	}

	private static ConfigHelper.Material addColumn(ConfigHelper helper, String name) {
		if(!ConfigHelper.verifyRegistry(helper.getMaterials(), name)) {
			error("重复的原料名：%s".formatted(name));
			return null;
		}
		ConfigHelper.Material ret = new ConfigHelper.Material(name, 0.0, Maps.newHashMap());
		helper.getMaterials().add(ret);
		return ret;
	}

	private static void changeColumnName(ConfigHelper helper, ConfigHelper.Material material, String name) {
		ConfigHelper.Material newMaterial = addColumn(helper, name);

		if(newMaterial != null) {
			try {
				helper.getMaterials().remove(material);
				newMaterial.compositions().putAll(material.compositions());
			} catch(RuntimeException exception) {
				error("非法的原料“%s”".formatted(material.name()));
			}
		}
	}

	private static void removeColumn(ConfigHelper helper, ConfigHelper.Material material) {
		try {
			helper.getMaterials().remove(material);
		} catch(RuntimeException exception) {
			error("非法的原料“%s”".formatted(material.name()));
		}
	}

	public static void debug(@Nonnull String message) {
		if(DEBUG) {
			System.out.println(message);
		}
	}

	public static void log(@Nonnull String message) {
		System.out.println(message);
	}

	public static void error(@Nonnull String message) {
		JOptionPane.showMessageDialog(null, message, "Error!", JOptionPane.ERROR_MESSAGE);
		System.err.println(message);
	}
}
