package com.scudata.ide.spl;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.scudata.app.common.AppConsts;
import com.scudata.app.common.AppUtil;
import com.scudata.app.common.Segment;
import com.scudata.cellset.datamodel.CellSet;
import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.Logger;
import com.scudata.common.Matrix;
import com.scudata.common.StringUtils;
import com.scudata.ide.common.ConfigFile;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.ConfigUtilIde;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.IAtomicCmd;
import com.scudata.ide.common.PrjxAppMenu;
import com.scudata.ide.common.PrjxAppToolBar;
import com.scudata.ide.common.control.CellRect;
import com.scudata.ide.common.control.CellSelection;
import com.scudata.ide.common.control.ControlUtilsBase;
import com.scudata.ide.common.dialog.DialogDataSource;
import com.scudata.ide.common.dialog.DialogFileReplace;
import com.scudata.ide.common.dialog.DialogMemory;
import com.scudata.ide.spl.control.CellSetParser;
import com.scudata.ide.spl.control.ControlUtils;
import com.scudata.ide.spl.control.SplEditor;
import com.scudata.ide.spl.dialog.DialogAbout;
import com.scudata.ide.spl.dialog.DialogExecCmd;
import com.scudata.ide.spl.resources.IdeSplMessage;
import com.scudata.ide.spl.update.UpdateManager;

public class GMSpl extends GM {

	/**
	 * ִ�в˵�����Sheet����
	 * 
	 * @param cmd
	 *            GCSpl��GC�ж���Ĳ˵�����
	 * @throws Exception
	 */
	public static void executeCmd(short cmd) throws Exception {
		switch (cmd) {
		case GC.iNEW:
			GV.appFrame.openSheetFile(null);
			return;
		case GCSpl.iNEW_SPL:
			GV.appFrame.openSheetFile("");
			return;
		case GC.iOPEN:
			String ext = "\"" + AppConsts.SPL_FILE_EXTS + "\"";
			File file = GM.dialogSelectFile(ext);
			if (file != null) {
				GV.appFrame.openSheetFile(file.getAbsolutePath());
			}
			return;
		case GC.iSAVE:
			GV.appSheet.save();
			return;
		case GC.iSAVEAS:
			GV.appSheet.saveAs();
			return;
		case GC.iSAVEALL:
			((SPL) GV.appFrame).saveAll();
			return;
		case GC.iFILE_CLOSE:
		case GC.iFILE_CLOSE1:
			if (GV.appSheet != null) {
				GV.appFrame.closeSheet(GV.appSheet);
			}
			return;
		case GC.iFILE_CLOSE_ALL:
			GV.appFrame.closeAll();
			return;
			// case GCSpl.iSPL_IMPORT_TXT:
			// importTxt2Spl();
			// return;
		case GC.iQUIT:
			GV.appFrame.quit();
			return;
		case GC.iDATA_SOURCE:
			DialogDataSource dds = new DialogDataSource(GV.dsModel);
			dds.setVisible(true);
			try {
				ConfigUtilIde.writeConfig();
			} catch (Exception ex) {
				Logger.debug(ex);
			}
			if (GVSpl.tabParam != null) {
				GVSpl.tabParam.resetEnv();
			}
			return;
		case GC.iOPTIONS:
			boolean showDB = ConfigOptions.bShowDBStruct;
			new com.scudata.ide.spl.dialog.DialogOptions().setVisible(true);
			((SPL) GV.appFrame).refreshOptions();
			if (showDB != ConfigOptions.bShowDBStruct) {
				if (GVSpl.tabParam != null) {
					GVSpl.tabParam.resetEnv();
				}
			}
			return;
		case GCSpl.iFILE_REPLACE:
			DialogFileReplace dfr = new DialogFileReplace(GV.appFrame);
			dfr.setVisible(true);
			return;
		case GC.iSHOW_WINLIST:
			((SPL) GV.appFrame).switchWinList();
			GM.resetAllSheetStyle();
			return;
		case GC.iVIEW_CONSOLE:
			((SPL) GV.appFrame).viewLeft();
			return;
		case GC.iVIEW_RIGHT:
			((SPL) GV.appFrame).viewRight();
			return;
		case GC.iCASCADE:
		case GC.iTILEHORIZONTAL:
		case GC.iTILEVERTICAL:
		case GC.iLAYER:
			GV.appFrame.arrangeSheet(cmd);
			GM.resetAllSheetStyle();
			return;
		case GC.iABOUT:
			new DialogAbout().setVisible(true);
			return;
		case GC.iCHECK_UPDATE:
			try {
				UpdateManager.checkUpdate(false);
			} catch (Exception e) {
				GM.showException(e, true, GM.getLogoImage(true), IdeSplMessage
						.get().getMessage("spl.updateerrorpre"));
			}
			return;
		case GC.iMEMORYTIDY:
			if (GV.dialogMemory == null) {
				GV.dialogMemory = new DialogMemory();
				GV.dialogMemory
						.setWrapStringBuffer(ControlUtilsBase.wrapStringBuffer);
			}
			GV.dialogMemory.setVisible(true);
			return;
			// case GCSpl.iFILE_EXPORTTXT:
			// ((SheetSpl) GV.appSheet).exportTxt();
			// return;
			// case GCSpl.iFUNC_MANAGER:
			// DialogFuncEditor dfe = new DialogFuncEditor(GV.appFrame, false);
			// dfe.setVisible(true);
			// return;
		}
		if (cmd == GCSpl.iEXEC_CMD) {
			if (GV.appSheet == null) {
				DialogExecCmd dec = new DialogExecCmd();
				dec.setVisible(true);
				return;
			}
		}
		// sheet commands
		GV.appSheet.executeCmd(cmd);
	}

