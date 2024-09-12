package com.example.uteqchatbot2024.Helper;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GlobalInfo {
    public static final String URL_CreatThread ="https://api.openai.com/v1/threads";
    public static final String Assistant_ID = "asst_dKqNiDGt1ltpNWq0hzIxuYbd";

    public static final String Instructions = "Eres un chatbot que responde preguntas sobre la Carrera de Ingeniería de Software y de la Universidad Técnica Estatal de Quevedo (UTEQ). Cuando respondas las preguntas con información de los archivos NO digas que lo sacaste del fichero. Se consiso da la información puntual. No respondas preguntas que no estén relacionadas con información de la Universidad Técnica Estatal de Quevedo , la carrera de Software de la UTEQ, ni con los datos de los alumnos de la asignatura de Programación en la Nube del 10mo semestre de la carrera de Software, manifiesta que eres un asistente virtual de la UTEQ. Al inicio pide que te hagan peguntas puntuales de tu base de conocimientos";

    public static final String getUrlCreateMessage(String ThreadID){
        return "https://api.openai.com/v1/threads/" + ThreadID + "/messages";

    }
    public static final String getURLRunMessage(String ThreadID){
        return "https://api.openai.com/v1/threads/" + ThreadID +"/runs";
    }
    public static final String getURLCheckStatus(String ThreadID, String runId){
        return "https://api.openai.com/v1/threads/" + ThreadID + "/runs/" + runId;
    }

    public static final String getURLGetMessage(String ThreadID, String lastMessageID){
        return "https://api.openai.com/v1/threads/" + ThreadID +"/messages" +
                "?order=asc&after=" + lastMessageID;
    }

    public static Map<String, String> getAuthHearders(String Token){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + Token);
        headers.put("OpenAI-Beta", "assistants=v2");
        return headers;
    }
    public static String getOpenAIApiKey(Context context) {
        Properties properties = new Properties();
        String apiKey = "";
        try {
            InputStream inputStream = context.getAssets().open("secrets.properties");
            properties.load(inputStream);
            apiKey = properties.getProperty("OpenAI_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiKey;
    }

}
