package sdnscale.benchmark;

import sdnscale.avl.AVL_Router_Tree;
import sdnscale.core.RouterTree;
import sdnscale.model.PacketRule;
import sdnscale.redblack.RedBlack_Router_Tree;
import sdnscale.util.DataGenerator;
import sdnscale.util.RotationCounter;
import sdnscale.validation.InvariantChecker;

import java.util.List;

public final class BenchmarkRunner {

    private static final int TOTAL_NOS = 100_000;

    private static final int TOTAL_BUSCAS = 10_000;

    private static final int PERCENTUAL_DELECAO = 20;

    private BenchmarkRunner() {
        throw new UnsupportedOperationException(
                "BenchmarkRunner é uma classe utilitária e não deve ser instanciada.");
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         SDN-Scale — Benchmark Comparativo            ║");
        System.out.println("║  AVL Router Tree  vs  Red-Black Router Tree          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("%nSeed fixa: %d | Nós: %,d | Buscas: %,d | Deleção: %d%%%n%n",
                DataGenerator.DEFAULT_SEED, TOTAL_NOS, TOTAL_BUSCAS, PERCENTUAL_DELECAO);

        AVL_Router_Tree avl = new AVL_Router_Tree();
        RedBlack_Router_Tree rbt = new RedBlack_Router_Tree();

        List<PacketRule> dataset = DataGenerator.generateShuffled(TOTAL_NOS);
        List<Integer> idsParaDeletar = DataGenerator.generateDeleteIds(dataset, PERCENTUAL_DELECAO);
        List<PacketRule> datasetsParaBusca = DataGenerator.generateShuffled(TOTAL_BUSCAS);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("FASE 1 — Inserção de %,d regras%n", TOTAL_NOS);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        RotationCounter.resetAll();
        long tempoAvlInsercao = medirInsercao(avl, dataset);
        int rotacoesAvlInsercao = avl.getRotationCount();

        RotationCounter.resetAll();
        long tempoRbtInsercao = medirInsercao(rbt, dataset);
        int rotacoesRbtInsercao = rbt.getRotationCount();

        imprimirResultadoFase("AVL", tempoAvlInsercao, rotacoesAvlInsercao, avl.getHeight(), avl.size());
        imprimirResultadoFase("RBT", tempoRbtInsercao, rotacoesRbtInsercao, rbt.getHeight(), rbt.size());

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("FASE 2 — Busca de %,d regras%n", TOTAL_BUSCAS);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long tempoAvlBusca = medirBusca(avl, datasetsParaBusca);
        long tempoRbtBusca = medirBusca(rbt, datasetsParaBusca);

        imprimirResultadoBusca("AVL", tempoAvlBusca, TOTAL_BUSCAS);
        imprimirResultadoBusca("RBT", tempoRbtBusca, TOTAL_BUSCAS);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("FASE 3 — Deleção de %d%% dos nós (%,d regras)%n",
                PERCENTUAL_DELECAO, idsParaDeletar.size());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        RotationCounter.resetAll();
        long tempoAvlDelecao = medirDelecao(avl, idsParaDeletar);
        int rotacoesAvlDelecao = avl.getRotationCount();

        RotationCounter.resetAll();
        long tempoRbtDelecao = medirDelecao(rbt, idsParaDeletar);
        int rotacoesRbtDelecao = rbt.getRotationCount();

        imprimirResultadoFase("AVL", tempoAvlDelecao, rotacoesAvlDelecao, avl.getHeight(), avl.size());
        imprimirResultadoFase("RBT", tempoRbtDelecao, rotacoesRbtDelecao, rbt.getHeight(), rbt.size());

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("FASE 4 — Validação de Invariantes Pós-Deleção");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        InvariantChecker.checkAVL(avl);
        InvariantChecker.checkRedBlack(rbt);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("RELATÓRIO FINAL");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("%-30s %-15s %-15s%n", "Métrica", "AVL", "Red-Black");
        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-30s %-15s %-15s%n",
                "Tempo inserção (ms)",
                formatarMs(tempoAvlInsercao),
                formatarMs(tempoRbtInsercao));
        System.out.printf("%-30s %-15s %-15s%n",
                "Rotações na inserção",
                String.format("%,d", rotacoesAvlInsercao),
                String.format("%,d", rotacoesRbtInsercao));
        System.out.printf("%-30s %-15s %-15s%n",
                "Tempo busca (ms)",
                formatarMs(tempoAvlBusca),
                formatarMs(tempoRbtBusca));
        System.out.printf("%-30s %-15s %-15s%n",
                "Tempo deleção 20%% (ms)",
                formatarMs(tempoAvlDelecao),
                formatarMs(tempoRbtDelecao));
        System.out.printf("%-30s %-15s %-15s%n",
                "Rotações na deleção",
                String.format("%,d", rotacoesAvlDelecao),
                String.format("%,d", rotacoesRbtDelecao));
        System.out.printf("%-30s %-15d %-15d%n",
                "Altura final",
                avl.getHeight(),
                rbt.getHeight());
        System.out.printf("%-30s %-15d %-15d%n",
                "Nós restantes",
                avl.size(),
                rbt.size());
        System.out.println("--------------------------------------------------------------");
        System.out.println("Benchmark concluído. Resultados prontos para o Post-Mortem.");
    }

    private static long medirInsercao(RouterTree tree, List<PacketRule> dataset) {
        long inicio = System.nanoTime();
        for (PacketRule rule : dataset) {
            tree.insert(rule);
        }
        return System.nanoTime() - inicio;
    }

    private static long medirBusca(RouterTree tree, List<PacketRule> dataset) {
        long inicio = System.nanoTime();
        for (PacketRule rule : dataset) {
            tree.search(rule.getRuleId());
        }
        return System.nanoTime() - inicio;
    }

    private static long medirDelecao(RouterTree tree, List<Integer> idsParaDeletar) {
        long inicio = System.nanoTime();
        for (int id : idsParaDeletar) {
            tree.delete(id);
        }
        return System.nanoTime() - inicio;
    }

    private static void imprimirResultadoFase(String nome, long tempoNs,
                                              int rotacoes, int altura, int nos) {
        System.out.printf("  [%s] Tempo: %s ms | Rotações: %,d | Altura: %d | Nós: %,d%n",
                nome, formatarMs(tempoNs), rotacoes, altura, nos);
    }

    private static void imprimirResultadoBusca(String nome, long tempoNs, int total) {
        double mediaNs = (double) tempoNs / total;
        System.out.printf("  [%s] Tempo total: %s ms | Média por busca: %.2f ns%n",
                nome, formatarMs(tempoNs), mediaNs);
    }

    private static String formatarMs(long nanos) {
        return String.format("%.2f", nanos / 1_000_000.0);
    }
}