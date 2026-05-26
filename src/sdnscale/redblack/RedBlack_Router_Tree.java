package sdnscale.redblack;

import sdnscale.core.RouterTree;
import sdnscale.model.PacketRule;
import sdnscale.util.RotationCounter;

/**
 * Implementação da Árvore Red-Black para o motor de busca do projeto SDN-Scale.
 *
 * <p>Gerencia objetos {@link PacketRule} mantendo as 5 propriedades
 * fundamentais da Red-Black Tree em todas as operações de escrita,
 * garantindo altura máxima {@code h ≤ 2·log₂(n+1)}.</p>
 *
 * <p><b>Estratégia de balanceamento:</b> Diferente da AVL, a RBT prioriza
 * recoloração antes de recorrer a rotações, o que resulta em menos rotações
 * totais em workloads write-intensive — dado central para a análise de
 * trade-off do Integrante 3.</p>
 *
 * <p><b>Nó sentinela NIL:</b> Todos os ponteiros "nulos" apontam para
 * {@link RBNode#NIL}, um nó BLACK estático. Isso elimina verificações de
 * {@code null} e simplifica o código de rotação e correção.</p>
 *
 * @author  Integrante 1 – Lead Software Engineer
 * @version 1.0
 * @see     RouterTree
 */
public class RedBlack_Router_Tree implements RouterTree {

    // =========================================================================
    // Estado interno
    // =========================================================================

    /** Raiz da árvore. Aponta para {@link RBNode#NIL} se vazia. */
    private RBNode root;

    /** Número de regras armazenadas atualmente. */
    private int size;

    /** Contador de rotações compartilhado com os benchmarks do Integrante 2. */
    private final RotationCounter rotationCounter;

    // =========================================================================
    // Construtor
    // =========================================================================

    /**
     * Cria uma RedBlack_Router_Tree vazia.
     * A raiz inicia apontando para o sentinela NIL.
     */
    public RedBlack_Router_Tree() {
        this.root            = RBNode.NIL;
        this.size            = 0;
        this.rotationCounter = RotationCounter.getInstance("RBT");
    }

    // =========================================================================
    // Operações de escrita — insert
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>O nó é inserido como RED e posicionado via BST padrão.
     * Em seguida, {@link #fixInsert(RBNode)} restaura as propriedades
     * de coloração que podem ter sido violadas.</p>
     */
    @Override
    public void insert(PacketRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("A regra não pode ser null.");
        }

        RBNode novo = new RBNode(rule);

        // Inserção BST padrão
        RBNode pai   = RBNode.NIL;
        RBNode atual = root;

        while (atual != RBNode.NIL) {
            pai = atual;
            int cmp = rule.compareTo(atual.rule);
            if (cmp < 0) {
                atual = atual.left;
            } else if (cmp > 0) {
                atual = atual.right;
            } else {
                throw new IllegalStateException(
                        "Já existe uma regra com o ID " + rule.getRuleId() + " na árvore.");
            }
        }

        novo.parent = pai;

        if (pai == RBNode.NIL) {
            root = novo;           // Árvore estava vazia
        } else if (rule.compareTo(pai.rule) < 0) {
            pai.left = novo;
        } else {
            pai.right = novo;
        }

        size++;
        fixInsert(novo);
    }

    /**
     * Restaura as propriedades Red-Black após a inserção.
     *
     * <p>Um nó RED inserido pode violar a propriedade 4 (nó RED não pode
     * ter filho RED). O algoritmo trata isso em três casos baseados na
     * cor do tio do nó inserido, repetindo até a raiz.</p>
     */
    private void fixInsert(RBNode z) {
        while (z.parent.color == RBNode.Color.RED) {
            if (z.parent == z.parent.parent.left) {
                // Pai é filho esquerdo do avô
                RBNode tio = z.parent.parent.right;

                if (tio.color == RBNode.Color.RED) {
                    // Caso 1: tio é RED → recoloração
                    z.parent.color         = RBNode.Color.BLACK;
                    tio.color              = RBNode.Color.BLACK;
                    z.parent.parent.color  = RBNode.Color.RED;
                    z = z.parent.parent;

                } else {
                    if (z == z.parent.right) {
                        // Caso 2: z é filho direito → rotação esquerda no pai
                        z = z.parent;
                        rotateLeft(z);
                    }
                    // Caso 3: z é filho esquerdo → recoloração + rotação direita no avô
                    z.parent.color        = RBNode.Color.BLACK;
                    z.parent.parent.color = RBNode.Color.RED;
                    rotateRight(z.parent.parent);
                }

            } else {
                // Pai é filho direito do avô (espelho)
                RBNode tio = z.parent.parent.left;

                if (tio.color == RBNode.Color.RED) {
                    // Caso 1 espelho: recoloração
                    z.parent.color        = RBNode.Color.BLACK;
                    tio.color             = RBNode.Color.BLACK;
                    z.parent.parent.color = RBNode.Color.RED;
                    z = z.parent.parent;

                } else {
                    if (z == z.parent.left) {
                        // Caso 2 espelho: rotação direita no pai
                        z = z.parent;
                        rotateRight(z);
                    }
                    // Caso 3 espelho: recoloração + rotação esquerda no avô
                    z.parent.color        = RBNode.Color.BLACK;
                    z.parent.parent.color = RBNode.Color.RED;
                    rotateLeft(z.parent.parent);
                }
            }
        }
        // Propriedade 2: raiz sempre BLACK
        root.color = RBNode.Color.BLACK;
    }

