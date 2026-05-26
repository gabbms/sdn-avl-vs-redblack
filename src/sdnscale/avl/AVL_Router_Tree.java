package sdnscale.avl;

import sdnscale.core.RouterTree;
import sdnscale.model.PacketRule;
import sdnscale.util.RotationCounter;

/**
 * Implementação da Árvore AVL para o motor de busca do projeto SDN-Scale.
 *
 * <p>Gerencia objetos {@link PacketRule} mantendo a invariante AVL em todas
 * as operações: para todo nó {@code n}, o Fator de Balanceamento
 * {@code FB(n) = altura(esquerdo) - altura(direito)} satisfaz {@code |FB| ≤ 1}.</p>
 *
 * <p><b>Rotações implementadas:</b></p>
 * <ul>
 *   <li>Rotação simples à direita (caso Left-Left)</li>
 *   <li>Rotação simples à esquerda (caso Right-Right)</li>
 *   <li>Rotação dupla esquerda-direita (caso Left-Right)</li>
 *   <li>Rotação dupla direita-esquerda (caso Right-Left)</li>
 * </ul>
 *
 * <p><b>Deleção:</b> Implementada via sucessor in-order (menor nó da
 * subárvore direita), com propagação de rebalanceamento até a raiz.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     RouterTree
 */
public class AVL_Router_Tree implements RouterTree {

    // =========================================================================
    // Estado interno
    // =========================================================================

    /** Raiz da árvore. {@code null} se a árvore estiver vazia. */
    private AVLNode root;

    /** Número de regras armazenadas atualmente. */
    private int size;

    /** Contador de rotações compartilhado com os benchmarks do Integrante 2. */
    private final RotationCounter rotationCounter;

    // =========================================================================
    // Construtor
    // =========================================================================

    /**
     * Cria uma AVL_Router_Tree vazia e inicializa o contador de rotações
     * isolado para esta estrutura.
     */
    public AVL_Router_Tree() {
        this.root            = null;
        this.size            = 0;
        this.rotationCounter = RotationCounter.getInstance("AVL");
    }

