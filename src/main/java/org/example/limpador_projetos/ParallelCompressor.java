package org.example.limpador_projetos;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;

public class ParallelCompressor {

    public void compressFolder(Path sourceFolder, Path destinationZip) {
        // 1. Prepara o arquivo de saída
        File zipFile = destinationZip.toFile();
        if (zipFile.exists()) {
            zipFile.delete();
        }

        // 2. Detecta o hardware (núcleos do processador)
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Usando " + cores + " núcleos para compressão paralela...");

        // Cria um pool de threads baseado no número de núcleos
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        
        // Cria o compactador paralelo
        ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator(executor);

        // 3. Caminha pelos arquivos e adiciona tarefas ao compactador
        try {
            Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 1. Evita loop infinito (não zipar o próprio zip)
                    if (file.equals(destinationZip)) {
                        return FileVisitResult.CONTINUE;
                    }

                    // Verifica se é um Link Simbólico ou se (por algum motivo) o Java achou que uma pasta era arquivo.
                    if (attrs.isSymbolicLink() || !attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // Define o nome do arquivo dentro do ZIP (caminho relativo)
                    String entryName = sourceFolder.relativize(file).toString().replace("\\", "/");
                    
                    // Cria a entrada do ZIP
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entryName);
                    zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
                    
                    // Cria o fornecedor de dados (Input Stream)
                    InputStreamSupplier supplier = () -> {
                        try {
                            return new FileInputStream(file.toFile());
                        } catch (IOException e) {
                            System.err.println("Erro ao ler arquivo: " + file + " - " + e.getMessage());
                            return new java.io.ByteArrayInputStream(new byte[0]); 
                        }
                    };

                    // Adiciona a tarefa na fila de execução paralela
                    scatterZipCreator.addArchiveEntry(() -> ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveEntry, supplier));
                    
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("Comprimindo dados...");
            
            // 4. Grava tudo no arquivo final
            try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(zipFile)) {
                // Configura o nível de compressão (BEST_SPEED é mais rápido, BEST_COMPRESSION economiza mais espaço)
                zipArchiveOutputStream.setLevel(Deflater.BEST_SPEED); 
                scatterZipCreator.writeTo(zipArchiveOutputStream);
            }

            System.out.println("Compactação finalizada com sucesso: " + destinationZip);

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Erro na compactação: " + e.getMessage());
        } finally {
            // Encerra as threads
            executor.shutdown();
        }
    }
}
