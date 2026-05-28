package sdnscale.benchmark;

import sdnscale.avl.AVL_Router_Tree;
import sdnscale.core.RouterTree;
import sdnscale.model.PacketRule;
import sdnscale.redblack.RedBlack_Router_Tree;
import sdnscale.util.DataGenerator;
import sdnscale.util.RotationCounter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public final class BenchmarkCSVExporter {

    private static final int[] VOLUMES = {
            10_000, 20_000, 30_000, 40_000, 50_000,
            60_000, 70_000, 80_000, 90_000, 100_000
    };

    private static final int PERCENTUAL_DELECAO = 20;

    private static final String CSV_PATH = "dados/benchmark_results.csv";

    private BenchmarkCSVExporter() {
        throw new UnsupportedOperationException(
                "BenchmarkCSVExporter é uma classe utilitária e não deve ser instanciada.");
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     SDN-Scale — Exportador de Dados para CSV         ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();

        // Cria a pasta dados/ se não existir
        new java.io.File("dados").mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_PATH))) {

            // Cabeçalho do CSV
            writer.println(
                    "volume," +
                            "avl_insercao_ns,rbt_insercao_ns," +
                            "avl_busca_ns,rbt_busca_ns," +
                            "avl_delecao_ns,rbt_delecao_ns," +
                            "avl_rotacoes_insercao,rbt_rotacoes_insercao," +
                            "avl_rotacoes_delecao,rbt_rotacoes_delecao," +
                            "avl_altura,rbt_altura"
            );

            for (int volume : VOLUMES) {
                System.out.printf("Processando volume: %,d regras...%n", volume);

                AVL_Router_Tree avl = new AVL_Router_Tree();
                RedBlack_Router_Tree rbt = new RedBlack_Router_Tree();

                List<PacketRule> dataset = DataGenerator.generateShuffled(volume);
                List<PacketRule> buscas  = DataGenerator.generateShuffled(Math.min(1000, volume));
                List<Integer> delecoes   = DataGenerator.generateDeleteIds(dataset, PERCENTUAL_DELECAO);

                // Inserção
                RotationCounter.resetAll();
                long avlInsercaoNs = medir(() -> {
                    for (PacketRule r : dataset) avl.insert(r);
                });
                int avlRotInsercao = avl.getRotationCount();

                RotationCounter.resetAll();
                long rbtInsercaoNs = medir(() -> {
                    for (PacketRule r : dataset) rbt.insert(r);
                });
                int rbtRotInsercao = rbt.getRotationCount();

                // Busca
                long avlBuscaNs = medir(() -> {
                    for (PacketRule r : buscas) avl.search(r.getRuleId());
                });
                long rbtBuscaNs = medir(() -> {
                    for (PacketRule r : buscas) rbt.search(r.getRuleId());
                });

                // Deleção
                RotationCounter.resetAll();
                long avlDelecaoNs = medir(() -> {
                    for (int id : delecoes) avl.delete(id);
                });
                int avlRotDelecao = avl.getRotationCount();

                RotationCounter.resetAll();
                long rbtDelecaoNs = medir(() -> {
                    for (int id : delecoes) rbt.delete(id);
                });
                int rbtRotDelecao = rbt.getRotationCount();

                // Escreve linha no CSV
                writer.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                        volume,
                        avlInsercaoNs, rbtInsercaoNs,
                        avlBuscaNs,    rbtBuscaNs,
                        avlDelecaoNs,  rbtDelecaoNs,
                        avlRotInsercao, rbtRotInsercao,
                        avlRotDelecao,  rbtRotDelecao,
                        avl.getHeight(), rbt.getHeight()
                );

                System.out.printf(
                        "  AVL → inserção: %,d ns | busca: %,d ns | deleção: %,d ns%n",
                        avlInsercaoNs, avlBuscaNs, avlDelecaoNs);
                System.out.printf(
                        "  RBT → inserção: %,d ns | busca: %,d ns | deleção: %,d ns%n%n",
                        rbtInsercaoNs, rbtBuscaNs, rbtDelecaoNs);
            }

            System.out.println("✅ CSV gerado em: " + CSV_PATH);
            System.out.println("   Rode agora: python graficos/gerar_graficos.py");

        } catch (IOException e) {
            System.err.println("❌ Erro ao escrever o CSV: " + e.getMessage());
        }
    }

    private static long medir(Runnable bloco) {
        long inicio = System.nanoTime();
        bloco.run();
        return System.nanoTime() - inicio;
    }
}