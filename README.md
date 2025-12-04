# Relatório do Projeto: Sistema de Xadrez em Java

### 1. Introdução

A ideia desse projeto foi fazer um **Sistema de Xadrez** rodando no console usando Java. O principal objetivo aqui não foi só fazer o jogo funcionar, mas sim aplicar de verdade os conceitos de **POO (Programação Orientada a Objetos)**. O jogo está completo, com todas as regras de movimento, além dos movimentos especiais (Roque, En Passant, Promoção) e a detecção de Xeque e Xeque-mate.

---

### 2. A Arquitetura: Dividindo o B.O. (Camadas)

A gente organizou o código em três partes para não virar uma bagunça e para cada pedaço cuidar só do seu trabalho.

#### 2.1. Camada `board` (A Estrutura Física)
Essa é a base. Ela só se preocupa com a **matriz** 8x8 e as coordenadas puras (`Position`).

* **Classes Chave:** `Board`, `Position`, `Piece`.
* **Decisão:** Foi essencial criar a **`BoardException`** aqui para barrar erros de coordenadas (tipo tentar acessar a linha 9), protegendo o sistema de baixo nível.

#### 2.2. Camada `chess` (As Regras do Jogo)
Aqui é onde a mágica do xadrez acontece. É o motor que conhece as regras, o turno, o Xeque, etc.

* **Classes Chave:** `ChessMatch`, `ChessPiece`, classes de Peças.
* **Decisão:** Essa camada lida com as coordenadas de xadrez (`a1` a `h8`) e faz a conversão para as coordenadas da matriz, isolando a lógica complexa do resto.

#### 2.3. Camada `application` (A UI)
Responsável por mostrar o jogo no console.

* **Classes Chave:** `Program`, `UI`.
* **Decisão:** Usamos códigos **ANSI** na classe `UI` para colorir as peças e os movimentos possíveis, o que facilitou muito a jogabilidade.

---

### 3. As Decisões de POO que Fizeram a Diferença

Aqui está o que realmente valeu a pena no projeto:

#### 3.1. Herança e Polimorfismo (O Coração do Movimento)
Essa foi a solução elegante para o movimento:

* **Hierarquia:** Criamos a `Piece` (a classe mais abstrata) e dela herdamos a `ChessPiece` (que já tem cor e contador de movimento). Todas as peças específicas (`King`, `Rook`, `Pawn`, etc.) herdam delas.
* **Polimorfismo:** Declaramos o método **`public abstract boolean[][] possibleMoves()`** na classe base `Piece`. Isso obrigou cada peça (Torre, Peão, Bispo) a escrever sua própria lógica de movimento. A classe `ChessMatch` só precisa chamar esse método, e o Java se vira para executar a lógica certa.

#### 3.2. Encapsulamento e Associações
Para proteger o jogo de "trapaças" e erros:

* **Encapsulamento:** Tudo que é vital (a matriz `pieces`, o `moveCount`, o `currentPlayer`) é **`private`**. Ninguém acessa isso diretamente.
* **Associações:** O `Board` e a `Piece` têm associações.

#### 3.3. Tratamento de Exceções (Programação Defensiva)
Em vez de deixar o programa quebrar, a gente usou as exceções para se defender:

* **`BoardException`:** Para erros estruturais.
* **`ChessException`:** Para erros de regras.
* **Decisão Crítica:** A regra de **não se colocar em Xeque** é resolvida com exceções: a gente **simula** o movimento, checa se deu Xeque, e se deu, o movimento é **desfeito** (`undoMove`) e a exceção é lançada.

---

### 4. As Regras que Deram Mais Trabalho

* **Xeque-mate:** Exigiu um teste duplo: primeiro checar se está em Xeque, e depois iterar sobre *todos* os movimentos possíveis do jogador em Xeque para ver se existe *pelo menos um* movimento que tire o Rei da ameaça.
* **Movimentos Especiais (Roque, En Passant):** Foram complexos porque alteram o estado de várias peças ou dependem do estado anterior da partida. A gente teve que atualizar os métodos **`makeMove()`** e **`undoMove()`** para dar conta dessas alterações.
* **Estruturas de Dados:** Usamos a **Matriz** para o tabuleiro e **Listas** (`ArrayList`) na `ChessMatch` para rastrear dinamicamente as peças ativas e capturadas, o que facilitou a busca e o gerenciamento do estado.

---

### 5. Conclusão

O projeto foi um sucesso, conseguimos implementar todas as regras e o código ficou organizado graças à arquitetura em camadas e ao uso consistente do POO. O maior aprendizado foi entender como o **Polimorfismo** realmente simplifica a lógica de movimento e como o **Encapsulamento** é vital para manter o estado do jogo sob controle.
