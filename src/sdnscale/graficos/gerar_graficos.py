import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import os

# ============================================================
# Configurações globais de estilo
# ============================================================
plt.rcParams.update({
    'font.family': 'DejaVu Sans',
    'font.size': 11,
    'axes.titlesize': 13,
    'axes.titleweight': 'bold',
    'axes.grid': True,
    'grid.alpha': 0.4,
    'lines.linewidth': 2.2,
    'lines.markersize': 7,
})

COR_AVL = '#1f77b4'   # Azul
COR_RBT = '#d62728'   # Vermelho
PASTA_SAIDA = 'graficos'
CSV_PATH = 'dados/benchmark_results.csv'

# ============================================================
# Leitura do CSV
# ============================================================
if not os.path.exists(CSV_PATH):
    print(f"❌ Arquivo '{CSV_PATH}' não encontrado.")
    print("   Execute primeiro o BenchmarkCSVExporter.java no IntelliJ.")
    exit(1)

df = pd.read_csv(CSV_PATH)
volumes = df['volume'] / 1_000  # Converte para milhares (eixo X: "10k, 20k...")

os.makedirs(PASTA_SAIDA, exist_ok=True)

# ============================================================
# Função auxiliar para salvar gráfico
# ============================================================
def salvar(nome_arquivo):
    caminho = os.path.join(PASTA_SAIDA, nome_arquivo)
    plt.tight_layout()
    plt.savefig(caminho, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"✅ Salvo: {caminho}")

def formatar_eixo_x(ax):
    ax.xaxis.set_major_formatter(ticker.FuncFormatter(
        lambda x, _: f'{int(x)}k'
    ))

# ============================================================
# Gráfico 1 — Inserção (ns)
# ============================================================
fig, ax = plt.subplots(figsize=(8, 5))

ax.plot(volumes, df['avl_insercao_ns'], color=COR_AVL, marker='o', label='AVL')
ax.plot(volumes, df['rbt_insercao_ns'], color=COR_RBT, marker='s', label='Red-Black')

ax.set_title('Tempo de Inserção — AVL vs Red-Black')
ax.set_xlabel('Volume de Dados (nós)')
ax.set_ylabel('Tempo Total (ns)')
ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f'{x/1_000_000:.1f}M' if x >= 1_000_000 else f'{x/1_000:.0f}k'))
formatar_eixo_x(ax)
ax.legend()
ax.set_xticks(volumes)

salvar('grafico_insercao.png')

# ============================================================
# Gráfico 2 — Busca (ns)
# ============================================================
fig, ax = plt.subplots(figsize=(8, 5))

ax.plot(volumes, df['avl_busca_ns'], color=COR_AVL, marker='o', label='AVL')
ax.plot(volumes, df['rbt_busca_ns'], color=COR_RBT, marker='s', label='Red-Black')

ax.set_title('Tempo de Busca — AVL vs Red-Black')
ax.set_xlabel('Volume de Dados (nós)')
ax.set_ylabel('Tempo Total (ns)')
ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f'{x/1_000:.0f}k'))
formatar_eixo_x(ax)
ax.legend()
ax.set_xticks(volumes)

salvar('grafico_busca.png')

# ============================================================
# Gráfico 3 — Deleção 20% (ns)
# ============================================================
fig, ax = plt.subplots(figsize=(8, 5))

ax.plot(volumes, df['avl_delecao_ns'], color=COR_AVL, marker='o', label='AVL')
ax.plot(volumes, df['rbt_delecao_ns'], color=COR_RBT, marker='s', label='Red-Black')

ax.set_title('Tempo de Deleção (20% dos nós) — AVL vs Red-Black')
ax.set_xlabel('Volume de Dados (nós)')
ax.set_ylabel('Tempo Total (ns)')
ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f'{x/1_000_000:.1f}M' if x >= 1_000_000 else f'{x/1_000:.0f}k'))
formatar_eixo_x(ax)
ax.legend()
ax.set_xticks(volumes)

salvar('grafico_delecao.png')

# ============================================================
# Gráfico 4 — Rotações totais (inserção + deleção)
# ============================================================
fig, ax = plt.subplots(figsize=(8, 5))

avl_rot_total = df['avl_rotacoes_insercao'] + df['avl_rotacoes_delecao']
rbt_rot_total = df['rbt_rotacoes_insercao'] + df['rbt_rotacoes_delecao']

ax.plot(volumes, avl_rot_total, color=COR_AVL, marker='o', label='AVL')
ax.plot(volumes, rbt_rot_total, color=COR_RBT, marker='s', label='Red-Black')

ax.set_title('Total de Rotações (Inserção + Deleção) — AVL vs Red-Black')
ax.set_xlabel('Volume de Dados (nós)')
ax.set_ylabel('Número de Rotações')
ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f'{x/1_000:.0f}k'))
formatar_eixo_x(ax)
ax.legend()
ax.set_xticks(volumes)

salvar('grafico_rotacoes.png')

# ============================================================
# Resumo final
# ============================================================
print()
print("Gráficos gerados na pasta 'graficos/':")
print("  - grafico_insercao.png")
print("  - grafico_busca.png")
print("  - grafico_delecao.png")
print("  - grafico_rotacoes.png")
print()
print("Adicione os PNGs ao relatório SBC na seção de Resultados.")