    // =========================================================================
    // Operações de escrita — insert
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Após a inserção, o caminho da raiz até o nó inserido é percorrido
     * de volta atualizando alturas e aplicando rotações onde {@code |FB| > 1}.</p>
     */
    @Override
    public void insert(PacketRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("A regra não pode ser null.");
        }
        root = insert(root, rule);
        size++;
    }

    private AVLNode insert(AVLNode node, PacketRule rule) {
        // Caso base: posição encontrada, cria folha
        if (node == null) {
            return new AVLNode(rule);
        }

        int cmp = rule.compareTo(node.rule);

        if (cmp < 0) {
            node.left = insert(node.left, rule);
        } else if (cmp > 0) {
            node.right = insert(node.right, rule);
        } else {
            throw new IllegalStateException(
                    "Já existe uma regra com o ID " + rule.getRuleId() + " na árvore.");
        }

        // Atualiza altura e rebalanceia se necessário
        updateHeight(node);
        return rebalance(node);
    }

    // =========================================================================
    // Operações de escrita — delete
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Desafio da Deleção:</b> Após remover o nó, o rebalanceamento
     * é propagado até a raiz via recursão, corrigindo todo nó cujo
     * {@code |FB| > 1} no caminho de volta.</p>
     */
    @Override
    public boolean delete(int ruleId) {
        if (!contains(root, ruleId)) {
            return false;
        }
        root = delete(root, ruleId);
        size--;
        return true;
    }

    private AVLNode delete(AVLNode node, int ruleId) {
        if (node == null) {
            return null;
        }

        if (ruleId < node.rule.getRuleId()) {
            node.left = delete(node.left, ruleId);

        } else if (ruleId > node.rule.getRuleId()) {
            node.right = delete(node.right, ruleId);

        } else {
            // Nó encontrado — três casos clássicos:

            // Caso 1: nó folha
            if (node.left == null && node.right == null) {
                return null;
            }

            // Caso 2: nó com apenas um filho
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            // Caso 3: nó com dois filhos — substitui pelo sucessor in-order
            // (menor elemento da subárvore direita)
            AVLNode successor = findMin(node.right);
            node.rule         = successor.rule;
            node.right        = delete(node.right, successor.rule.getRuleId());
        }

        updateHeight(node);
        return rebalance(node);
    }

    // =========================================================================
    // Operações de leitura — search
    // =========================================================================

    /** {@inheritDoc} */
    @Override
    public PacketRule search(int ruleId) {
        AVLNode result = search(root, ruleId);
        return (result != null) ? result.rule : null;
    }

    private AVLNode search(AVLNode node, int ruleId) {
        if (node == null) {
            return null;
        }
        if (ruleId == node.rule.getRuleId()) {
            return node;
        }
        if (ruleId < node.rule.getRuleId()) {
            return search(node.left, ruleId);
        }
        return search(node.right, ruleId);
    }

    // =========================================================================
    // Inspeção estrutural (suporte ao Integrante 3 — QA)
    // =========================================================================

    /** {@inheritDoc} */
    @Override
    public int getHeight() {
        return height(root);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getRotationCount() {
        return rotationCounter.getCount();
    }

    /** {@inheritDoc} */
    @Override
    public void resetRotationCount() {
        rotationCounter.reset();
    }

    // =========================================================================
    // Utilitários de diagnóstico
    // =========================================================================

    /** {@inheritDoc} */
    @Override
    public String inOrderTraversal() {
        StringBuilder sb = new StringBuilder();
        inOrder(root, sb);
        // Remove a vírgula e espaço finais, se existirem
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private void inOrder(AVLNode node, StringBuilder sb) {
        if (node == null) return;
        inOrder(node.left, sb);
        sb.append(node.rule.getRuleId()).append(", ");
        inOrder(node.right, sb);
    }

    /** {@inheritDoc} */
    @Override
    public String toTreeString() {
        if (root == null) return "(árvore vazia)";
        StringBuilder sb = new StringBuilder();
        toTreeString(root, sb, "", "");
        return sb.toString();
    }

    private void toTreeString(AVLNode node, StringBuilder sb,
                              String prefix, String childPrefix) {
        sb.append(prefix).append(node).append("\n");
        if (node.left != null || node.right != null) {
            if (node.right != null) {
                toTreeString(node.right, sb,
                        childPrefix + "├── R: ", childPrefix + "│   ");
            }
            if (node.left != null) {
                toTreeString(node.left, sb,
                        childPrefix + "└── L: ", childPrefix + "    ");
            }
        }
    }

    // =========================================================================
    // Núcleo AVL — altura, fator de balanceamento e rotações
    // =========================================================================

    /**
     * Retorna a altura do nó, tratando {@code null} como {@code -1}.
     */
    int height(AVLNode node) {
        return (node == null) ? -1 : node.height;
    }

    /**
     * Atualiza a altura de um nó com base na altura de seus filhos.
     * Deve ser chamado após qualquer modificação nos filhos do nó.
     */
    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * Calcula o Fator de Balanceamento do nó.
     *
     * <ul>
     *   <li>{@code FB > 1}  → subárvore esquerda mais alta → rotação à direita</li>
     *   <li>{@code FB < -1} → subárvore direita mais alta  → rotação à esquerda</li>
     * </ul>
     */
    private int balanceFactor(AVLNode node) {
        return height(node.left) - height(node.right);
    }

    /**
     * Verifica o Fator de Balanceamento e aplica a rotação correta se necessário.
     *
     * <p>Quatro casos possíveis:</p>
     * <pre>
     *   FB > 1  e filho esquerdo FB >= 0 → Left-Left   → rotação simples direita
     *   FB > 1  e filho esquerdo FB <  0 → Left-Right  → rotação dupla esq-dir
     *   FB < -1 e filho direito  FB <= 0 → Right-Right → rotação simples esquerda
     *   FB < -1 e filho direito  FB >  0 → Right-Left  → rotação dupla dir-esq
     * </pre>
     */
    private AVLNode rebalance(AVLNode node) {
        int fb = balanceFactor(node);

        // Caso Left-Left — rotação simples à direita
        if (fb > 1 && balanceFactor(node.left) >= 0) {
            return rotateRight(node);
        }

        // Caso Left-Right — rotação dupla (esquerda no filho, direita na raiz)
        if (fb > 1 && balanceFactor(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Caso Right-Right — rotação simples à esquerda
        if (fb < -1 && balanceFactor(node.right) <= 0) {
            return rotateLeft(node);
        }

        // Caso Right-Left — rotação dupla (direita no filho, esquerda na raiz)
        if (fb < -1 && balanceFactor(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        // Nó já balanceado
        return node;
    }

    /**
     * Rotação simples à direita (caso Left-Left).
     *
     * <pre>
     *       z                y
     *      / \             /   \
     *     y   T4   →      x     z
     *    / \             / \   / \
     *   x   T3          T1 T2 T3 T4
     *  / \
     * T1  T2
     * </pre>
     *
     * @param z nó desbalanceado (raiz local antes da rotação)
     * @return nova raiz local após a rotação ({@code y})
     */
    private AVLNode rotateRight(AVLNode z) {
        AVLNode y  = z.left;
        AVLNode T3 = y.right;

        // Executa a rotação
        y.right = z;
        z.left  = T3;

        // Atualiza alturas (z primeiro, pois agora é filho de y)
        updateHeight(z);
        updateHeight(y);

        rotationCounter.increment();
        return y;
    }

    /**
     * Rotação simples à esquerda (caso Right-Right).
     *
     * <pre>
     *   z                    y
     *  / \                 /   \
     * T1   y       →      z     x
     *     / \            / \   / \
     *    T2   x         T1 T2 T3 T4
     *        / \
     *       T3  T4
     * </pre>
     *
     * @param z nó desbalanceado (raiz local antes da rotação)
     * @return nova raiz local após a rotação ({@code y})
     */
    private AVLNode rotateLeft(AVLNode z) {
        AVLNode y  = z.right;
        AVLNode T2 = y.left;

        // Executa a rotação
        y.left  = z;
        z.right = T2;

        // Atualiza alturas (z primeiro, pois agora é filho de y)
        updateHeight(z);
        updateHeight(y);

        rotationCounter.increment();
        return y;
    }

    // =========================================================================
    // Auxiliares internos
    // =========================================================================

    /**
     * Verifica se existe um nó com o {@code ruleId} fornecido.
     * Usado internamente antes da deleção para retornar {@code false}
     * sem alterar o estado da árvore.
     */
    private boolean contains(AVLNode node, int ruleId) {
        if (node == null) return false;
        if (ruleId == node.rule.getRuleId()) return true;
        if (ruleId < node.rule.getRuleId()) return contains(node.left, ruleId);
        return contains(node.right, ruleId);
    }

    /**
     * Retorna o nó com o menor {@code ruleId} na subárvore enraizada em
     * {@code node}. Utilizado para encontrar o sucessor in-order na deleção.
     */
    private AVLNode findMin(AVLNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
}