package org.example.limpador_projetos;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ProjectCleaner {

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
    private static long espacoLiberado = 0;
    private static int pastasRemovidas = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== LIMPADOR DE PROJETOS ===");
        System.out.print("Digite o caminho da pasta raiz para escanear (ex: C:/MeusProjetos): ");
        String inputPath = scanner.nextLine();

        Path startPath = Paths.get(inputPath);

        if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
            System.out.println("Caminho inválido ou não é um diretório.");
            return;
        }

        System.out.println("Escaneando... (Isso pode demorar dependendo do tamanho do disco)");

        try {
            scanAndClean(startPath);
            System.out.println("\n=== CONCLUÍDO ===");
            System.out.println("Pastas removidas: " + pastasRemovidas);
            System.out.printf("Espaço liberado estimado: %.2f MB\n", espacoLiberado / (1024.0 * 1024.0));
        } catch (IOException e) {
            System.err.println("Erro ao acessar arquivos: " + e.getMessage());
        }
    }

    private static void scanAndClean(Path startPath) throws IOException {
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Verifica se o nome da pasta atual está na nossa lista negra
                String dirName = dir.getFileName().toString();

                if (PASTAS_ALVO.contains(dirName)) {
                    System.out.println("Encontrado: " + dir.toAbsolutePath());
                    
                    // Opcional: Perguntar antes de deletar (Descomente se quiser segurança extra)
                    // System.out.println("Deletar? (s/n)"); ...
                    
                    try {
                        long size = calcularTamanho(dir); // Calcula tamanho antes de apagar para o relatório
                        deletarRecursivamente(dir);
                        espacoLiberado += size;
                        pastasRemovidas++;
                        System.out.println(" [DELETADO] - " + (size / 1024) + " KB liberados.");
                        
                        // IMPORTANTE: Retorna SKIP_SUBTREE para não tentar entrar numa pasta que já não existe
                        return FileVisitResult.SKIP_SUBTREE;
                    } catch (IOException e) {
                        System.err.println(" [ERRO] Não foi possível deletar: " + e.getMessage());
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
    private static void deletarRecursivamente(Path path) throws IOException {
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
    private static long calcularTamanho(Path path) {
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
