package sdnscale.validation;

import sdnscale.avl.AVL_Router_Tree;
import sdnscale.redblack.RedBlack_Router_Tree;
import sdnscale.core.RouterTree;

public final class InvariantChecker {



    private InvariantChecker() {
        throw new UnsupportedOperationException(
                "InvariantChecker é uma classe utilitária e não deve ser instanciada.");
    }


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