    // =========================================================================
    // Operações de escrita — delete
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Desafio da Deleção:</b> A remoção em RBT é o algoritmo mais
     * complexo da estrutura. Após remover o nó, {@link #fixDelete(RBNode)}
     * restaura as propriedades de coloração percorrendo até a raiz,
     * tratando 4 casos distintos baseados na cor do irmão do nó removido.</p>
     */
    @Override
    public boolean delete(int ruleId) {
        RBNode alvo = search(root, ruleId);
        if (alvo == RBNode.NIL) {
            return false;
        }
        deleteNode(alvo);
        size--;
        return true;
    }

    private void deleteNode(RBNode z) {
        RBNode y = z;
        RBNode x;
        RBNode.Color corOriginalY = y.color;

        if (z.left == RBNode.NIL) {
            // Caso 1: sem filho esquerdo
            x = z.right;
            transplant(z, z.right);

        } else if (z.right == RBNode.NIL) {
            // Caso 2: sem filho direito
            x = z.left;
            transplant(z, z.left);

        } else {
            // Caso 3: dois filhos — encontra sucessor in-order
            y = findMin(z.right);
            corOriginalY = y.color;
            x = y.right;

            if (y.parent == z) {
                x.parent = y;
            } else {
                transplant(y, y.right);
                y.right        = z.right;
                y.right.parent = y;
            }

            transplant(z, y);
            y.left        = z.left;
            y.left.parent = y;
            y.color       = z.color;
        }

        // Se o nó removido era BLACK, pode ter violado a propriedade 5
        if (corOriginalY == RBNode.Color.BLACK) {
            fixDelete(x);
        }
    }

    /**
     * Substitui a subárvore enraizada em {@code u} pela subárvore enraizada
     * em {@code v}, atualizando o ponteiro do pai de {@code u}.
     */
    private void transplant(RBNode u, RBNode v) {
        if (u.parent == RBNode.NIL) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    /**
     * Restaura as propriedades Red-Black após a deleção.
     *
     * <p>Trata 4 casos baseados na cor do irmão de {@code x}, repetindo
     * até que {@code x} seja a raiz ou um nó RED (que é recolorido para
     * BLACK no final).</p>
     */
    private void fixDelete(RBNode x) {
        while (x != root && x.color == RBNode.Color.BLACK) {
            if (x == x.parent.left) {
                RBNode irmao = x.parent.right;

                // Caso 1: irmão é RED → recoloração + rotação
                if (irmao.color == RBNode.Color.RED) {
                    irmao.color    = RBNode.Color.BLACK;
                    x.parent.color = RBNode.Color.RED;
                    rotateLeft(x.parent);
                    irmao = x.parent.right;
                }

                if (irmao.left.color  == RBNode.Color.BLACK &&
                        irmao.right.color == RBNode.Color.BLACK) {
                    // Caso 2: ambos os filhos do irmão são BLACK → recoloração
                    irmao.color = RBNode.Color.RED;
                    x = x.parent;

                } else {
                    if (irmao.right.color == RBNode.Color.BLACK) {
                        // Caso 3: filho direito do irmão é BLACK → rotação direita
                        irmao.left.color = RBNode.Color.BLACK;
                        irmao.color      = RBNode.Color.RED;
                        rotateRight(irmao);
                        irmao = x.parent.right;
                    }
                    // Caso 4: filho direito do irmão é RED → rotação esquerda
                    irmao.color        = x.parent.color;
                    x.parent.color     = RBNode.Color.BLACK;
                    irmao.right.color  = RBNode.Color.BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }

            } else {
                // Espelho: x é filho direito
                RBNode irmao = x.parent.left;

                if (irmao.color == RBNode.Color.RED) {
                    irmao.color    = RBNode.Color.BLACK;
                    x.parent.color = RBNode.Color.RED;
                    rotateRight(x.parent);
                    irmao = x.parent.left;
                }

                if (irmao.right.color == RBNode.Color.BLACK &&
                        irmao.left.color  == RBNode.Color.BLACK) {
                    irmao.color = RBNode.Color.RED;
                    x = x.parent;

                } else {
                    if (irmao.left.color == RBNode.Color.BLACK) {
                        irmao.right.color = RBNode.Color.BLACK;
                        irmao.color       = RBNode.Color.RED;
                        rotateLeft(irmao);
                        irmao = x.parent.left;
                    }
                    irmao.color       = x.parent.color;
                    x.parent.color    = RBNode.Color.BLACK;
                    irmao.left.color  = RBNode.Color.BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = RBNode.Color.BLACK;
    }

    // =========================================================================
    // Operações de leitura — search
    // =========================================================================

    /** {@inheritDoc} */
    @Override
    public PacketRule search(int ruleId) {
        RBNode result = search(root, ruleId);
        return (result != RBNode.NIL) ? result.rule : null;
    }

    private RBNode search(RBNode node, int ruleId) {
        if (node == RBNode.NIL) {
            return RBNode.NIL;
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

    private int height(RBNode node) {
        if (node == RBNode.NIL) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
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
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private void inOrder(RBNode node, StringBuilder sb) {
        if (node == RBNode.NIL) return;
        inOrder(node.left, sb);
        sb.append(node.rule.getRuleId()).append(", ");
        inOrder(node.right, sb);
    }

    /** {@inheritDoc} */
    @Override
    public String toTreeString() {
        if (root == RBNode.NIL) return "(árvore vazia)";
        StringBuilder sb = new StringBuilder();
        toTreeString(root, sb, "", "");
        return sb.toString();
    }

    private void toTreeString(RBNode node, StringBuilder sb,
                              String prefix, String childPrefix) {
        if (node == RBNode.NIL) return;
        sb.append(prefix).append(node).append("\n");
        if (node.left != RBNode.NIL || node.right != RBNode.NIL) {
            if (node.right != RBNode.NIL) {
                toTreeString(node.right, sb,
                        childPrefix + "├── R: ", childPrefix + "│   ");
            }
            if (node.left != RBNode.NIL) {
                toTreeString(node.left, sb,
                        childPrefix + "└── L: ", childPrefix + "    ");
            }
        }
    }

    // =========================================================================
    // Núcleo RBT — rotações
    // =========================================================================

    /**
     * Rotação simples à esquerda.
     *
     * <pre>
     *   x                y
     *  / \             /   \
     * T1   y    →     x     T3
     *     / \        / \
     *    T2  T3     T1  T2
     * </pre>
     */
    private void rotateLeft(RBNode x) {
        RBNode y = x.right;
        x.right  = y.left;

        if (y.left != RBNode.NIL) {
            y.left.parent = x;
        }

        y.parent = x.parent;

        if (x.parent == RBNode.NIL) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left   = x;
        x.parent = y;

        rotationCounter.increment();
    }

    /**
     * Rotação simples à direita.
     *
     * <pre>
     *       y              x
     *      / \           /   \
     *     x   T3   →    T1    y
     *    / \                 / \
     *   T1  T2              T2  T3
     * </pre>
     */
    private void rotateRight(RBNode y) {
        RBNode x = y.left;
        y.left   = x.right;

        if (x.right != RBNode.NIL) {
            x.right.parent = y;
        }

        x.parent = y.parent;

        if (y.parent == RBNode.NIL) {
            root = x;
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right  = y;
        y.parent = x;

        rotationCounter.increment();
    }

    // =========================================================================
    // Auxiliar interno
    // =========================================================================

    /**
     * Retorna o nó com o menor {@code ruleId} na subárvore enraizada em
     * {@code node}. Usado para encontrar o sucessor in-order na deleção.
     */
    private RBNode findMin(RBNode node) {
        while (node.left != RBNode.NIL) {
            node = node.left;
        }
        return node;
    }
}