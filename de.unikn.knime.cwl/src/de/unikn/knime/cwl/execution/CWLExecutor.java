/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   1 Aug 2019 : created
 */
package de.unikn.knime.cwl.execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.commons.io.FileUtils;
import org.knime.core.node.NodeLogger;

import de.unikn.knime.cwl.preferences.Preferences;

/**
 * Class for executing a CWL file.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public final class CWLExecutor {
    
    private CWLExecutor() { }
    
    private static final NodeLogger LOGGER = NodeLogger.getLogger(CWLExecutor.class);
    
    /**
     * Executes the CWL wrapper using the cwl-runner configured in the preferences.
     * @param toolPath the path to the CWL tool
     * @param inputs the input data
     * @param extraCWLRunnerArgs extra arguments for the CWL runner
     * @return the output data
     * @throws IOException when data cannot be written or the tool cannot be executed
     * @throws InterruptedException when an interrupt signal is sent while waiting for the tool
     */
    public static CWLExecutionResult execute(final String toolPath,
            final Map<String, JsonValue> inputs, final String extraCWLRunnerArgs)
            throws IOException, InterruptedException {
        Path baseDir = Paths.get(Files.createTempDirectory("knime_").toUri());
        
        // Build YAML file content
        StringBuilder yamlInputs = new StringBuilder();
        for (Entry<String, JsonValue> e : inputs.entrySet()) {
            yamlInputs.append(e.getKey()).append(": ");
            yamlInputs.append(e.getValue().toString()).append("\n");
        }
        
        // Write input for tool into YAML file
        File yamlFile = baseDir.resolve("input.yml").toFile();
        FileUtils.writeStringToFile(yamlFile, yamlInputs.toString(), Charset.defaultCharset());
        
        // Retrieve the path to the runner from the preferences.
        String runnerPath = Preferences.getCWLRunnerPath();
        
        String globExtraArgs = Preferences.getExtraArgs();
        String[] parsedGlobExtraArgs = translateCommandline(globExtraArgs);
        
        String[] parsedExtraArgs = extraCWLRunnerArgs == null ? new String[0]
                                    : translateCommandline(extraCWLRunnerArgs);
        
        String[] args = new String[parsedGlobExtraArgs.length + parsedExtraArgs.length + 4];
        // cwl-runner in quiet mode
        args[0] = runnerPath;
        args[1] = "--quiet";
        // Insert extra arguments
        System.arraycopy(parsedGlobExtraArgs, 0, args, 2, parsedGlobExtraArgs.length);
        System.arraycopy(parsedExtraArgs, 0, args, parsedGlobExtraArgs.length + 2, parsedExtraArgs.length);
        // Lastly the tool and its inputs
        args[args.length - 2] = toolPath;
        args[args.length - 1] = yamlFile.getAbsolutePath();
        
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.directory(baseDir.toFile());
        LOGGER.info("Executing CWL " + toolPath);
        LOGGER.info(String.join(" ", pb.command()));
        Process pr = pb.start();
        InputStream is = pr.getInputStream();
        StringBuilder jsonOutput = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        boolean outputStarted = false;
        String line = null;
        while ((line = reader.readLine()) != null) {
            LOGGER.debug(line);
            if (line.startsWith("{")) {
                outputStarted = true;
            }
            if (outputStarted) {
                jsonOutput.append(line);
            }
            if (line.startsWith("}")) {
                break;
            }
        }
        int exitCode = pr.waitFor();
        if (exitCode != 0) {
            LOGGER.warn("Tool execution returned exit code " + exitCode);
            LOGGER.warn(jsonOutput);
            // Not all tools have proper exits codes
            // eg diff has 0 if the files are the same and 1 otherwise
            if (!outputStarted) {
                throw new IOException("CWL execution produced no valid output");
            }
        }
        
        // Parse output
        Map<String, JsonValue> outputObject = new HashMap<>();
        JsonObject outputJson = Json.createReader(new StringReader(jsonOutput.toString())).readObject();
        for (String key : outputJson.keySet()) {
            outputObject.put(key, outputJson.get(key));
        }
        return new CWLExecutionResult(outputJson, exitCode);
    }
    
    /**
     * [code borrowed from ant.jar]
     * Crack a command line.
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    private static String[] translateCommandline(final String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<String>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        result.add(current.toString());
                        current.setLength(0);
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new RuntimeException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Result of an execution of the CWL runner.
     * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
     *
     */
    public static class CWLExecutionResult {
        private JsonObject m_outputJson;
        private int m_exitCode;
        
        /**
         * Creates a new instance of {@code CWLExecutionResult}.
         * @param outputJson the output JSON read from the CWL runner's output
         * @param statusCode the status code returned by the CWL runner
         */
        public CWLExecutionResult(final JsonObject outputJson, final int statusCode) {
            super();
            m_outputJson = outputJson;
            m_exitCode = statusCode;
        }

        /**
         * @return the JSON result from the CWL runner.
         */
        public JsonObject getOutputJson() {
            return m_outputJson;
        }

        /**
         * @return the CWL runner's exit code
         */
        public int getExitCode() {
            return m_exitCode;
        }
    }
}
