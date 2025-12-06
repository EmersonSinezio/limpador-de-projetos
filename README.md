# Limpador de Projetos

O **Limpador de Projetos** é uma ferramenta desenvolvida em Java para auxiliar desenvolvedores na limpeza de artefatos de build e dependências de projetos, liberando espaço em disco. A ferramenta oferece tanto uma interface gráfica (GUI) quanto uma versão em linha de comando (CLI).

## 🚀 Funcionalidades

- **Limpeza de Diretórios**: Identifica e remove automaticamente pastas comuns de build e dependências, como:
  - `node_modules` (Node.js)
  - `target` (Maven)
  - `build` (Gradle / Outros)
  - `dist` (Frontend builds)
  - `.next` (Next.js)
  - `bin` (C# / Java)
  - `obj` (C#)
- **Backup (Compactação)**: Opção para criar um backup compactado (`.zip`) do diretório selecionado antes de realizar a limpeza.
- **Relatório de Estatísticas**: Exibe o número de pastas removidas e o espaço total liberado após a operação.
- **Processamento Paralelo**: Utiliza threads separadas para operações de compactação e limpeza, garantindo que a interface permaneça responsiva.

## 🛠️ Tecnologias Utilizadas

- **Java 22**: Linguagem base do projeto.
- **JavaFX 22**: Framework para construção da interface gráfica.
- **Apache Commons Compress**: Biblioteca para criação eficiente de arquivos zip.
- **Maven**: Gerenciador de dependências e build.

## 📋 Pré-requisitos

Certifique-se de ter instalado em sua máquina:

- [Java JDK 22](https://jdk.java.net/22/)
- [Maven](https://maven.apache.org/)

## 🔧 Instalação e Build

1. **Clone o repositório**:

   ```bash
   git clone https://github.com/seu-usuario/Limpador_projetos.git
   cd Limpador_projetos
   ```

2. **Compile o projeto**:
   ```bash
   mvn clean package
   ```

## 💻 Como Usar

### Interface Gráfica (GUI)

Para iniciar a aplicação com interface gráfica:

```bash
mvn javafx:run
```

Ou execute o jar gerado (se configurado):

```bash
java -jar target/Limpador_projetos-1.0-SNAPSHOT.jar
```

1. Clique em **"Selecionar Pasta"** para escolher o diretório raiz onde estão seus projetos.
2. (Opcional) Marque a caixa **"Compactar antes de limpar"** se desejar fazer um backup.
3. Clique em **"Iniciar Limpeza"**.
4. Acompanhe o log na tela para ver o progresso e o resultado final.

### Linha de Comando (CLI)

Para usar a versão em linha de comando (executando a classe `ProjectCleaner` diretamente):

```bash
mvn exec:java -Dexec.mainClass="org.example.limpador_projetos.ProjectCleaner"
```

1. O programa solicitará o caminho da pasta raiz.
2. Digite o caminho e pressione Enter.
3. O script escaneará os diretórios e removerá as pastas alvo, exibindo o espaço liberado ao final.

## 📂 Estrutura do Projeto

- `src/main/java/org/example/limpador_projetos/`:
  - `HelloApplication.java`: Ponto de entrada da aplicação JavaFX.
  - `HelloController.java`: Controlador da interface gráfica.
  - `ProjectCleaner.java`: Implementação da versão CLI.
  - `ParallelCompressor.java`: Lógica de compactação paralela.
- `src/main/resources/org/example/limpador_projetos/`:
  - `hello-view.fxml`: Layout da interface gráfica.
  - `style.css`: Estilos da interface.

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
