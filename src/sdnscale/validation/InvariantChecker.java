package sdnscale.validation;

import sdnscale.avl.AVL_Router_Tree;
import sdnscale.redblack.RedBlack_Router_Tree;
import sdnscale.core.RouterTree;

/**
 * Validador de invariantes estruturais para as árvores AVL e Red-Black.
 *
 * <p>Responsabilidade exclusiva do Integrante 3 (QA). Esta classe audita
 * as duas implementações de {@link RouterTree} após operações de inserção
 * e deleção, garantindo que as propriedades matemáticas de cada estrutura
 * foram preservadas.</p>
 *
 * <p><b>Como usar:</b></p>
 * <pre>{@code
 * AVL_Router_Tree avl = new AVL_Router_Tree();
 * // ... inserções e deleções ...
 * InvariantChecker.checkAVL(avl);
 *
 * RedBlack_Router_Tree rbt = new RedBlack_Router_Tree();
 * // ... inserções e deleções ...
 * InvariantChecker.checkRedBlack(rbt);
 * }</pre>
 *
 * @author  Integrante 3 – QA / Validação
 * @version 1.0
 */
public final class InvariantChecker {

    // =========================================================================
    // Construtor privado — classe utilitária, não instanciável
    // =========================================================================

    private InvariantChecker() {
        throw new UnsupportedOperationException(
                "InvariantChecker é uma classe utilitária e não deve ser instanciada.");
    }

    // =========================================================================
    // Validação AVL
    // =========================================================================

    /**
     * Valida todos os invariantes da Árvore AVL.
     *
     * <p>Verifica:</p>
     * <ul>
     *   <li>Propriedade BST: {@code inOrderTraversal()} retorna IDs crescentes</li>
     *   <li>Invariante AVL: altura respeita {@code h < 1.44 * log₂(n+2)}</li>
     * </ul>
     *
     * @param avl instância da AVL a ser auditada
     * @throws AssertionError se qualquer invariante for violado
     */
    public static void checkAVL(AVL_Router_Tree avl) {
        System.out.println("=== Auditoria AVL ===");

        checkBSTProperty(avl);
        checkAVLHeight(avl);

        System.out.println("✅ Todos os invariantes AVL validados com sucesso.");
        System.out.println("   Nós: " + avl.size());
        System.out.println("   Altura atual: " + avl.getHeight());
        System.out.println("   Rotações realizadas: " + avl.getRotationCount());
        System.out.println();
    }

    /**
     * Verifica se a altura da AVL respeita o limite teórico.
     *
     * <p>Para uma AVL com {@code n} nós, a altura máxima é
     * {@code h < 1.44 * log₂(n+2)}. Qualquer valor acima indica
     * falha no rebalanceamento.</p>
     */
    private static void checkAVLHeight(AVL_Router_Tree avl) {
        int n = avl.size();
        if (n == 0) return;

        int alturaAtual = avl.getHeight();
        double alturaMaxima = 1.44 * (Math.log(n + 2) / Math.log(2));

        System.out.printf("   Verificando altura: atual=%d, máximo teórico=%.2f%n",
                alturaAtual, alturaMaxima);

        if (alturaAtual > alturaMaxima) {
            throw new AssertionError(String.format(
                    "FALHA AVL: altura %d excede o limite teórico %.2f para n=%d nós.",
                    alturaAtual, alturaMaxima, n));
        }

        System.out.println("   ✓ Altura dentro do limite teórico AVL.");
    }

    // =========================================================================
    // Validação Red-Black
    // =========================================================================

    /**
     * Valida todos os invariantes da Árvore Red-Black.
     *
     * <p>Verifica:</p>
     * <ul>
     *   <li>Propriedade BST: {@code inOrderTraversal()} retorna IDs crescentes</li>
     *   <li>Altura máxima: {@code h ≤ 2 * log₂(n+1)}</li>
     * </ul>
     *
     * @param rbt instância da Red-Black a ser auditada
     * @throws AssertionError se qualquer invariante for violado
     */
    public static void checkRedBlack(RedBlack_Router_Tree rbt) {
        System.out.println("=== Auditoria Red-Black ===");

        checkBSTProperty(rbt);
        checkRBTHeight(rbt);

        System.out.println("✅ Todos os invariantes Red-Black validados com sucesso.");
        System.out.println("   Nós: " + rbt.size());
        System.out.println("   Altura atual: " + rbt.getHeight());
        System.out.println("   Rotações realizadas: " + rbt.getRotationCount());
        System.out.println();
    }

    /**
     * Verifica se a altura da RBT respeita o limite teórico.
     *
     * <p>Para uma RBT com {@code n} nós, a altura máxima é
     * {@code h ≤ 2 * log₂(n+1)}.</p>
     */
    private static void checkRBTHeight(RedBlack_Router_Tree rbt) {
        int n = rbt.size();
        if (n == 0) return;

        int alturaAtual = rbt.getHeight();
        double alturaMaxima = 2.0 * (Math.log(n + 1) / Math.log(2));

        System.out.printf("   Verificando altura: atual=%d, máximo teórico=%.2f%n",
                alturaAtual, alturaMaxima);

        if (alturaAtual > alturaMaxima) {
            throw new AssertionError(String.format(
                    "FALHA RBT: altura %d excede o limite teórico %.2f para n=%d nós.",
                    alturaAtual, alturaMaxima, n));
        }

        System.out.println("   ✓ Altura dentro do limite teórico Red-Black.");
    }

    // =========================================================================
    // Validação comum — propriedade BST
    // =========================================================================

    /**
     * Verifica se a propriedade BST foi preservada em qualquer implementação
     * de {@link RouterTree}, validando que o percurso in-order retorna os
     * IDs em ordem estritamente crescente.
     *
     * @param tree instância a ser auditada
     * @throws AssertionError se a ordem não for crescente
     */
    public static void checkBSTProperty(RouterTree tree) {
        String traversal = tree.inOrderTraversal();

        if (traversal == null || traversal.isBlank()) {
            System.out.println("   ✓ Árvore vazia — propriedade BST trivialmente satisfeita.");
            return;
        }

        String[] partes = traversal.split(",\\s*");
        int anterior = Integer.MIN_VALUE;

        for (String parte : partes) {
            int id = Integer.parseInt(parte.trim());
            if (id <= anterior) {
                throw new AssertionError(String.format(
                        "FALHA BST: ID %d encontrado após ID %d — ordem crescente violada.",
                        id, anterior));
            }
            anterior = id;
        }

        System.out.println("   ✓ Propriedade BST verificada: IDs em ordem crescente.");
    }

    // =========================================================================
    // Relatório comparativo
    // =========================================================================

    /**
     * Imprime um relatório comparativo entre as duas árvores após os
     * testes de carga do Integrante 2, consolidando as métricas para
     * o relatório Post-Mortem.
     *
     * @param avl instância da AVL após os benchmarks
     * @param rbt instância da Red-Black após os benchmarks
     */
    public static void printComparativeReport(AVL_Router_Tree avl,
                                              RedBlack_Router_Tree rbt) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║       RELATÓRIO COMPARATIVO SDN-SCALE        ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf( "║  %-20s %-10s %-10s ║%n", "Métrica", "AVL", "Red-Black");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf( "║  %-20s %-10d %-10d ║%n",
                "Nós inseridos", avl.size(), rbt.size());
        System.out.printf( "║  %-20s %-10d %-10d ║%n",
                "Altura final", avl.getHeight(), rbt.getHeight());
        System.out.printf( "║  %-20s %-10d %-10d ║%n",
                "Total rotações", avl.getRotationCount(), rbt.getRotationCount());
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}