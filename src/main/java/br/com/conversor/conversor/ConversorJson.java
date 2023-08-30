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

    public static void start(String sistema, String processo, String namePackage, String nomeArquivoInicio, String nomeArquivoFinal) throws ParseException, IOException {
        JSONObject jsonObject;
        JSONParser parser = new JSONParser();

        String json = leituraArquivoJSON();

        jsonObject = (JSONObject) parser.parse(json);

        tratamentoRegistro(sistema, processo, namePackage, jsonObject, nomeArquivoInicio, "", nomeArquivoFinal);

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

    private static void tratamentoRegistro(String sistema, String processo, String namePackage, JSONObject jsonObject, String nomeArquivoInicio, String nomeArquivoMeio, String nomeArquivoFinal) throws IOException {
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
            if (key.equals("values"))
                System.out.println("Breakpoint");

            Object value = jsonObject.get(key);

            String type = null;

            if (value != null) {
                try {
                    JSONObject jsonItem = (JSONObject) value;
                    nomeArquivoMeio = StringUtils.capitalize(key.toString());
                    String nomeArquivoFilho = nomeArquivoInicio + nomeArquivoMeio + nomeArquivoFinal;
                    br.write("    private " + nomeArquivoFilho + " " + key + ";");
                    br.newLine();
                    variables.put(key.toString(), nomeArquivoFilho);
                    tratamentoRegistro(sistema, processo, namePackage, jsonItem, nomeArquivoInicio, nomeArquivoMeio, nomeArquivoFinal);
                    continue;
                } catch (Exception e) {
                }

                try {
                    int quantidadeArray = ((JSONArray) value).size();
                    if (quantidadeArray > 0) {
                        JSONArray jsonArray = (JSONArray) value;
                        if (jsonArray.get(0).getClass() == String.class) {
                            type = "List<String>";
                            br.write("    private " + type + " " + key + ";");
                            br.newLine();
                            variables.put(key.toString(), type);
                        } else {
                            nomeArquivoMeio = StringUtils.capitalize(key.toString());
                            String nomeArquivoFilho = "List<" + nomeArquivoInicio + nomeArquivoMeio + nomeArquivoFinal + ">";
                            br.write("    private " + nomeArquivoFilho + " " + key + ";");
                            br.newLine();
                            variables.put(key.toString(), nomeArquivoFilho);
                            tratamentoRegistro(sistema, processo, namePackage, (JSONObject) jsonArray.get(0), nomeArquivoInicio, nomeArquivoMeio, nomeArquivoFinal);
                        }
                        continue;
                    }
                } catch (Exception e) {
                }


                if (value.getClass() == String.class)
                    type = "String";
                if (value.getClass() == Double.class || value.getClass() == Long.class)
                    type = "double";
                if (value.getClass() == Boolean.class)
                    type = "boolean";

                if (type != null) {
                    br.write("    private " + type + " " + key + ";");
                    variables.put(key.toString(), type);
                } else {
                    br.write("    // " + key);
                }
                br.newLine();

            } else {
                br.write("    // " + key);
                br.newLine();
            }
        }

        br.newLine();

        OutputStream osInsert = new FileOutputStream("result\\insert_" + nomeArquivo + ".java");
        Writer wrInsert = new OutputStreamWriter(osInsert);
        BufferedWriter brInsert = new BufferedWriter(wrInsert);

        for (String value : variables.keySet()) {

            String key = variables.get(value);

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

            String field = value;
            String insert = "INSERT INTO db_vdc.ipaas_system_field (system_id, process_id, field_id, name) VALUES ";
            insert = insert + "('" + sistema.toUpperCase() + "', '" + processo.toUpperCase() + "', '";
            insert = insert + field.toUpperCase() + "', '" + field.toLowerCase() + "');";
            brInsert.write(insert);
            brInsert.newLine();

        }

        br.write("}");

        br.close();
        brInsert.close();
    }

}



