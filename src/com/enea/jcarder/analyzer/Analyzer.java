/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.analyzer;

import static com.enea.jcarder.common.contexts.ContextFileReader.CONTEXTS_DB_FILENAME;
import static com.enea.jcarder.common.contexts.ContextFileReader.EVENT_DB_FILENAME;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextFileReader;
import com.enea.jcarder.common.contexts.ContextReaderIfc;
import com.enea.jcarder.common.events.EventFileReader;
import com.enea.jcarder.util.BuildInformation;
import com.enea.jcarder.util.InvalidOptionException;
import com.enea.jcarder.util.OptionParser;
import com.enea.jcarder.util.logging.AppendableHandler;
import com.enea.jcarder.util.logging.Handler;
import com.enea.jcarder.util.logging.Logger;
import com.enea.jcarder.util.logging.Logger.Level;

/**
 * The main class of the JCarder analyzer.
 */
public final class Analyzer {

    enum OutputMode { INCLUDE_ALL,
                      INCLUDE_CYCLES,
                      INCLUDE_ONLY_MULTI_THREADED_CYCLES };

    /*
     * Cycles with only one thread can never cause a deadlock, but it might be
     * possible that basic tests of a single class are very simplified and use
     * only a single thread where a real program might invoke the methods from
     * several different threads. Therefore single-threaded cycles are also
     * interesting to detect and include by default.
     */
    private OutputMode mOutputMode = OutputMode.INCLUDE_CYCLES;
    private boolean mIncludePackages = false;
    private boolean mPrintDetails = false;
    private Logger mLogger;
    private Level mLogLevel = Logger.Level.INFO;
    private String mInputDirectory = ".";

    public static void main(String[] args) {
        new Analyzer().start(args);
    }

    public void start(String[] args) {
        parseArguments(args);
        initLogger();
        LockGraphBuilder graphBuilder = new LockGraphBuilder();
        final ContextReaderIfc contextReader;

        try {
            contextReader =
                new ContextFileReader(mLogger, new File(mInputDirectory,
                                                        CONTEXTS_DB_FILENAME));

            EventFileReader eventReader = new EventFileReader(mLogger);
            eventReader.parseFile(new File(mInputDirectory, EVENT_DB_FILENAME),
                                  graphBuilder);
        }
        catch (IOException e) {
            mLogger.severe("Error while reading result database: "
                           + e.getMessage());
            return;
        }
        printInitiallyLoadedStatistics(graphBuilder.getAllLocks());

        CycleDetector cycleDetector = new CycleDetector(mLogger);
        cycleDetector.analyzeLockNodes(graphBuilder.getAllLocks());
        printCycleAnalysisStatistics(cycleDetector);

        if (mOutputMode == OutputMode.INCLUDE_ALL) {
            printDetailsIfEnabled(cycleDetector.getCycles(), contextReader);
            try {
                generatGraphvizFileForAllNodes(graphBuilder, contextReader);
            } catch (IOException e) {
                mLogger.severe("Error while generating Graphviz file: "
                               + e.getMessage());
            }
        } else {
            if (mOutputMode == OutputMode.INCLUDE_ONLY_MULTI_THREADED_CYCLES) {
                cycleDetector.removeSingleThreadedCycles();
            }
            if (cycleDetector.getCycles().isEmpty()) {
                System.out.println("No cycles found!");
                return;
            }
            graphBuilder.clear(); // Help GC.
            /*
             * TODO Also clear all references in LockNode.mOutgoingEdges to
             * avoid keeping references to a lot of LockEdge and LockNode
             * objects in order to release as much memory as possible for the
             * memory mapped file?
             *
             * It is not necessary to use the DuplicateEdgeshandler since those
             * duplicates are removed anyway when cycles that are alike are
             * removed.
             */
            cycleDetector.removeAlikeCycles(contextReader);

            printDetailsIfEnabled(cycleDetector.getCycles(), contextReader);
            try {
                generateGraphvizFilesForCycles(contextReader, cycleDetector);
            } catch (IOException e) {
                mLogger.severe("Error while generating Graphviz file: "
                               + e.getMessage());
            }
        }
    }

