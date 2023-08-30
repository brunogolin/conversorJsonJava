package br.com.conversor.conversor;

import org.json.simple.parser.ParseException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ConversorApplication {

    public static void main(String[] args) throws ParseException, IOException {

        String sistema = "OMIE";
        String processo = "OMIE_PRODUTO_ALTERADO";
        String nomeArquivoInicio = "OmieFindProduct";
        String nomeArquivoFinal = "ResultV1DTO";

        String namePackage = "br.com.vouDeClick.vouDeIntegracao.external." + sistema.toLowerCase();
        ConversorJson.start(sistema, processo, namePackage, nomeArquivoInicio, nomeArquivoFinal);
    }

}
