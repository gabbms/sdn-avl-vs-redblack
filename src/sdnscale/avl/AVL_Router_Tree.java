package sdnscale.avl;

import sdnscale.core.RouterTree;
import sdnscale.model.PacketRule;
import sdnscale.util.RotationCounter;


public class AVL_Router_Tree implements RouterTree {


    /** Raiz da árvore. {@code null} se a árvore estiver vazia. */
    private AVLNode root;

    /** Número de regras armazenadas atualmente. */
    private int size;

    /** Contador de rotações compartilhado com os benchmarks do Integrante 2. */
    private final RotationCounter rotationCounter;


    public AVL_Router_Tree() {
        this.root            = null;
        this.size            = 0;
        this.rotationCounter = RotationCounter.getInstance("AVL");
    }


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



    @Override
    public int getHeight() {
        return height(root);
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public int getRotationCount() {
        return rotationCounter.getCount();
    }


    @Override
    public void resetRotationCount() {
        rotationCounter.reset();
    }


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


    int height(AVLNode node) {
        return (node == null) ? -1 : node.height;
    }


    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }


    private int balanceFactor(AVLNode node) {
        return height(node.left) - height(node.right);
    }


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



    private boolean contains(AVLNode node, int ruleId) {
        if (node == null) return false;
        if (ruleId == node.rule.getRuleId()) return true;
        if (ruleId < node.rule.getRuleId()) return contains(node.left, ruleId);
        return contains(node.right, ruleId);
    }


    private AVLNode findMin(AVLNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
}