	/**
	 * �������ָ������ȡ��Ԫ�����
	 * 
	 * @param cellSet
	 *            ����
	 * @param rect
	 *            ����
	 * @return
	 */
	public static Matrix getMatrixCells(CellSet cellSet, CellRect rect) {
		return getMatrixCells(cellSet, rect, true);
	}

	/**
	 * �������ָ������ȡ��Ԫ�����
	 * 
	 * @param cellSet
	 *            ����
	 * @param rect
	 *            ����
	 * @param cloneCell
	 *            ��Ԫ���Ƿ��¡
	 * @return
	 */
	public static Matrix getMatrixCells(CellSet cellSet, CellRect rect,
			boolean cloneCell) {
		if (rect == null) {
			return null;
		}
		int rowSize = 0;
		CellSetParser csp = new CellSetParser(cellSet);
		for (int i = 0; i < rect.getRowCount(); i++) {
			if (csp.isRowVisible(rect.getBeginRow() + i)) {
				rowSize++;
			}
		}
		int colSize = 0;
		for (int j = 0; j < rect.getColCount(); j++) {
			if (csp.isColVisible((int) (j + rect.getBeginCol()))) {
				colSize++;
			}
		}
		if (rowSize == 0 || colSize == 0) {
			return null;
		}
		Matrix m = new Matrix(rowSize, colSize);
		NormalCell nc;
		int rs = 0;
		for (int i = 0; i < rect.getRowCount(); i++) {
			int row = rect.getBeginRow() + i;
			if (!csp.isRowVisible(row)) {
				continue;
			}
			int cs = 0;
			for (int j = 0; j < rect.getColCount(); j++) {
				int col = (int) (j + rect.getBeginCol());
				if (!csp.isColVisible(col)) {
					continue;
				}
				NormalCell temp = (NormalCell) cellSet.getCell(row, col);
				if (cloneCell) {
					nc = (NormalCell) temp.deepClone();
				} else {
					nc = (NormalCell) temp;
				}
				nc.setValue(GM.getOptionTrimChar0Value(temp.getValue()));
				m.set(rs, cs, nc);
				cs++;
			}
			rs++;
		}
		return m;
	}

