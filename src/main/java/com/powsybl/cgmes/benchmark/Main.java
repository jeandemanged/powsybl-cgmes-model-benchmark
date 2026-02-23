package com.powsybl.cgmes.benchmark;

import com.google.common.base.Stopwatch;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.datasource.ZipArchiveDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    record Stats(int i, double loadTime, double qLines, double qGens, double qLoads) {

        public static void logSingleRunTime(Stats stats, Collection<?> lines, Collection<?> gens, Collection<?> loads) {
            LOGGER.info("[run {}/{}] import: {} ms", stats.i, NUM_RUNS, stats.loadTime);
            LOGGER.info("[run {}/{}]   {} lines queried in {} ms", stats.i, NUM_RUNS, lines.size(), stats.qLines);
            LOGGER.info("[run {}/{}]   {} gens  queried in {} ms", stats.i, NUM_RUNS, gens.size(), stats.qGens);
            LOGGER.info("[run {}/{}]   {} loads queried in {} ms", stats.i, NUM_RUNS, loads.size(), stats.qLoads);
        }

        public static void logAverageTime(String name, List<Stats> statsList) {
            LOGGER.info("{} Averages:", name);
            LOGGER.info("    Average load time: {} ms", statsList.stream().mapToDouble(Stats::loadTime).average().orElseThrow());
            LOGGER.info("    Average lines query time: {} ms", statsList.stream().mapToDouble(Stats::qLines).average().orElseThrow());
            LOGGER.info("    Average gens  query time: {} ms", statsList.stream().mapToDouble(Stats::qGens).average().orElseThrow());
            LOGGER.info("    Average loads query time: {} ms", statsList.stream().mapToDouble(Stats::qLoads).average().orElseThrow());
        }

    }

    public static final int NUM_WARMUP_RUNS = 5;
    public static final int NUM_RUNS = 10;
    public static final Path REAL_GRID_PATH = Paths.get("./CGMES_v2.4.15_RealGridTestConfiguration_v2.zip");

    public static void main(String[] args) {

        jvmWarmup();

        cgmesImporterBenchmark();

        cgmesModelBenchmark();

    }

    private static void cgmesModelBenchmark() {
        List<Stats> statsListCgmesModel = new ArrayList<>();
        for (int i = 1; i <= NUM_RUNS; i++) {
            Stopwatch sw = Stopwatch.createStarted();
            CgmesModel model = CgmesModelFactory.create(new ZipArchiveDataSource(REAL_GRID_PATH));
            double loadTime = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            PropertyBags acLineSegments = model.acLineSegments();
            double qLines = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            PropertyBags synchronousMachinesAll = model.synchronousMachinesAll();
            double qGens = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            PropertyBags energyConsumers = model.energyConsumers();
            double qLoads = sw.elapsed(TimeUnit.MILLISECONDS);
            Stats stats = new Stats(i, loadTime, qLines, qGens, qLoads);
            Stats.logSingleRunTime(stats, acLineSegments, synchronousMachinesAll, energyConsumers);
            statsListCgmesModel.add(stats);
        }
        Stats.logAverageTime("PowSyBl CGMES Model", statsListCgmesModel);
    }

    private static void cgmesImporterBenchmark() {
        List<Stats> statsListCgmesImporter = new ArrayList<>();
        for (int i = 1; i <= NUM_RUNS; i++) {
            Stopwatch sw = Stopwatch.createStarted();
            Network net = Network.read(REAL_GRID_PATH);
            double loadTime = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            var acLineSegments = net.getLineStream().toList();
            double qLines = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            var synchronousMachinesAll = net.getGeneratorStream().toList();
            double qGens = sw.elapsed(TimeUnit.MILLISECONDS);
            sw.reset().start();
            var energyConsumers = net.getLoadStream().toList();
            double qLoads = sw.elapsed(TimeUnit.MILLISECONDS);
            Stats stats = new Stats(i, loadTime, qLines, qGens, qLoads);
            Stats.logSingleRunTime(stats, acLineSegments, synchronousMachinesAll, energyConsumers);
            statsListCgmesImporter.add(stats);
        }
        Stats.logAverageTime("PowSyBl CGMES Importer", statsListCgmesImporter);
    }

    private static void jvmWarmup() {
        // a few JVM warmup runs
        for (int i = 1; i <= NUM_WARMUP_RUNS; i++) {
            LOGGER.info("warmup {}/{} ...",  i, NUM_WARMUP_RUNS);
            Network.read(REAL_GRID_PATH);
        }
    }
}
