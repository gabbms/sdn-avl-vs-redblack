# SDN-Scale: AVL vs Red-Black Tree

Análise comparativa de desempenho entre Árvore AVL e Red-Black Tree aplicadas ao gerenciamento de regras de roteamento em um Load Balancer SDN simulado.

> **Disciplina:** Estrutura de Dados II  
> **Professor:** Ricardo Sekeff  
> **Instituição:** iCEV — Instituto de Ensino Superior

---

## Equipe

| Integrante  | Função | Responsabilidade |
|-------------|---|---|
| Gabriel M.  | Lead Software Engineer (Int. 1) | Estruturas AVL, Red-Black, utilitários |
| Eduardo C.  | DevOps & SRE (Int. 2) | Benchmarks, gráficos, coleta de dados |
| Clidenor J. | QA & Analytics (Int. 3) | Validação de invariantes, code review |

---

## Estrutura do Projeto

```
sdn-avl-vs-redblack/
├── src/
│   └── sdnscale/
│       ├── Main.java                        # Ponto de entrada — demonstração básica
│       ├── core/
│       │   └── RouterTree.java              # Interface polimórfica (Int. 1)
│       ├── model/
│       │   └── PacketRule.java              # Domínio: regra de roteamento (Int. 1)
│       ├── avl/
│       │   ├── AVLNode.java                 # Nó interno da AVL (Int. 1)
│       │   └── AVL_Router_Tree.java         # Árvore AVL completa (Int. 1)
│       ├── redblack/
│       │   ├── RBNode.java                  # Nó interno da Red-Black (Int. 1)
│       │   └── RedBlack_Router_Tree.java    # Red-Black Tree completa (Int. 1)
│       ├── util/
│       │   ├── DataGenerator.java           # Gerador de datasets com seed fixa (Int. 1)
│       │   └── RotationCounter.java         # Contador global de rotações (Int. 1)
│       ├── validation/
│       │   ├── InvariantChecker.java        # Validador de invariantes (Int. 3)
│       │   └── QARunner.java                # Executor local de validação (Int. 3)
│       └── benchmark/
│           ├── BenchmarkRunner.java         # Benchmark comparativo 4 fases (Int. 2)
│           └── BenchmarkCSVExporter.java    # Exportador CSV para gráficos (Int. 2)
├── dados/
│   └── benchmark_results.csv               # Resultados dos benchmarks (gerado)
├── graficos/
│   ├── gerar_graficos.py                   # Script Python para gráficos (Int. 2)
│   ├── grafico_insercao.png                # Gráfico de inserção (gerado)
│   ├── grafico_busca.png                   # Gráfico de busca (gerado)
│   ├── grafico_delecao.png                 # Gráfico de deleção (gerado)
│   └── grafico_rotacoes.png                # Gráfico de rotações (gerado)
└── resultados.txt                          # Output do BenchmarkRunner (Int. 2)
```

---

## Pré-requisitos

- **Java 21** ou superior
- **Python 3.8+** (apenas para gerar os gráficos)
- Bibliotecas Python: `matplotlib`, `pandas`

---

## Como Executar

### 1. Demonstração básica

Clique com botão direito em `Main.java` no IntelliJ → `Run 'Main.main()'`

Insere 10 regras, realiza buscas, deleta 20% e valida os invariantes.

### 2. Benchmark completo (100.000 regras)

Clique com botão direito em `BenchmarkRunner.java` → `Run 'BenchmarkRunner.main()'`

Executa as 4 fases:
- **Fase 1:** Inserção de 100.000 regras
- **Fase 2:** Busca de 10.000 regras
- **Fase 3:** Deleção de 20% dos nós (20.000 regras)
- **Fase 4:** Validação dos invariantes pós-deleção

### 3. Gerar dados CSV para gráficos

Clique com botão direito em `BenchmarkCSVExporter.java` → `Run 'BenchmarkCSVExporter.main()'`

Gera `dados/benchmark_results.csv` com resultados para 10 volumes (10k a 100k nós).

### 4. Gerar gráficos comparativos

```bash
# Instalar dependências Python (apenas na primeira vez)
pip install matplotlib pandas

# Gerar os gráficos (na raiz do projeto)
python graficos/gerar_graficos.py
```

Os 4 PNGs serão gerados na pasta `graficos/`.

### 5. Validação local dos invariantes (QA)

Clique com botão direito em `QARunner.java` → `Run 'QARunner.main()'`

Valida os invariantes com 1.000 nós antes do benchmark completo.

---

## Arquitetura

### Interface `RouterTree`

Contrato polimórfico que garante benchmarks idênticos entre as duas implementações:

```java
RouterTree avl = new AVL_Router_Tree();
RouterTree rbt = new RedBlack_Router_Tree();

// Mesmo código para as duas estruturas
avl.insert(rule);
rbt.insert(rule);
```

### `PacketRule`

Regra de roteamento com ordenação natural por `ruleId`:

```java
PacketRule rule = new PacketRule(
    42,              // ruleId (chave BST — imutável)
    "10.0.0.1/32",   // IP de origem
    "172.16.0.0/16", // IP de destino
    100              // prioridade (1–65535, OpenFlow)
);
```

### Invariantes garantidos

| Estrutura | Invariante | Verificação |
|---|---|---|
| AVL | `\|FB\| ≤ 1` em todos os nós | `InvariantChecker.checkAVL()` |
| AVL | `h < 1,44 · log₂(n+2)` | `InvariantChecker.checkAVL()` |
| Red-Black | 5 propriedades de coloração | `InvariantChecker.checkRedBlack()` |
| Red-Black | `h ≤ 2 · log₂(n+1)` | `InvariantChecker.checkRedBlack()` |
| Ambas | BST: in-order crescente | `InvariantChecker.checkBSTProperty()` |

---

## Resultados (seed=42, n=100.000)

| Métrica | AVL | Red-Black |
|---|---|---|
| Tempo de inserção (ms) | 87,16 | 40,90 |
| Rotações na inserção | 69.920 | 58.252 |
| Tempo de busca (ms) | 5,38 | 4,64 |
| Média por busca (ns) | 538,39 | 463,78 |
| Tempo de deleção 20% (ms) | 24,72 | 21,26 |
| Rotações na deleção | 7.211 | 6.634 |
| Altura final | 19 | 19 |
| Nós restantes | 80.000 | 80.000 |

**Conclusão:** A Red-Black Tree superou a AVL na inserção (53% mais rápida) e na busca. Ambas mantiveram altura final idêntica de 19 nós após a deleção dos 20%, confirmando o rebalanceamento correto.

---

## Gestão Git

### Branches

| Branch | Responsável | Conteúdo |
|---|---|---|
| `main` | Todos | Código aprovado e mergeado |
| `feature/int1-structures` | Integrante 1 | AVL, Red-Black, utilitários |
| `feature/int2-benchmark` | Integrante 2 | Benchmarks e gráficos |
| `feature/int3-qa` | Integrante 3 | Validação e QA |