	/**
	 * ȡ�ƶ�������ӵ�ԭ�������
	 * 
	 * @param editor
	 *            ����༭��
	 * @param srcRect
	 *            Դ����
	 * @param tarRect
	 *            Ŀ������
	 * @return
	 */
	public static Vector<IAtomicCmd> getMoveRectCmd(SplEditor editor,
			CellRect srcRect, CellRect tarRect) {
		if (srcRect.getColCount() == 0) {
			return null;
		}
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		CellSet ics = editor.getComponent().getCellSet();

		int cols = tarRect.getEndCol() - ics.getColCount();
		if (cols > 0) {
			return null;
		}
		int rows = tarRect.getEndRow() - ics.getRowCount();
		if (rows > 0) {
			cmds.add(editor.getAppendRows(rows));
		}

		Matrix srcCells = getMatrixCells(ics, srcRect);
		CellSelection cs = new CellSelection(srcCells, srcRect, editor
				.getComponent().getCellSet());
		AtomicSpl ad = new AtomicSpl(editor.getComponent());
		ad.setType(AtomicSpl.MOVE_RECT);
		ad.setRect(tarRect);
		ad.setValue(cs);
		cmds.add(ad);
		return cmds;
	}

	/**
	 * ȡ�����п���
	 * 
	 * @param cs
	 *            ����
	 * @param col
	 *            ��
	 * @return
	 */
	public static float getMaxColWidth(CellSet cs, int col) {
		if (cs == null || cs.getColCount() < col || col < 1) {
			return -1;
		}
		int rc = cs.getRowCount();
		NormalCell nc;
		String cellText;
		float maxWidth = -1, temp;
		Font font = GC.font;
		for (int row = 1; row <= rc; row++) {
			nc = (NormalCell) cs.getCell(row, col);
			if (nc == null) {
				continue;
			}
			cellText = nc.getExpString();
			if (cellText == null) {
				continue;
			}
			temp = ControlUtils.getStringMaxWidth(cellText, font);
			if (maxWidth < temp) {
				maxWidth = temp;
			}
		}
		if (maxWidth < GCSpl.MIN_COL_WIDTH) {
			return GCSpl.MIN_COL_WIDTH;
		}
		return maxWidth;
	}

	/**
	 * ȡ�����и߶�
	 * 
	 * @param cs
	 *            ����
	 * @param row
	 *            �к�
	 * @return
	 */
	public static float getMaxRowHeight(CellSet cs, int row) {
		if (cs == null || cs.getRowCount() < row || row < 1) {
			return -1;
		}
		CellSetParser parser = new CellSetParser(cs);
		int cc = cs.getColCount();
		NormalCell nc;
		String cellText;
		float maxHeight = -1, temp;
		for (int col = 1; col <= cc; col++) {
			nc = (NormalCell) cs.getCell(row, col);
			if (nc == null) {
				continue;
			}
			Font font = GC.font;
			cellText = nc.getExpString();
			if (cellText == null) {
				continue;
			}
			float width = parser.getColWidth(col);
			temp = ControlUtils.getStringHeight(cellText, width, font);
			if (maxHeight < temp) {
				maxHeight = temp;
			}
		}
		if (maxHeight < GCSpl.MIN_ROW_HEIGHT) {
			return GCSpl.MIN_ROW_HEIGHT;
		}
		return maxHeight;
	}

