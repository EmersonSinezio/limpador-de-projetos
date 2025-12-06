package org.example.limpador_projetos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

public class HelloController {

    @FXML private Label pathLabel;
    @FXML private TextArea logArea;
    @FXML private CheckBox compressCheckBox;
    
    // Contêineres e Labels de destino
    @FXML private HBox destinationContainer;
    @FXML private Label destinationLabel;
    
    // Botões (para desabilitar durante o processo)
    @FXML private Button compressButton;
    @FXML private Button cleanButton;

    private File selectedDirectory;
    private File destinationDirectory;

    // Lista de pastas que queremos apagar
    private static final Set<String> PASTAS_ALVO = Set.of(
            "node_modules",
            "target",       // Java Maven
            "build",        // Java Gradle / Outros
            "dist",         // Frontend builds
            ".next",        // Next.js
            "bin",          // C# / Java
            "obj"           // C#
    );

    // Contadores para estatísticas
    private long espacoLiberado = 0;
    private int pastasRemovidas = 0;

    @FXML
    public void initialize() {
        // Vincula a visibilidade da opção de destino ao CheckBox
        destinationContainer.visibleProperty().bind(compressCheckBox.selectedProperty());
        destinationContainer.managedProperty().bind(compressCheckBox.selectedProperty());
    }

    @FXML
    protected void onSelectDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta do projeto");
        File file = directoryChooser.showDialog(new Stage());

        if (file != null) {
            selectedDirectory = file;
            pathLabel.setText(file.getAbsolutePath());
            logToScreen("Alvo selecionado: " + file.getName());
        }
    }

    @FXML
    protected void onSelectDestination() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Onde salvar o backup?");
        File file = directoryChooser.showDialog(new Stage());

        if (file != null) {
            destinationDirectory = file;
            destinationLabel.setText(file.getAbsolutePath());
            logToScreen("Destino definido: " + file.getName());
        }
    }

    // === LÓGICA DO BOTÃO COMPACTAR ===
    @FXML
    protected void onStartCompression() {
        if (selectedDirectory == null) {
            logToScreen("ERRO: Nenhuma pasta selecionada para compactar.");
            return;
        }

        // Define onde salvar (Se não escolheu destino, salva na pasta pai do alvo)
        File pastaDestino = (destinationDirectory != null) ? destinationDirectory : selectedDirectory.getParentFile();
        String nomeZip = selectedDirectory.getName() + "_backup.zip";
        File arquivoZip = new File(pastaDestino, nomeZip);

        logArea.clear();
        logToScreen("Iniciando compactação para: " + arquivoZip.getName() + "...");
        logToScreen("Por favor, aguarde...");

        // Roda em uma Thread separada para não travar a tela
        new Thread(() -> {
            try {
                long startComp = System.currentTimeMillis();
                // Chama sua classe ParallelCompressor
                ParallelCompressor compressor = new ParallelCompressor();
                compressor.compressFolder(selectedDirectory.toPath(), arquivoZip.toPath());
                long endComp = System.currentTimeMillis();
                long compressionTime = endComp - startComp;

                // Atualiza a tela quando terminar (Platform.runLater é obrigatório para mexer na UI de outra thread)
                Platform.runLater(() -> {
                    logToScreen("✅ Compactação concluída com sucesso!");
                    logToScreen("Salvo em: " + arquivoZip.getAbsolutePath());
                    logToScreen("Tempo total: " + (compressionTime / 1000.0) + " s");
                });

            } catch (Exception e) {
                Platform.runLater(() -> logToScreen("❌ Erro ao compactar: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    protected void onStartCleaning() {
        if (selectedDirectory == null) {
            logToScreen("ERRO: Selecione uma pasta primeiro!");
            return;
        }

        logArea.clear();
        logToScreen("Iniciando processo de limpeza...");

        boolean shouldCompress = compressCheckBox.isSelected();
        
        // Resetar contadores
        espacoLiberado = 0;
        pastasRemovidas = 0;

        // Executar em uma thread separada para não travar a UI
        new Thread(() -> {
            try {
                long compressionTime = 0;
                if (shouldCompress) {
                    // Se o usuário não escolheu destino, usa a pasta pai da pasta selecionada
                    File pastaDestinoFinal = (destinationDirectory != null) ? destinationDirectory : selectedDirectory.getParentFile();
                    
                    String nomeZip = selectedDirectory.getName() + "_backup.zip";
                    File arquivoZip = new File(pastaDestinoFinal, nomeZip);
                    
                    logToScreen("Compactando para: " + arquivoZip.getAbsolutePath());
                    
                    long startComp = System.currentTimeMillis();
                    ParallelCompressor compressor = new ParallelCompressor();
                    compressor.compressFolder(selectedDirectory.toPath(), arquivoZip.toPath());
                    long endComp = System.currentTimeMillis();
                    compressionTime = endComp - startComp;
                    
                    logToScreen("Compactação concluída.");
                    logToScreen("Tempo de compactação: " + (compressionTime / 1000.0) + " segundos");
                }

                logToScreen("Iniciando varredura em: " + selectedDirectory.getAbsolutePath());
                long startClean = System.currentTimeMillis();
                scanAndClean(selectedDirectory.toPath());
                long endClean = System.currentTimeMillis();
                long cleaningTime = endClean - startClean;
                
                long finalCompressionTime = compressionTime;
                Platform.runLater(() -> {
                    logToScreen("\n=== CONCLUÍDO ===");
                    logToScreen("Pastas removidas: " + pastasRemovidas);
                    logToScreen(String.format("Espaço liberado estimado: %.2f MB", espacoLiberado / (1024.0 * 1024.0)));
                    if (shouldCompress) {
                         logToScreen("Tempo total de compactação: " + (finalCompressionTime / 1000.0) + " s");
                    }
                    logToScreen("Tempo total de limpeza: " + (cleaningTime / 1000.0) + " s");
                });
            } catch (IOException e) {
                Platform.runLater(() -> logToScreen("Erro ao acessar arquivos: " + e.getMessage()));
            }
        }).start();
    }

    // Método auxiliar para escrever no log
    private void logToScreen(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private void scanAndClean(Path startPath) throws IOException {
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Verifica se o nome da pasta atual está na nossa lista negra
                String dirName = dir.getFileName().toString();

                if (PASTAS_ALVO.contains(dirName)) {
                    Platform.runLater(() -> logToScreen("Encontrado: " + dir.toAbsolutePath()));
                    
                    try {
                        long size = calcularTamanho(dir); // Calcula tamanho antes de apagar para o relatório
                        deletarRecursivamente(dir);
                        espacoLiberado += size;
                        pastasRemovidas++;
                        Platform.runLater(() -> logToScreen(" [DELETADO] - " + (size / 1024) + " KB liberados."));
                        
                        // IMPORTANTE: Retorna SKIP_SUBTREE para não tentar entrar numa pasta que já não existe
                        return FileVisitResult.SKIP_SUBTREE;
                    } catch (IOException e) {
                        Platform.runLater(() -> logToScreen(" [ERRO] Não foi possível deletar: " + e.getMessage()));
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Ignora erros de permissão e continua (ex: pastas do sistema)
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Método auxiliar para deletar pasta cheia (Java não deleta pasta com arquivos dentro nativamente)
    private void deletarRecursivamente(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.deleteIfExists(path);
        }
    }

    // Método auxiliar apenas para calcular o tamanho da pasta (estatística)
    private long calcularTamanho(Path path) {
        final long[] size = {0};
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size[0] += attrs.size();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // Ignora erro de calculo
        }
        return size[0];
    }
}
