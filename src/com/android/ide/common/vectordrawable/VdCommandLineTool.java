/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ide.common.vectordrawable;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


class VdCommandLineOptions {
    // Support a command line tool to convert or show the VectorDrawable.
    // Below are the options or information for this tool.
    private static final String OPTION_CONVERT = "-c";
    private static final String OPTION_DISPLAY = "-d";
    private static final String OPTION_IN = "-in";
    private static final String OPTION_OUT = "-out";
    private static final String OPTION_FORCE_WIDTH_DP = "-widthDp";
    private static final String OPTION_FORCE_HEIGHT_DP = "-heightDp";
    private static final String OPTION_ADD_HEADER = "-addHeader";
    public static final String COMMAND_LINE_OPTION = "Converts SVG files to VectorDrawable XML files.\n"
            + "Displays VectorDrawables.\n"
            + "Usage: [-c] [-d] [-in <file or directory>] [-out <directory>] [-widthDp <size>] "
            + "[-heightDp <size>] [-addHeader]\n"
            + "Options:\n"
            + "  -in <file or directory>:  If -c is specified, Converts the given .svg file \n"
            + "                            to VectorDrawable XML, or if a directory is specified,\n"
            + "                            all .svg files in the given directory. Otherwise, if -d\n"
            + "                            is specified, displays the given VectorDrawable XML file\n"
            + "                            or all VectorDrawables in the given directory.\n"
            + "  -out <directory>          If specified, write converted files out to the given\n"
            + "                            directory, which must exist. If not specified the\n"
            + "                            converted files will be written to the directory\n"
            + "                            containing the input files.\n"
            + "  -c                        If present, SVG files are converted to VectorDrawable XML\n"
            + "                            and written out.\n"
            + "  -d                        Displays the given VectorDrawable(s), or if -c is\n"
            + "                            specified the results of the conversion.\n"
            + "  -widthDp <size>           Force the width to be <size> dp, <size> must be integer\n"
            + "  -heightDp <size>          Force the height to be <size> dp, <size> must be integer\n"
            + "  -addHeader                Add AOSP header to the top of the generated XML file\n"
            + "Examples:                   \n"
            + "  1) Convert SVG files from <directory> into XML files at the same directory"
            + " and visualize the XML file results:\n"
            + "  vd-tool -c -d -in <directory> \n"
            + "  2) Convert SVG file and visualize the XML file results:\n"
            + "  vd-tool -c -d -in file.svg \n"
            + "  3) Display VectorDrawable's XML files from <directory>:\n"
            + "  vd-tool -d -in <directory> \n"
            ;
    private boolean mConvertSvg;
    private File[] mInputFiles;
    private File mOutputDir;
    private boolean mDisplayXml;
    public int getForceWidth() {
        return mForceWidth;
    }
    public int getForceHeight() {
        return mForceHeight;
    }
    public boolean isAddHeader() {
        return mAddHeader;
    }
    private int mForceWidth = -1;
    private int mForceHeight = -1;
    private boolean mAddHeader = false;
    public boolean getDisplayXml() {
        return mDisplayXml;
    }
    public boolean getConvertSvg() {
        return mConvertSvg;
    }
    public File[] getInputFiles() {
        return mInputFiles;
    }
    public File getOutputDir() {
        return mOutputDir;
    }
    /**
     * Parse the command line options.
     *
     * @param args the incoming command line options
     * @return null if no critical error happens, otherwise the error message.
     */
    public String parse(String[] args) {
        File argIn = null;
        mOutputDir = null;
        mConvertSvg = false;
        mDisplayXml = false;
        // First parse the command line options.
        if (args != null && args.length > 0) {
            int index = 0;
            while (index < args.length) {
                String currentArg = args[index];
                if (OPTION_CONVERT.equalsIgnoreCase(currentArg)) {
                    System.out.println(OPTION_CONVERT + " parsed, so we will convert the SVG files");
                    mConvertSvg = true;
                } else if (OPTION_DISPLAY.equalsIgnoreCase(currentArg)) {
                    System.out.println(OPTION_DISPLAY + " parsed, so we will display the XML files");
                    mDisplayXml = true;
                } else if (OPTION_IN.equalsIgnoreCase(currentArg)) {
                    if ((index + 1) < args.length) {
                        argIn = new File(args[index + 1]);
                        System.out.println(OPTION_IN + " parsed " + argIn.getAbsolutePath());
                        index++;
                    }
                } else if (OPTION_OUT.equalsIgnoreCase(currentArg)) {
                    if ((index + 1) < args.length) {
                        mOutputDir = new File(args[index + 1]
                                .replaceFirst("^~", System.getProperty("user.home")));
                        System.out.println(OPTION_OUT + " parsed " + mOutputDir.getAbsolutePath());
                        index++;
                    }
                }  else if (OPTION_FORCE_WIDTH_DP.equalsIgnoreCase(currentArg)) {
                    if ((index + 1) < args.length) {
                        mForceWidth = Integer.parseInt(args[index + 1]);
                        System.out.println(OPTION_FORCE_WIDTH_DP + " parsed " + mForceWidth);
                        index++;
                    }
                }  else if (OPTION_FORCE_HEIGHT_DP.equalsIgnoreCase(currentArg)) {
                    if ((index + 1) < args.length) {
                        mForceHeight = Integer.parseInt(args[index + 1]);
                        System.out.println(OPTION_FORCE_HEIGHT_DP + " parsed " + mForceHeight);
                        index++;
                    }
                }  else if (OPTION_ADD_HEADER.equalsIgnoreCase(currentArg)) {
                    mAddHeader = true;
                    System.out.println(OPTION_ADD_HEADER + " parsed, add AOSP header to the XML file");
                } else {
                    return "ERROR: unrecognized option " + currentArg;
                }
                index++;
            }
        } else {
            return "ERROR: empty arguments";
        }
        if (!mConvertSvg && !mDisplayXml) {
            return "ERROR: either " + OPTION_CONVERT + " or " + OPTION_DISPLAY + " must be specified";
        }
        // Then we decide the input resources.
        mInputFiles = null;
        if (argIn != null) {
            if (argIn.isFile()) {
                mInputFiles = new File[1];
                mInputFiles[0] = argIn;
                if (mOutputDir == null && mConvertSvg) {
                    mOutputDir = argIn.getParentFile();
                }
            } else if (argIn.isDirectory()) {
                File parsedSVGDir = argIn;
                mInputFiles = parsedSVGDir.listFiles();
                // Sort the files by the name.
                Arrays.sort(mInputFiles);
                if (mOutputDir == null && mConvertSvg) {
                    mOutputDir = argIn;
                }
            }
        } else {
            return "ERROR: no input files argument";
        }
        if (mConvertSvg) {
            if (mOutputDir != null) {
                if (!mOutputDir.isDirectory()) {
                    return ("ERROR: Output directory " + mOutputDir.getAbsolutePath()
                            + " doesn't exist or isn't a valid directory");
                }
            } else {
                return "ERROR: no output directory specified";
            }
        }
        if (mInputFiles != null && mInputFiles.length == 0) {
            return "ERROR: There is no file to process in " + argIn.getName();
        }
        return null;
    }
}
/**
 * Support a command line tool to convert SVG files to VectorDrawables and display them.
 */
