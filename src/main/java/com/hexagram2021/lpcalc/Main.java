package com.hexagram2021.lpcalc;

import com.google.common.collect.Maps;
import com.hexagram2021.lpcalc.utils.ConfigHelper;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class Main {
	private static final boolean DEBUG = true;

	@SuppressWarnings("unused")
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

	private static Writer logWriter = null;

	public static void main(String[] args) {
		ConfigHelper helper = new ConfigHelper();

		File logFile = new File("./日志.log");
		if(!logFile.exists()) {
			try {
				if(!logFile.createNewFile()) {
					error("日志文件创建失败！");
				}
			} catch (IOException exception) {
				except("日志文件访问失败！", exception);
			}
		}
		try {
			logWriter = new FileWriter(logFile, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			except("日志文件读取失败！", exception);
		}

		JFrame frame = new JFrame();
		JPanel panel = new JPanel(null);

		frame.setTitle("饲料配比计算器 v" + Main.class.getPackage().getImplementationVersion());
		frame.setSize(960, 560);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				try {
					logWriter.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		});

		buildEditConfigTable(panel, helper);

		frame.setContentPane(panel);
		frame.setVisible(true);

		//Solver solver = new Solver(TestData.N, TestData.M, TestData.A, TestData.B, TestData.C);
		//solver.solve();
	}

	static final int LEFT_DIST = 20;
	static final int TOP_DIST = 80;
	static final int CELL_WIDTH = 120;
	static final int CELL_HEIGHT = 50;
	static final int CELL_PADDING_WIDTH = 10;
	static final int CELL_PADDING_HEIGHT = 10;
	static final int CELL_MARGIN_HEIGHT = 20;

	static int skipColumn = 1;
	static int skipRow = 1;

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

		JButton toSolve = new JButton("编辑价格");
		toSolve.setSize(120, 40);
		toSolve.setLocation(20, 10);
		toSolve.setFont(font);
		toSolve.setFocusable(false);
		toSolve.addActionListener(e -> {
			skipRow = skipColumn = 1;
			refreshAll(panel, helper, Main::buildPriceTable);
		});
		panel.add(toSolve);

		JSlider topDownSlider = new JSlider(SwingConstants.VERTICAL, 1, helper.getCompositionCount() - 1, skipRow);
		topDownSlider.setSize(40, 480);
		topDownSlider.setLocation(900, 0);
		topDownSlider.setInverted(true);
		topDownSlider.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {
				skipRow = topDownSlider.getValue();
				refreshAll(panel, helper, Main::buildEditConfigTable);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		panel.add(topDownSlider);

		JSlider leftRightSlider = new JSlider(SwingConstants.HORIZONTAL, 1, helper.getMaterialCount() - 1, skipColumn);
		leftRightSlider.setSize(900, 40);
		leftRightSlider.setLocation(0, 480);
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
		title.setSize(400, 40);
		title.setLocation(280, 10);
		title.setFont(fontTitle);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(title);

		JLabel label = new JLabel("成分\\原料");
		label.setLocation(LEFT_DIST, TOP_DIST);
		label.setFont(font);
		label.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
		panel.add(label);

		int cnt = 1;
		for (ConfigHelper.Composition composition : helper.getCompositions()) {
			if(cnt < skipRow) {
				cnt += 1;
				continue;
			}
			if(cnt == skipRow && cnt != 1) {
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
				compositionLabel.setLocation(LEFT_DIST, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (cnt - skipRow + 1));
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
		int col = 1;
		for(ConfigHelper.Material material: helper.getMaterials()) {
			if (col < skipColumn) {
				col += 1;
				continue;
			}
			if (col == skipColumn && col != 1) {
				JLabel materialLabel = new JLabel("...");
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH), TOP_DIST);
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

				int row = 1;
				for (ConfigHelper.Composition ignored : helper.getCompositions()) {
					if(row < skipRow) {
						row += 1;
						continue;
					}
					JLabel skipLabel = new JLabel("...");
					skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (row - skipRow + 1));
					skipLabel.setFont(font);
					skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
					panel.add(skipLabel);
					row += 1;
				}
			} else {
				JLabel materialLabel = new JLabel(material.name());
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST);
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

				int row = 1;
				for (ConfigHelper.Composition composition : helper.getCompositions()) {
					if(row < skipRow) {
						row += 1;
						continue;
					}
					if(row == skipRow && row != 1) {
						JLabel skipLabel = new JLabel("...");
						skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT));
						skipLabel.setFont(font);
						skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
						panel.add(skipLabel);
					} else {
						JComponent jComponent;
						if (material.compositions().containsKey(composition)) {
							JTextField textField = new JTextField();
							textField.setText(String.valueOf(material.compositions().get(composition).doubleValue()));
							textField.addActionListener(e -> material.compositions().replace(composition, Double.parseDouble(textField.getText())));
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
						jComponent.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (row - skipRow + 1));
						jComponent.setFont(font);
						jComponent.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
						panel.add(jComponent);
					}
					row += 1;
				}
			}
			col += 1;
		}
	}

	private static void buildPriceTable(JPanel panel, ConfigHelper helper) {
		JButton refresh = new JButton("刷新");
		refresh.setSize(80, 40);
		refresh.setLocation(800, 10);
		refresh.setFont(font);
		refresh.setFocusable(false);
		refresh.addActionListener(e -> refreshAll(panel, helper, Main::buildPriceTable));
		panel.add(refresh);

		JButton toConfig = new JButton("编辑约束");
		toConfig.setSize(120, 40);
		toConfig.setLocation(20, 10);
		toConfig.setFont(font);
		toConfig.setFocusable(false);
		toConfig.addActionListener(e -> {
			skipRow = skipColumn = 1;
			refreshAll(panel, helper, Main::buildSolveTable);
		});
		panel.add(toConfig);

		JSlider leftRightSlider = new JSlider(SwingConstants.HORIZONTAL, 1, helper.getMaterialCount() - 1, skipColumn);
		leftRightSlider.setSize(900, 40);
		leftRightSlider.setLocation(0, 480);
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

		JLabel title = new JLabel("每单位重量的原料价格");
		title.setSize(400, 40);
		title.setLocation(280, 10);
		title.setFont(fontTitle);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(title);

		JLabel label1 = new JLabel("原料");
		label1.setLocation(LEFT_DIST, TOP_DIST);
		label1.setFont(font);
		label1.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
		panel.add(label1);
		JLabel label2 = new JLabel("价格");
		label2.setLocation(LEFT_DIST, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT));
		label2.setFont(font);
		label2.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
		panel.add(label2);

		int col = 1;
		for(ConfigHelper.Material material: helper.getMaterials()) {
			if (col < skipColumn) {
				col += 1;
				continue;
			}
			if (col == skipColumn && col != 1) {
				JLabel materialLabel = new JLabel("...");
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH), TOP_DIST);
				materialLabel.setFont(font);
				materialLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(materialLabel);

				JPopupMenu menu = new JPopupMenu();
				menu.add("添加原料").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的原料名：");
					addColumn(helper, name);
					refreshAll(panel, helper, Main::buildPriceTable);
				});
				materialLabel.setComponentPopupMenu(menu);

				JLabel skipLabel = new JLabel("...");
				skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT));
				skipLabel.setFont(font);
				skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(skipLabel);
			} else {
				JLabel materialLabel = new JLabel(material.name());
				materialLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST);
				materialLabel.setFont(font);
				materialLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(materialLabel);

				JPopupMenu menu = new JPopupMenu();
				menu.add("删除原料").addActionListener(e -> {
					removeColumn(helper, material);
					refreshAll(panel, helper, Main::buildPriceTable);
				});
				menu.add("修改名称").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("原名称：%s\n请输入修改后的原料名：".formatted(material.name()));
					changeColumnName(helper, material, name);
					refreshAll(panel, helper, Main::buildPriceTable);
				});
				menu.add("移至末行").addActionListener(e -> {
					swapToLastColumn(helper, material);
					refreshAll(panel, helper, Main::buildPriceTable);
				});
				menu.add("添加原料").addActionListener(e -> {
					String name = JOptionPane.showInputDialog("请输入新的原料名：");
					addColumn(helper, name);
					refreshAll(panel, helper, Main::buildPriceTable);
				});
				materialLabel.setComponentPopupMenu(menu);

				JTextField textField = new JTextField();
				textField.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT));
				textField.setFont(font);
				textField.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				textField.setText(String.valueOf(material.price()));
				textField.addActionListener(e -> changePrice(helper, material, Double.parseDouble(textField.getText())));
				panel.add(textField);
			}
			col += 1;
		}
	}

	static final List<ConfigHelper.Constraint.ConstraintType> ALL_TYPES = Arrays.stream(ConfigHelper.Constraint.ConstraintType.values()).toList();

	private static void buildSolveTable(JPanel panel, ConfigHelper helper) {
		JButton refresh = new JButton("刷新");
		refresh.setSize(80, 40);
		refresh.setLocation(800, 10);
		refresh.setFont(font);
		refresh.setFocusable(false);
		refresh.addActionListener(e -> refreshAll(panel, helper, Main::buildSolveTable));
		panel.add(refresh);

		JButton compute = new JButton("开始计算！");
		compute.setSize(160, 40);
		compute.setLocation(720, 60);
		compute.setFont(font);
		compute.setFocusable(false);
		compute.addActionListener(e -> {
			ConfigHelper.LazyGenerator generator = helper.getLazyGenerator();
			Solver solver = new Solver(generator.getN(), generator.getM(), generator.getA(), generator.getB(), generator.getC());
			solver.solve();
			double[] answer = solver.getAnswer();
			StringBuilder builder = new StringBuilder("全局最优解（价格最小值）为：");
			builder.append(solver.getBest());
			builder.append("\n所有原料的购买量：\n");
			for(int i = 0; i < helper.getMaterialCount(); ++i) {
				builder.append("    %s购买%f；\n".formatted(helper.getMaterials().get(i).name(), answer[i]));
			}
			JOptionPane.showMessageDialog(null, builder.toString(), "计算结果", JOptionPane.INFORMATION_MESSAGE);
		});
		panel.add(compute);

		JSlider topDownSlider = new JSlider(SwingConstants.VERTICAL, 1, helper.getConstraintCount() - 1, skipRow);
		topDownSlider.setSize(40, 480);
		topDownSlider.setLocation(900, 0);
		topDownSlider.setInverted(true);
		topDownSlider.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {
				skipRow = topDownSlider.getValue();
				refreshAll(panel, helper, Main::buildSolveTable);
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		panel.add(topDownSlider);

		JButton toConfig = new JButton("编辑配置");
		toConfig.setSize(120, 40);
		toConfig.setLocation(20, 10);
		toConfig.setFont(font);
		toConfig.setFocusable(false);
		toConfig.addActionListener(e -> {
			skipRow = skipColumn = 1;
			refreshAll(panel, helper, Main::buildEditConfigTable);
		});
		panel.add(toConfig);

		JLabel title = new JLabel("原料约束");
		title.setSize(400, 40);
		title.setLocation(280, 10);
		title.setFont(fontTitle);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(title);

		int row = 1;
		for (ConfigHelper.Constraint constraint : helper.getConstraints()) {
			if(row < skipRow) {
				row += 1;
				continue;
			}
			if(row == skipRow && row != 1) {
				for(int col = 1; col <= 3; ++col) {
					JLabel skipLabel = new JLabel("...");
					skipLabel.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * (col - skipColumn + 1), TOP_DIST);
					skipLabel.setFont(font);
					skipLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
					panel.add(skipLabel);
				}
			} else {
				JLabel compositionLabel = new JLabel(constraint.composition().name());
				compositionLabel.setLocation(LEFT_DIST, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (row - skipRow));
				compositionLabel.setFont(font);
				compositionLabel.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				panel.add(compositionLabel);

				JPopupMenu menu = new JPopupMenu();
				menu.add("删除约束").addActionListener(e -> {
					removeConstraint(helper, constraint);
					refreshAll(panel, helper, Main::buildSolveTable);
				});
				menu.add("移至末行").addActionListener(e -> {
					swapToLastConstraint(helper, constraint);
					refreshAll(panel, helper, Main::buildSolveTable);
				});
				menu.add("添加约束").addActionListener(e -> {
					int index = JOptionPane.showOptionDialog(
							null, "请选择新的约束对应的成分名：", "添加约束",
							JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
							helper.getCompositions().toArray(), 0
					);
					if(index != JOptionPane.CLOSED_OPTION) {
						addConstraint(helper, helper.getCompositions().get(index));
						refreshAll(panel, helper, Main::buildSolveTable);
					}
				});
				menu.add("修改约束成分").addActionListener(e -> {
					int index = JOptionPane.showOptionDialog(
							null, "请选择修改对应的成分名：", "修改约束成分",
							JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
							helper.getCompositions().toArray(), 0
					);
					if(index != JOptionPane.CLOSED_OPTION) {
						changeConstraintIndex(helper, constraint, helper.getCompositions().get(index));
						refreshAll(panel, helper, Main::buildSolveTable);
					}
				});
				compositionLabel.setComponentPopupMenu(menu);

				JComboBox<ConfigHelper.Constraint.ConstraintType> typeBox = new JComboBox<>(ConfigHelper.Constraint.ConstraintType.values());
				typeBox.setSelectedIndex(ALL_TYPES.indexOf(constraint.type()));
				typeBox.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH), TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (row - skipRow));
				typeBox.setFont(font);
				typeBox.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				typeBox.addActionListener(e -> changeConstraintType(helper, constraint, ALL_TYPES.get(typeBox.getSelectedIndex())));
				panel.add(typeBox);

				JTextField textField = new JTextField();
				textField.setText(String.valueOf(constraint.constraint()));
				textField.setLocation(LEFT_DIST + (CELL_WIDTH + CELL_PADDING_WIDTH) * 2, TOP_DIST + (CELL_HEIGHT + CELL_PADDING_HEIGHT) * (row - skipRow));
				textField.setFont(font);
				textField.setSize(CELL_WIDTH, CELL_HEIGHT - CELL_MARGIN_HEIGHT);
				textField.addActionListener(e -> changeConstraint(helper, constraint, Double.parseDouble(textField.getText())));
				panel.add(textField);
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
			except("非法的成分“%s”".formatted(composition.name()), exception);
		}
	}

	private static void addRow(ConfigHelper helper, String name) {
		if(!ConfigHelper.verifyRegistry(helper.getCompositions(), name)) {
			error("重复的成分名：%s".formatted(name));
			return;
		}
		helper.getCompositions().add(new ConfigHelper.Composition(name));
	}

	private static void changeRowName(ConfigHelper helper, ConfigHelper.Composition composition, String name) {
		if(!ConfigHelper.verifyRegistry(helper.getCompositions(), name)) {
			error("重复的成分名：%s".formatted(name));
			return;
		}
		ConfigHelper.Composition newComposition = new ConfigHelper.Composition(name);

		try {
			helper.getCompositions().replaceAll(c -> {
				if(c == composition) {
					return newComposition;
				}
				return c;
			});
			for(ConfigHelper.Material material: helper.getMaterials()) {
				Double v = material.compositions().remove(composition);
				if(v != null) {
					material.compositions().put(newComposition, v);
				}
			}
			helper.getConstraints().replaceAll(c -> {
				if(c.composition() == composition) {
					return new ConfigHelper.Constraint(newComposition, c.constraint(), c.type());
				}
				return c;
			});
		} catch(RuntimeException exception) {
			except("非法的成分“%s”".formatted(composition.name()), exception);
		}
	}

	private static void removeRow(ConfigHelper helper, ConfigHelper.Composition composition) {
		try {
			helper.getCompositions().remove(composition);
			for(ConfigHelper.Material material: helper.getMaterials()) {
				material.compositions().remove(composition);
			}
		} catch(RuntimeException exception) {
			except("非法的成分“%s”".formatted(composition.name()), exception);
		}
	}

	private static void swapToLastColumn(ConfigHelper helper, ConfigHelper.Material material) {
		try {
			helper.getMaterials().remove(material);
			helper.getMaterials().add(material);
		} catch(RuntimeException exception) {
			except("非法的原料“%s”".formatted(material.name()), exception);
		}
	}

	private static void addColumn(ConfigHelper helper, String name) {
		if(!ConfigHelper.verifyRegistry(helper.getMaterials(), name)) {
			error("重复的原料名：%s".formatted(name));
			return;
		}
		helper.getMaterials().add(new ConfigHelper.Material(name, 0.0, Maps.newHashMap()));
	}

	private static void changeColumnName(ConfigHelper helper, ConfigHelper.Material material, String name) {
		ConfigHelper.Material newMaterial = new ConfigHelper.Material(name, material.price(), Maps.newHashMap(material.compositions()));

		try {
			helper.getMaterials().replaceAll(m -> {
				if(m == material) {
					return newMaterial;
				}
				return m;
			});
		} catch(RuntimeException exception) {
			except("非法的原料“%s”".formatted(material.name()), exception);
		}
	}

	private static void changePrice(ConfigHelper helper, ConfigHelper.Material material, double price) {
		ConfigHelper.Material newMaterial = new ConfigHelper.Material(material.name(), price, Maps.newHashMap(material.compositions()));

		try {
			helper.getMaterials().replaceAll(m -> {
				if(m == material) {
					return newMaterial;
				}
				return m;
			});
		} catch(RuntimeException exception) {
			except("非法的原料“%s”".formatted(material.name()), exception);
		}
	}

	private static void removeColumn(ConfigHelper helper, ConfigHelper.Material material) {
		try {
			helper.getMaterials().remove(material);
		} catch(RuntimeException exception) {
			except("非法的原料“%s”".formatted(material.name()), exception);
		}
	}

	private static void swapToLastConstraint(ConfigHelper helper, ConfigHelper.Constraint constraint) {
		try {
			helper.getConstraints().remove(constraint);
			helper.getConstraints().add(constraint);
		} catch(RuntimeException exception) {
			except("非法的约束“%s%s%f”".formatted(constraint.composition().name(), constraint.type().getCharacter(), constraint.constraint()), exception);
		}
	}

	private static void changeConstraint(ConfigHelper helper, ConfigHelper.Constraint constraint, double value) {
		ConfigHelper.Constraint newConstraint = new ConfigHelper.Constraint(constraint.composition(), value, constraint.type());

		try {
			helper.getConstraints().replaceAll(m -> {
				if(m == constraint) {
					return newConstraint;
				}
				return m;
			});
		} catch(RuntimeException exception) {
			except("非法的约束“%s%s%f”".formatted(constraint.composition().name(), constraint.type().getCharacter(), constraint.constraint()), exception);
		}
	}

	private static void changeConstraintType(ConfigHelper helper, ConfigHelper.Constraint constraint, ConfigHelper.Constraint.ConstraintType type) {
		ConfigHelper.Constraint newConstraint = new ConfigHelper.Constraint(constraint.composition(), constraint.constraint(), type);

		try {
			helper.getConstraints().replaceAll(m -> {
				if(m == constraint) {
					return newConstraint;
				}
				return m;
			});
		} catch(RuntimeException exception) {
			except("非法的约束“%s%s%f”".formatted(constraint.composition().name(), constraint.type().getCharacter(), constraint.constraint()), exception);
		}
	}

	private static void addConstraint(ConfigHelper helper, ConfigHelper.Composition composition) {
		helper.getConstraints().add(new ConfigHelper.Constraint(composition, 0.0, ConfigHelper.Constraint.ConstraintType.LESS_THAN));
	}

	private static void changeConstraintIndex(ConfigHelper helper, ConfigHelper.Constraint constraint, ConfigHelper.Composition composition) {
		ConfigHelper.Constraint newConstraint = new ConfigHelper.Constraint(composition, constraint.constraint(), constraint.type());

		try {
			helper.getConstraints().replaceAll(c -> {
				if(c == constraint) {
					return newConstraint;
				}
				return c;
			});
		} catch(RuntimeException exception) {
			except("非法的约束“%s%s%f”".formatted(constraint.composition().name(), constraint.type().getCharacter(), constraint.constraint()), exception);
		}
	}

	private static void removeConstraint(ConfigHelper helper, ConfigHelper.Constraint constraint) {
		try {
			helper.getConstraints().remove(constraint);
		} catch(RuntimeException exception) {
			except("非法的约束“%s%s%f”".formatted(constraint.composition().name(), constraint.type().getCharacter(), constraint.constraint()), exception);
		}
	}

	@SuppressWarnings("unused")
	public static void debug(@Nonnull String message) {
		if(DEBUG) {
			System.out.println(message);
		}
	}

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void log(@Nonnull String message) {
		System.out.println(message);

		if(logWriter != null) {
			try {
				logWriter.write("[%s][LOG] ".formatted(dateFormat.format(System.currentTimeMillis())) + message + '\n');
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public static void error(@Nonnull String message) {
		JOptionPane.showMessageDialog(null, message, "错误！", JOptionPane.ERROR_MESSAGE);
		System.err.println(message);
		if(logWriter != null) {
			try {
				logWriter.write("[%s][ERR] ".formatted(dateFormat.format(System.currentTimeMillis())) + message + '\n');
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public static void except(@Nonnull Throwable exc) {
		error(exc.getMessage());
		if(logWriter != null) {
			try {
				logWriter.write("[%s][EXC] ".formatted(dateFormat.format(System.currentTimeMillis())) + exc + '\n');
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	public static void except(@Nonnull String message, @Nonnull Throwable exc) {
		error(message);
		if(logWriter != null) {
			try {
				logWriter.write("[%s][EXC] ".formatted(dateFormat.format(System.currentTimeMillis())) + exc + '\n');
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}