    private void initLogger() {
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new AppendableHandler(System.out,
                                           Logger.Level.CONFIG,
                                           "{message}\n"));
        mLogger = new Logger(handlers, mLogLevel);
    }

    private void generateGraphvizFilesForCycles(ContextReaderIfc reader,
                                                CycleDetector cycleDetector)
    throws IOException {
        System.out.println();
        int index = 0;
        Collection<HashSet<LockEdge>> cycles =
            cycleDetector.mergeCyclesWithIdenticalLocks();
        for (HashSet<LockEdge> edges : cycles) {
            if (index >= 100) {
                System.out.println("Aborting. Too many cycles!");
                break;
            }
            GraphvizGenerator graphvizGenerator = new GraphvizGenerator();
            createGraphvizFile(graphvizGenerator.generate(edges,
                                                          reader,
                                                          mIncludePackages),
                                                          index++);
        }
    }

    private void  printCycleAnalysisStatistics(CycleDetector cycleDetector) {
        System.out.println("\nCycle analysis result: ");
        System.out.println("   Cycles:          "
                           + cycleDetector.getCycles().size());
        System.out.println("   Edges in cycles: "
                           + cycleDetector.getNumberOfEdges());
        System.out.println("   Nodes in cycles: "
                           + cycleDetector.getNumberOfNodes());
        System.out.println("   Max cycle depth: "
                           + cycleDetector.getMaxCycleDepth());
        System.out.println("   Max graph depth: "
                           + cycleDetector.getMaxDepth());
        System.out.println();
    }

    private void generatGraphvizFileForAllNodes(LockGraphBuilder graphBuilder,
                                                ContextReaderIfc reader)
    throws IOException {
        DuplicatedEdgesHandler.mergeDuplicatedEdges(graphBuilder.getAllLocks(),
                                                    reader);
        // TODO Print statistics about removed duplicates?
        LinkedList<LockEdge> allEdges = new LinkedList<LockEdge>();
        for (LockNode node : graphBuilder.getAllLocks()) {
            allEdges.addAll(node.getOutgoingEdges());
        }
        GraphvizGenerator graphvizGenerator = new GraphvizGenerator();
        createGraphvizFile(graphvizGenerator.generate(allEdges,
                                                      reader,
                                                      mIncludePackages),
                                                      0);
    }

    private void parseArguments(String[] args) {
        OptionParser op = new OptionParser();
        configureOptionParser(op);

        try {
            op.parse(args);
        } catch (InvalidOptionException e) {
            handleBadOption(op, e.getMessage());
        }

        handleOptions(op);
    }

    private void configureOptionParser(OptionParser op) {
        /*
         * TODO Add parameters for filtering (including & excluding) specific
         * locks and edges for example by specifying thread names, object
         * classes, method names or packages?
         */

        op.addOption("-help",
                     "Print this help text");
        op.addOption("-d <directory>",
                     "Read results to analyze from <directory> (default:"
                     + " current directory)");
        op.addOption("-includepackages",
                     "Include packages (not only class names) in graph");
        op.addOption("-loglevel <level>",
                     "Set log level to <level> (one of "
                     + Logger.Level.getEnumeration()
                     + ")");
        op.addOption("-outputmode <mode>",
                     "Set output mode to <mode> (one of ALL, CYCLES, MTCYCLES);"
                     + " ALL: include everything;"
                     + " CYCLES: only include cycles (this is the default);"
                     + " MTCYCLES: only include multi-thread cycles");
        op.addOption("-printdetails",
                     "Print details");
        op.addOption("-version",
                     "Print program version");
    }

    private void handleOptions(OptionParser op) {
        Map<String, String> options = op.getOptions();
        for (String option : options.keySet()) {
            if (option.equals("-help")) {
                printHelpText(System.out, op);
                System.exit(0);
            } else if (option.equals("-i")) {
                mInputDirectory = options.get(option);
            } else if (option.equals("-includepackages")) {
                mIncludePackages = true;
            } else if (option.equals("-loglevel")) {
                mLogLevel = Logger.Level.fromString(options.get(option));
                if (mLogLevel == null) {
                    handleBadOption(op, "bad log level");
                }
            } else if (option.equals("-outputmode")) {
                String value = options.get(option);
                if (value.equalsIgnoreCase("all")) {
                    mOutputMode = OutputMode.INCLUDE_ALL;
                } else if (value.equalsIgnoreCase("cycles")) {
                    mOutputMode = OutputMode.INCLUDE_CYCLES;
                } else if (value.equalsIgnoreCase("mtcycles")) {
                    mOutputMode = OutputMode.INCLUDE_ONLY_MULTI_THREADED_CYCLES;
                } else {
                    handleBadOption(op, "bad output mode");
                }
            } else if (option.equals("-printdetails")) {
                mPrintDetails = true;
            } else if (option.equals("-version")) {
                BuildInformation.printLongBuildInformation();
                System.exit(0);
            }
        }
    }

    private void printHelpText(PrintStream stream, OptionParser op) {
        stream.print("Usage: java -jar jcarder.jar [options]\n\n");
        stream.print("Options:\n");
        stream.print(op.getOptionHelp());
    }

    private void handleBadOption(OptionParser optionParser, String message) {
        System.err.println("JCarder: " + message);
        printHelpText(System.err, optionParser);
        System.exit(1);
    }

    private void printDetailsIfEnabled(Iterable<Cycle> cycles,
                                       ContextReaderIfc reader) {
        if (!mPrintDetails) {
            return;
        }
        SortedSet<String> threads = new TreeSet<String>();
        SortedSet<String> methods = new TreeSet<String>();
        for (Cycle cycle : cycles) {
            for (LockEdge edge : cycle.getEdges()) {
                LockingContext source =
                    reader.readContext(edge.getSourceLockingContextId());
                LockingContext target =
                    reader.readContext(edge.getTargetLockingContextId());
                threads.add(source.getThreadName());
                threads.add(target.getThreadName());
                methods.add(source.getMethodWithClass());
                methods.add(target.getMethodWithClass());
            }
        }
        System.out.println();
        System.out.println("Threads involved in cycles:");
        for (String thread : threads) {
            System.out.println("   " + thread);
        }
        System.out.println();
        System.out.println("Methods involved in cycles:");
        for (String method : methods) {
            System.out.println("   " + method);
        }
        System.out.println();
    }


    private void printInitiallyLoadedStatistics(Iterable<LockNode> locks) {
        int numberOfNodes = 0;
        int numberOfUniqueEdges = 0;
        int numberOfDuplicatedEdges = 0;
        for (LockNode lock : locks) {
            numberOfNodes++;
            numberOfUniqueEdges += lock.numberOfUniqueEdges();
            numberOfDuplicatedEdges += lock.numberOfDuplicatedEdges();
        }
        System.out.println("\nLoaded from database files:");
        System.out.println("   Nodes: " + numberOfNodes);
        System.out.println("   Edges: " + numberOfUniqueEdges
                           + " (excluding " + numberOfDuplicatedEdges
                           + " duplicated)");
    }

    private void createGraphvizFile(String s, int index) throws IOException {
        File file = new File("jcarder_result_" + index + ".gv");
        System.out.println("Writing Graphviz file: " + file.getAbsolutePath());
        FileWriter fw = new FileWriter(file);
        fw.write(s);
        fw.flush();
        fw.close();
    }
}
