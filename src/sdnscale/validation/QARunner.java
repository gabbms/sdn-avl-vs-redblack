package sdnscale.validation;

import sdnscale.avl.AVL_Router_Tree;
import sdnscale.redblack.RedBlack_Router_Tree;
import sdnscale.model.PacketRule;


public class QARunner {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         SDN-Scale — Validação Local de QA            ║");
        System.out.println("║         Integrante 3 – InvariantChecker              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();

        AVL_Router_Tree avl = new AVL_Router_Tree();
        RedBlack_Router_Tree rbt = new RedBlack_Router_Tree();

        // =====================================================================
        // Fase 1 — Inserção de 1.000 regras (dataset reduzido para validação)
        // =====================================================================
        System.out.println("── Fase 1: Inserindo 1.000 regras ──");
        for (int i = 1; i <= 1000; i++) {
            avl.insert(new PacketRule(i, i % 100 + 1));
            rbt.insert(new PacketRule(i, i % 100 + 1));
        }
        System.out.println("   AVL: " + avl.size() + " nós | Altura: " + avl.getHeight());
        System.out.println("   RBT: " + rbt.size() + " nós | Altura: " + rbt.getHeight());
        System.out.println();

        // =====================================================================
        // Fase 2 — Validação pós-inserção
        // =====================================================================
        System.out.println("── Fase 2: Validação pós-inserção ──");
        InvariantChecker.checkAVL(avl);
        InvariantChecker.checkRedBlack(rbt);

        // =====================================================================
        // Fase 3 — Deleção de 20% (200 nós)
        // =====================================================================
        System.out.println("── Fase 3: Deletando 20% dos nós (200 regras) ──");
        for (int i = 1; i <= 200; i++) {
            avl.delete(i);
            rbt.delete(i);
        }
        System.out.println("   AVL: " + avl.size() + " nós restantes | Altura: " + avl.getHeight());
        System.out.println("   RBT: " + rbt.size() + " nós restantes | Altura: " + rbt.getHeight());
        System.out.println();

        // =====================================================================
        // Fase 4 — Validação pós-deleção
        // =====================================================================
        System.out.println("── Fase 4: Validação pós-deleção ──");
        InvariantChecker.checkAVL(avl);
        InvariantChecker.checkRedBlack(rbt);

        // =====================================================================
        // Relatório comparativo final
        // =====================================================================
        InvariantChecker.printComparativeReport(avl, rbt);

        System.out.println("Validação local concluída. Estruturas aprovadas para benchmark completo.");
    }
}