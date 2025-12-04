# Relatório do Projeto: Sistema de Xadrez em Java

### 1. Introdução

A ideia desse projeto foi fazer um **Sistema de Xadrez** rodando no console usando Java. O principal objetivo aqui não foi só fazer o jogo funcionar, mas sim meter a mão na massa e aplicar de verdade os conceitos de **POO (Programação Orientada a Objetos)**. [cite_start]O jogo está completo, com todas as regras de movimento, além dos movimentos especiais (Roque, En Passant, Promoção) e a detecção de Xeque e Xeque-mate [cite: 245-246, 249, 275].

---

### 2. A Arquitetura: Dividindo o B.O. (Camadas)

[cite_start]A gente organizou o código em três partes (o famoso **Padrão de Camadas**) para não virar uma bagunça e para cada pedaço cuidar só do seu trabalho[cite: 55, 90].

#### 2.1. Camada `board` (A Estrutura Física)
Essa é a base. [cite_start]Ela só se preocupa com a **matriz** 8x8 [cite: 31, 57] e as coordenadas puras (`Position`).

* **Classes Chave:** `Board`, `Position`, `Piece`.
* [cite_start]**Decisão:** Foi essencial criar a **`BoardException`** aqui para barrar erros de coordenadas (tipo tentar acessar a linha 9), protegendo o sistema de baixo nível[cite: 67, 69].

#### 2.2. Camada `chess` (As Regras do Jogo)
Aqui é onde a mágica do xadrez acontece. [cite_start]É o motor que conhece as regras, o turno, o Xeque, etc.[cite: 45].

* **Classes Chave:** `ChessMatch`, `ChessPiece`, classes de Peças.
* **Decisão:** Essa camada lida com as coordenadas de xadrez (`a1` a `h8`) e faz a conversão para as coordenadas da matriz, isolando a lógica complexa do resto.

#### 2.3. Camada `application` (A UI)
É só a casca. [cite_start]Responsável por mostrar o jogo no console[cite: 46].

* **Classes Chave:** `Program`, `UI`.
* [cite_start]**Decisão:** Usamos códigos **ANSI** na classe `UI` para colorir as peças e os movimentos possíveis, o que facilitou muito a jogabilidade[cite: 92].

---

### 3. As Decisões de POO que Fizeram a Diferença

[cite_start]Aqui está o que realmente valeu a pena no projeto[cite: 17, 24, 47, 63, 72, 81, 108, 133, 147, 154, 166, 178, 214, 220, 227, 235, 242]:

#### 3.1. Herança e Polimorfismo (O Coração do Movimento)
Essa foi a solução elegante para o movimento:

* [cite_start]**Hierarquia:** Criamos a `Piece` (a classe mais abstrata) [cite: 134] [cite_start]e dela herdamos a `ChessPiece` [cite: 51] (que já tem cor e contador de movimento). [cite_start]Todas as peças específicas (`King`, `Rook`, `Pawn`, etc.) herdam delas[cite: 64].
* [cite_start]**Polimorfismo:** Declaramos o método **`public abstract boolean[][] possibleMoves()`** [cite: 128] na classe base `Piece`. [cite_start]Isso obrigou cada peça (Torre, Peão, Bispo) a escrever sua própria lógica de movimento[cite: 65, 156, 222, 230, 237, 244]. A classe `ChessMatch` só precisa chamar esse método, e o Java se vira para executar a lógica certa.

#### 3.2. Encapsulamento e Associações
Para proteger o jogo de "trapaças" e erros:

* [cite_start]**Encapsulamento:** Tudo que é vital (a matriz `pieces`, o `moveCount`, o `currentPlayer`) é **`private`**[cite: 18, 28, 49, 84, 109, 155, 167, 180, 215, 221, 228, 236, 243]. Ninguém acessa isso diretamente.
* [cite_start]**Associações:** O `Board` e a `Piece` têm associações[cite: 27].

#### 3.3. Tratamento de Exceções (Programação Defensiva)
Em vez de deixar o programa quebrar, a gente usou as exceções para se defender:

* [cite_start]**`BoardException`:** Para erros estruturais[cite: 74].
* [cite_start]**`ChessException`:** Para erros de regras[cite: 78, 83, 109, 117, 135, 141, 168].
* [cite_start]**Decisão Crítica:** A regra de **não se colocar em Xeque** é resolvida com exceções: a gente **simula** o movimento, checa se deu Xeque, e se deu, o movimento é **desfeito** (`undoMove`) e a exceção é lançada[cite: 186].

---

### 4. As Regras que Deram Mais Trabalho

* [cite_start]**Xeque-mate:** Exigiu um teste duplo: primeiro checar se está em Xeque, e depois iterar sobre *todos* os movimentos possíveis do jogador em Xeque para ver se existe *pelo menos um* movimento que tire o Rei da ameaça[cite: 197].
* **Movimentos Especiais (Roque, En Passant):** Foram complexos porque alteram o estado de várias peças ou dependem do estado anterior da partida. [cite_start]A gente teve que atualizar os métodos **`makeMove()`** e **`undoMove()`** para dar conta dessas alterações [cite: 247-248, 272-273].
* [cite_start]**Estruturas de Dados:** Usamos a **Matriz** para o tabuleiro [cite: 31] [cite_start]e **Listas** (`ArrayList`) na `ChessMatch` para rastrear dinamicamente as peças ativas e capturadas[cite: 174, 183], o que facilitou a busca e o gerenciamento do estado.

---

### 5. Conclusão

O projeto foi um sucesso, conseguimos implementar todas as regras e o código ficou organizado graças à arquitetura em camadas e ao uso consistente do POO. O maior aprendizado foi entender como o **Polimorfismo** realmente simplifica a lógica de movimento e como o **Encapsulamento** é vital para manter o estado do jogo sob controle.
