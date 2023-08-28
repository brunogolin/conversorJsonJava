package br.com.conversor.conversor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ConversorJson {

    public static void start(String namePackage, String nomeArquivoInicio, String nomeArquivoFinal) throws ParseException, IOException {
        JSONObject jsonObject;
        JSONParser parser = new JSONParser();

        String json = leituraArquivoJSON();

        jsonObject = (JSONObject) parser.parse(json);

        tratamentoRegistro(namePackage, jsonObject, nomeArquivoInicio, "", nomeArquivoFinal);

        System.out.println("FIM - " + nomeArquivoInicio + nomeArquivoFinal);
    }

    private static String leituraArquivoJSON() throws IOException {
        String json = "";
        BufferedReader buffRead = new BufferedReader(new FileReader("original\\json.txt"));
        String linha = "";
        while (true) {
            if (linha != null) {
                json = json + linha;
            } else
                break;
            linha = buffRead.readLine();
        }
        buffRead.close();

        json = json.replaceAll(" ","");

        return json;
    }

    private static void tratamentoRegistro(String namePackage, JSONObject jsonObject, String nomeArquivoInicio, String nomeArquivoMeio, String nomeArquivoFinal) throws IOException {
        String nomeArquivo = nomeArquivoInicio + nomeArquivoMeio + nomeArquivoFinal;
        OutputStream os = new FileOutputStream("result\\" + nomeArquivo + ".java");
        Writer wr = new OutputStreamWriter(os);
        BufferedWriter br = new BufferedWriter(wr);

        HashMap<String, String> variables = new LinkedHashMap<>();

        br.write("package " + namePackage + ";");
        br.newLine();
        br.newLine();
        br.write("public class " + nomeArquivo + " {");
        br.newLine();
        br.newLine();

        for (Object key: jsonObject.keySet()) {
            Object value = jsonObject.get(key);

            String type = "";

            if (value != null) {
                try {
                    JSONObject jsonItem = (JSONObject) value;
                    nomeArquivoMeio = StringUtils.capitalize(key.toString());
                    String nomeArquivoFilho = nomeArquivoInicio + nomeArquivoMeio + nomeArquivoFinal;
                    br.write("    private " + nomeArquivoFilho + " " + key + ";");
                    br.newLine();
                    variables.put(nomeArquivoFilho, key.toString());
                    tratamentoRegistro(namePackage, jsonItem, nomeArquivoInicio, nomeArquivoMeio, nomeArquivoFinal);
                    continue;
                } catch (Exception e) {
                }

                try {
                    int quantidadeArray = ((JSONArray) value).size();
                    if (quantidadeArray > 0) {
                        JSONArray josnArray = (JSONArray) value;
                        nomeArquivoMeio = StringUtils.capitalize(key.toString());
                        String nomeArquivoFilho = "List<" + nomeArquivoInicio + nomeArquivoMeio + nomeArquivoFinal + ">";
                        br.write("    private " + nomeArquivoFilho + " " + key + ";");
                        br.newLine();
                        variables.put(nomeArquivoFilho, key.toString());
                        tratamentoRegistro(namePackage, (JSONObject) josnArray.get(0), nomeArquivoInicio, nomeArquivoMeio, nomeArquivoFinal);
                        continue;
                    }
                } catch (Exception e) {
                }


                try {
                    Double.parseDouble(value.toString());
                    if (value.toString().contains("\"")) {
                        type = "String";
                    } else {
                        type = "double";
                    }
                } catch (NumberFormatException ex) {
                    type = "String";
                }

                br.write("    private " + type + " " + key + ";");
                br.newLine();
                variables.put(type, key.toString());
            } else {
                br.write("    // " + key);
                br.newLine();
            }
        }

        br.newLine();

        for (String key : variables.keySet()) {

            String value = variables.get(key);

            br.write("    public " + key + " get" + StringUtils.capitalize(value) + "() {");
            br.newLine();
            br.write("        return " + value + ";");
            br.newLine();
            br.write("    }");
            br.newLine();
            br.newLine();

            br.write("    public void set" + StringUtils.capitalize(value) + "(" + key + " " + value + ") {");
            br.newLine();
            br.write("        this." + value + " = " + value + ";");
            br.newLine();
            br.write("    }");
            br.newLine();
            br.newLine();
        }

        br.write("}");

        br.close();
    }

}