public class VdCommandLineTool {
    // Show Vector Drawables in a table, below are the parameters for the table.
    private static final int COLUMN_NUMBER = 8;
    private static final int ROW_HEIGHT = 80;
    private static final Dimension MAX_WINDOW_SIZE = new Dimension(600, 600);
    public static final String BROKEN_FILE_EXTENSION = ".broken";
    private static final boolean DBG_COPY_BROKEN_SVG = false;
    private static void exitWithErrorMessage(String message) {
        System.err.println(message);
        System.exit(-1);
    }
    public static void main(String[] args) {
        VdCommandLineOptions options = new VdCommandLineOptions();
        String criticalError = options.parse(args);
        if (criticalError != null) {
            exitWithErrorMessage(criticalError + "\n\n" + VdCommandLineOptions.COMMAND_LINE_OPTION);
        }
        boolean needsConvertSvg = options.getConvertSvg();
        boolean needsDisplayXml = options.getDisplayXml();
        File[] filesToDisplay = options.getInputFiles();
        if (needsConvertSvg) {
            filesToDisplay = convertSVGToXml(options);
        }
        if (needsDisplayXml) {
            displayXmlAsync(filesToDisplay);
        }
    }
    private static void displayXmlAsync(final File[] displayFiles) {
        SwingUtilities.invokeLater(() -> displayXml(displayFiles));
    }
    private static class MyTableModel extends AbstractTableModel {
        private VdIcon[] mIconList;
        public MyTableModel(VdIcon[] iconList) {
            mIconList = iconList;
        }
        @Override
        public String getColumnName(int column) {
            return null;
        }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int index = rowIndex * COLUMN_NUMBER + columnIndex;
            if (index < 0) {
                return null;
            }
            return mIconList.length > index ? mIconList[index] : null;
        }
        @Override
        public int getRowCount() {
            return mIconList.length / COLUMN_NUMBER +
                    ((mIconList.length % COLUMN_NUMBER == 0) ? 0 : 1);
        }
        @Override
        public int getColumnCount() {
            return Math.min(COLUMN_NUMBER, mIconList.length);
        }
        @Override
        public Class<?> getColumnClass(int column) {
            return Icon.class;
        }
    }
    private static void displayXml(File[] inputFiles) {
        ArrayList<VdIcon> iconList = new ArrayList<>();
        int validXmlFileCounter = 0;
        for (File xmlFile : inputFiles) {
            if (!xmlFile.isFile() || xmlFile.length() == 0) {
                continue;
            }
            String xmlFilename = xmlFile.getName();
            if (xmlFilename.isEmpty()) {
                continue;
            }
            if (!xmlFilename.endsWith(SdkConstants.DOT_XML)) {
                continue;
            }
            try {
                VdIcon icon = new VdIcon(xmlFile.toURI().toURL());
                icon.enableCheckerBoardBackground(true);
                iconList.add(icon);
            } catch (IllegalArgumentException | IOException e) {
                e.printStackTrace();
            }
            validXmlFileCounter++;
        }
        System.out.println("Showing " + validXmlFileCounter + " valid icons");
        MyTableModel model = new MyTableModel(iconList.toArray(new VdIcon[0]));
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                JComponent c = (JComponent)super.prepareRenderer(renderer, row, column);
                VdIcon icon = (VdIcon)getValueAt(row, column);
                c.setToolTipText(icon != null ? icon.getName() : null);
                return c;
            }
        };
        table.setOpaque(false);
        table.setPreferredScrollableViewportSize(MAX_WINDOW_SIZE);
        table.setRowHeight(ROW_HEIGHT);
        table.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                // do nothing
            }
        });
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (table.getPreferredSize().getHeight() > MAX_WINDOW_SIZE.getHeight()) {
            JScrollPane pane = new JScrollPane(table);
            frame.getContentPane().add(pane);
        } else {
            frame.getContentPane().add(table);
        }
        frame.pack();
        frame.setVisible(true);
    }
    private static final String AOSP_HEADER = "<!--\n" +
            "Copyright (C) " + Calendar.getInstance().get(Calendar.YEAR) +
            " The Android Open Source Project\n\n" +
            "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            "    you may not use this file except in compliance with the License.\n" +
            "    You may obtain a copy of the License at\n\n" +
            "         http://www.apache.org/licenses/LICENSE-2.0\n\n" +
            "    Unless required by applicable law or agreed to in writing, software\n" +
            "    distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            "    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            "    See the License for the specific language governing permissions and\n" +
            "    limitations under the License.\n" +
            "-->\n";
    private static File[] convertSVGToXml(VdCommandLineOptions options) {
        File[] inputSVGFiles = options.getInputFiles();
        File outputDir = options.getOutputDir();
        int totalSvgFileCounter = 0;
        int errorSvgFileCounter = 0;
        ArrayList<File> allOutputFiles = new ArrayList<>();
        for (File inputSVGFile : inputSVGFiles) {
            String svgFilename = inputSVGFile.getName();
            if (!svgFilename.endsWith(SdkConstants.DOT_SVG)) {
                continue;
            }
            String svgFilenameWithoutExtension = svgFilename.substring(0,
                    svgFilename.lastIndexOf('.'));
            File outputFile = new File(outputDir,
                    svgFilenameWithoutExtension + SdkConstants.DOT_XML);
            allOutputFiles.add(outputFile);
            try {
                ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
                String error = Svg2Vector.parseSvgToXml(Paths.get(inputSVGFile.getAbsolutePath()), byteArrayOutStream);
                if (!error.isEmpty()) {
                    errorSvgFileCounter++;
                    System.err.println("error is " + error);
                    if (DBG_COPY_BROKEN_SVG) {
                        // Copy the broken svg file in the same directory but with a new extension.
                        String brokenFileName = svgFilename + BROKEN_FILE_EXTENSION;
                        File brokenSvgFile = new File(outputDir, brokenFileName);
                      //  Files.copy(inputSVGFile, brokenSvgFile);
                    }
                }
                // Override the size info if needed. Negative value will be ignored.
                String vectorXmlContent = byteArrayOutStream.toString();
                if (options.getForceHeight() > 0 || options.getForceWidth() > 0) {
                    Document vdDocument = parseVdStringIntoDocument(vectorXmlContent, null);
                    if (vdDocument != null) {
                        VdOverrideInfo info = new VdOverrideInfo(options.getForceWidth(),
                                options.getForceHeight(), null, 1,
                                false /*auto mirrored*/);
                        vectorXmlContent = VdPreview.overrideXmlContent(vdDocument, info, null);
                    }
                }
                if (options.isAddHeader()) {
                    vectorXmlContent = AOSP_HEADER + vectorXmlContent;
                }
                // Write the final result into the output XML file.
                try (PrintWriter writer = new PrintWriter(outputFile)) {
                    writer.print(vectorXmlContent);
                }
            } catch (Exception e) {
                System.err.println("exception" + e.getMessage());
                e.printStackTrace();
            }
            totalSvgFileCounter++;
        }
        System.out.println("Convert " + totalSvgFileCounter + " SVG files in total, errors found in "
                + errorSvgFileCounter + " files");
        return allOutputFiles.toArray(new File[0]);
    }
    /**
     * Parses a vector drawable XML file into a {@link Document} object.
     *
     * @param xmlFileContent the content of the VectorDrawable's XML file.
     * @param errorLog when errors were found, log them in this builder if it is not null.
     * @return parsed document or null if errors happened.
     */
    @Nullable
    private static Document parseVdStringIntoDocument(
            @NonNull String xmlFileContent, @Nullable StringBuilder errorLog) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(xmlFileContent)));
        } catch (Exception e) {
            if (errorLog != null) {
                errorLog.append("Exception while parsing XML file:\n").append(e.getMessage());
            }
            return null;
        }
    }
}