	/**
	 * �ӳ�������ҳ���޸�״̬
	 */
	public static void invokeSheetChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GV.appSheet.setChanged(true);
			}
		});
	}

	/**
	 * �����ı��ļ���spl�ļ�
	 * 
	 * @return
	 */
	// public static boolean importTxt2Spl() {
	// File file = GM.dialogSelectFile(AppConsts.FILE_SPL);
	// if (file == null) {
	// return false;
	// }
	// try {
	// String filePath = file.getAbsolutePath();
	// PgmCellSet cellSet = readSPL(filePath);
	// filePath = getNotDuplicateName(filePath, AppConsts.FILE_SPL);
	// ((SPL) GV.appFrame).openSheet(filePath, cellSet, false);
	// invokeSheetChanged();
	// } catch (Throwable ex) {
	// GM.showException(ex);
	// return false;
	// }
	// return true;
	// }

	/**
	 * ��ȡSPL�ļ�����������
	 * 
	 * @param filePath
	 *            SPL�ļ�·��
	 * @return
	 * @throws Exception
	 */
	public static PgmCellSet readSPL(String filePath) throws Exception {
		PgmCellSet cellSet = AppUtil.readSPL(filePath);
		if (cellSet == null) {
			return new PgmCellSet(ConfigOptions.iRowCount.intValue(),
					ConfigOptions.iColCount.intValue());
		}
		return cellSet;
	}

	/**
	 * ȡ���ظ���spl����
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * @param postfix
	 *            ��׺
	 * @return
	 */
	private static String getNotDuplicateName(String filePath, String postfix) {
		String preName = filePath;
		if (postfix != null && filePath.endsWith("." + postfix)) {
			preName = filePath.substring(0, preName.length() - postfix.length()
					- 1);
		}
		String newName = preName;
		int index = 1;
		while (((SPL) GV.appFrame).getSheet(newName) != null) {
			newName = preName + index;
			index++;
		}
		return newName;
	}

	/**
	 * Set Locale according to options
	 * 
	 */
	public static void setOptionLocale() {
		try {
			ConfigFile cf = ConfigFile.getConfigFile();
			cf.setConfigNode(ConfigFile.NODE_OPTIONS);
			String val = cf.getAttrValue("iLocale");
			if (StringUtils.isValidString(val)) {
				Byte ii = Byte.valueOf(val);
				if (ii != null) {
					ConfigOptions.iLocale = ii;
				}
			}
			/* Currently there are only two versions in Chinese and English */
			if (ConfigOptions.iLocale != null) {
				switch (ConfigOptions.iLocale.byteValue()) {
				case GC.ASIAN_CHINESE:
					Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
					break;
				default:
					Locale.setDefault(Locale.ENGLISH);
					break;
				}
				GC.initLocale();
			} else {
				if (GC.LANGUAGE == GC.ASIAN_CHINESE) {
				} else if (GC.LANGUAGE != GC.ENGLISH) {
					ConfigOptions.iLocale = new Byte(GC.ENGLISH);
					Locale.setDefault(Locale.ENGLISH);
					GC.initLocale();
				}
			}
		} catch (Throwable e) {
			Locale.setDefault(Locale.ENGLISH);
			e.printStackTrace();
		}
		GC.resetLocal();
	}

	/**
	 * ȡ����������
	 * 
	 * @return
	 */
	public static String getNewName() {
		String pre = GCSpl.PRE_NEWPGM;
		return getNewName(pre);
	}

	/**
	 * ȡ���ظ�������������
	 * 
	 * @param pre
	 * @return
	 */
	public static String getNewName(String pre) {
		String[] titles = ((SPL) GV.appFrame).getSheetTitles();
		ArrayList<String> names = new ArrayList<String>();
		if (titles != null) {
			for (int i = 0; i < titles.length; i++) {
				names.add(titles[i]);
			}
		}
		int index = 1;
		while (names.contains(pre + index)) {
			index++;
		}
		return pre + index;
	}

	/**
	 * ����水ť
	 */
	public static void enableSave() {
		if (GVSpl.splEditor != null)
			GVSpl.splEditor.setDataChanged(true);
		enableSave(true);
	}

	/**
	 * �޸ı��水ť״̬
	 * 
	 * @param isDataChanged
	 *            �Ƿ񼤻�水ť
	 */
	public static void enableSave(boolean isDataChanged) {
		if (GV.appMenu != null) {
			((PrjxAppMenu) GV.appMenu).enableSave(isDataChanged);
		}
		if (GV.appTool != null) {
			((PrjxAppToolBar) GV.appTool).enableSave(isDataChanged);
		}
	}

	/**
	 * config.txt�ļ��еļ���
	 */
	private static final String KEY_JVM = "jvm_args";
	private static final String KEY_XMX = "-xmx";
	private static final String KEY_XMS = "-xms";

	/**
	 * ȡ����ڴ�
	 * 
	 * @return
	 */
	public static String getXmx() {
		String jvmArgs = getConfigValue(KEY_JVM);
		if (jvmArgs == null)
			return null;
		String[] args = jvmArgs.split(" ");
		if (args == null)
			return null;
		for (int i = 0; i < args.length; i++) {
			if (StringUtils.isValidString(args[i])) {
				args[i] = args[i].trim();
				if (args[i].toLowerCase().startsWith(KEY_XMX)) {
					String xmx = args[i].substring(KEY_XMX.length());
					if (StringUtils.isValidString(xmx))
						return xmx.trim();
					else
						return null;
				}
			}
		}
		return null;
	}

	/**
	 * ��������ڴ�
	 * 
	 * @param xmx
	 */
	public static void setXmx(String xmx) {
		if (!StringUtils.isValidString(xmx))
			return;
		xmx = xmx.trim();
		try {
			Integer.parseInt(xmx);
			xmx += "m"; // ûд��λƴ��M
		} catch (Exception e) {
		}
		String jvmArgs = getConfigValue(KEY_JVM);
		if (jvmArgs == null)
			return;
		String[] args = jvmArgs.split(" ");
		if (args == null)
			return;
		for (int i = 0; i < args.length; i++) {
			if (StringUtils.isValidString(args[i])) {
				args[i] = args[i].trim();
				if (args[i].toLowerCase().startsWith(KEY_XMX)) {
					jvmArgs = jvmArgs.replaceFirst(args[i], "-Xmx" + xmx);
				} else if (args[i].toLowerCase().startsWith(KEY_XMS)) {
					// xmsҲ���ó�xmxһ����С
					jvmArgs = jvmArgs.replaceFirst(args[i], "-Xms" + xmx);
				}
			}
		}
		setConfigValue(KEY_JVM, jvmArgs);
	}

	/**
	 * ��config.txt�ļ���ȡָ������ֵ
	 * 
	 * @param key
	 * @return
	 */
	public static String getConfigValue(String key) {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			String configFile = GM.getAbsolutePath("bin" + File.separator
					+ "config.txt");
			fr = new FileReader(configFile);
			br = new BufferedReader(fr);
			String segValue = br.readLine();
			Segment seg = new Segment(segValue);
			return seg.get(key);
		} catch (Exception x) {
			// Logger.debug(x.getMessage(), x);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * ����config.xml�ļ��еļ�ֵ
	 * 
	 * @param key
	 * @param value
	 */
	public static void setConfigValue(String key, String value) {
		String configFile = GM.getAbsolutePath("bin" + File.separator
				+ "config.txt");
		FileReader fr = null;
		BufferedReader br = null;
		Segment seg = null;
		try {
			fr = new FileReader(configFile);
			br = new BufferedReader(fr);
			String segValue = br.readLine();
			seg = new Segment(segValue);
		} catch (Exception e) {
			GM.showException(e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}
		if (seg == null)
			return;
		seg.put(key, value, false);
		FileWriter fw = null;
		BufferedWriter writer = null;
		try {
			fw = new FileWriter(new File(configFile));
			writer = new BufferedWriter(fw);
			writer.write(seg.toString());
			writer.close();
		} catch (Exception e) {
			GM.showException(e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}
		}
	}
}