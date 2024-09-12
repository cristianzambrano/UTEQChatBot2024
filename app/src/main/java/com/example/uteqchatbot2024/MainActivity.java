package com.example.uteqchatbot2024;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.uteqchatbot2024.Adapter.MensajesAdaptador;
import com.example.uteqchatbot2024.Helper.GlobalInfo;
import com.example.uteqchatbot2024.Helper.Utf8StringRequest;
import com.example.uteqchatbot2024.Models.Mensaje;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private boolean isPlaying = false, isSonido = true;
    private LottieAnimationView lottiMicrofono, lottieCirculo;

    public RecyclerView recyclerView;

    String TokenOpenAI;

    ArrayList<Mensaje> myList = new ArrayList<>();
    MensajesAdaptador adapatorMensajes;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    //private ImageView micButton;
    private TextView txtEstado;
    TextToSpeech textToSpeech;

    private RequestQueue requestQueue;
    private Handler handler = new Handler();
    private Handler hVerificadorSpeaking = new Handler();

    public String ThreadID;
    public String lastMessageID;
    public String run_ID;

    Runnable rVerificadorSpeaking;

    FloatingActionButton fabLista;
    FloatingActionButton fabPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TokenOpenAI = GlobalInfo.getOpenAIApiKey(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
        txtEstado = findViewById(R.id.txtEstado);
        recyclerView = (RecyclerView) findViewById(R.id.rcLista);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int resId = R.anim.layout_animation_down_to_up;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(),
                resId);
        recyclerView.setLayoutAnimation(animation);

        adapatorMensajes = new MensajesAdaptador(getApplicationContext(), myList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapatorMensajes);

        fabLista = findViewById(R.id.fabLista);
        fabPlay = findViewById(R.id.fabPlay);
        fabPlay.setEnabled(false);
        lottiMicrofono = findViewById(R.id.lineasabajo);
        lottiMicrofono.setFrame(0);
        lottiMicrofono.pauseAnimation();

        lottieCirculo = findViewById(R.id.sonido);
        lottieCirculo.setAnimation(R.raw.circulo_ia);
        lottieCirculo.setFrame(0);
        lottieCirculo.pauseAnimation();

        lottieCirculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.stop();
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {         }
            @Override
            public void onBeginningOfSpeech() {
                lottiMicrofono.playAnimation();
                lottieCirculo.setFrame(0);
                lottieCirculo.pauseAnimation();
                lottieCirculo.setAnimation(R.raw.circulo_ia);
                fabPlay.setImageResource(R.drawable.baseline_stop_circle_24);
               }

            @Override
            public void onRmsChanged(float v) {        }
            @Override
            public void onBufferReceived(byte[] bytes) {            }

            @Override
            public void onEndOfSpeech() {
                lottiMicrofono.setFrame(0);
                lottiMicrofono.pauseAnimation();
                isPlaying=false;
                fabPlay.setImageResource(android.R.drawable.ic_media_play);
            }

            @Override
            public void onError(int i) {          }

            @Override
            public void onResults(Bundle bundle) {
                fabPlay.setImageResource(android.R.drawable.ic_media_play);

                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String texto = data.get(0);
                if (!texto.equals("")) {
                    myList.add(new Mensaje(texto, "U"));
                    adapatorMensajes.notifyData(myList);
                    recyclerView.scrollToPosition(myList.size() - 1);
                    doQuestion(texto);

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {           }

            @Override
            public void onEvent(int i, Bundle bundle) {            }
        });

        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    speechRecognizer.stopListening();
                } else {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                isPlaying = !isPlaying;
            }
        });

        recyclerView.setVisibility(View.GONE);
        lottieCirculo.setVisibility(View.VISIBLE);
        fabLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSonido) {
                    recyclerView.setVisibility(View.VISIBLE);
                    lottieCirculo.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    lottieCirculo.setVisibility(View.VISIBLE);

                }
                isSonido = !isSonido;
            }
        });


        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locSpanish = new Locale("spa", "MEX");
                textToSpeech.setLanguage(locSpanish);
            }
        });

        requestQueue = Volley.newRequestQueue(this);
        createThread();

        rVerificadorSpeaking = new Runnable() {
            public void run() {
                if (textToSpeech.isSpeaking()) {
                    if(!lottieCirculo.isAnimating()) lottieCirculo.playAnimation();
                    hVerificadorSpeaking.postDelayed(this, 100);
                }else{
                    lottieCirculo.setAnimation(R.raw.circulo_ia);
                    lottieCirculo.setFrame(0);
                    lottieCirculo.pauseAnimation();
                    hVerificadorSpeaking.removeCallbacks(rVerificadorSpeaking);
                    fabPlay.setEnabled(true);
                }
            }
        };
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }


    private void createThread() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                GlobalInfo.URL_CreatThread,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResp = new JSONObject(response);
                            ThreadID = jsonResp.getString("id").toString();

                            txtEstado.setText("Chat ID: " + ThreadID);
                            String msg="Bienvenido al ChatBot de la UTEQ, " +
                                    "presiona el micrófono para enviar un mensaje";
                            myList.add(new Mensaje(msg,"A"));
                            adapatorMensajes.notifyData(myList);
                            recyclerView.scrollToPosition(myList.size()-1);

                            lottieCirculo.setAnimation(R.raw.lineasabajo);
                            Bundle params = new Bundle();
                            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "3300");
                            textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, params, "3300");
                            hVerificadorSpeaking.postDelayed(rVerificadorSpeaking, 100);

                        } catch (JSONException e) {
                            txtEstado.setText("Error Creating Thread: " + e.toString());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtEstado.setText("Error CreatingThread " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return GlobalInfo.getAuthHearders(TokenOpenAI);
            }
        };
        requestQueue.add(stringRequest);
    }


    private void createMessage(String msg) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                GlobalInfo.getUrlCreateMessage(ThreadID),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResp = new JSONObject(response);
                            lastMessageID = jsonResp.getString("id").toString();
                            txtEstado.setText("Mensaje ID: " + lastMessageID);
                            runMessage();
                        } catch (JSONException e) {
                            txtEstado.setText("Error CreatingMessage: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtEstado.setText("Error CreatingMessage: " + error.getMessage());
                fabPlay.setEnabled(true);
                lottieCirculo.setAnimation(R.raw.circulo_ia);
                lottieCirculo.setFrame(0);
                lottieCirculo.pauseAnimation();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return GlobalInfo.getAuthHearders(TokenOpenAI);
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String requestBody = "{" +
                        "      \"role\": \"user\", " +
                        "      \"content\": \"" + msg + "\"}";
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        requestQueue.add(stringRequest);
    }


    private void runMessage() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                GlobalInfo.getURLRunMessage(ThreadID),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResp = new JSONObject(response);
                            run_ID = jsonResp.getString("id").toString();
                            txtEstado.setText("Ejecución ID: " + run_ID);
                            checkRunStatusPeriodically();
                        } catch (JSONException e) {
                            txtEstado.setText("Error onRUN " + e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtEstado.setText("Error onRUN : " + error.toString());
                fabPlay.setEnabled(true);
                lottieCirculo.setAnimation(R.raw.circulo_ia);
                lottieCirculo.setFrame(0);
                lottieCirculo.pauseAnimation();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return GlobalInfo.getAuthHearders(TokenOpenAI);
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String requestBody = "{ \"assistant_id\": \"" + GlobalInfo.Assistant_ID + "\"," +
                        "      \"instructions\": \"" + GlobalInfo.Instructions + "\"}";
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        requestQueue.add(stringRequest);
    }

    private void checkRunStatusPeriodically() {

        Runnable statusChecker = new Runnable() {
            @Override
            public void run() {
                StringRequest checkStatusRequest = new StringRequest(Request.Method.GET,
                        GlobalInfo.getURLCheckStatus(ThreadID, run_ID),
                        response -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String status = jsonResponse.getString("status");
                                if ("completed".equals(status)) {
                                    getFinalMessage();
                                    txtEstado.setText("Respuesta Lista!");
                                } else {
                                    txtEstado.setText("Estado: " + status);
                                    handler.postDelayed(this, 1000);

                                }
                            } catch (Exception e) {
                                txtEstado.setText("Error GettingStatus: " + e.toString());
                            }
                        },
                        error -> {
                            txtEstado.setText("Error GettingStatus: " + error.toString());
                            fabPlay.setEnabled(true);
                            lottieCirculo.setAnimation(R.raw.circulo_ia);
                            lottieCirculo.setFrame(0);
                            lottieCirculo.pauseAnimation();
                        })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return GlobalInfo.getAuthHearders(TokenOpenAI);
                    }
                };

                requestQueue.add(checkStatusRequest);
            }
        };

        handler.post(statusChecker);
    }

    private void getFinalMessage() {
        StringRequest getMessageRequest = new Utf8StringRequest(Request.Method.GET,
                GlobalInfo.getURLGetMessage(ThreadID, lastMessageID),
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray listaMsg = jsonResponse.getJSONArray("data");
                        JSONObject jsonMsg = listaMsg.getJSONObject(0);
                        JSONArray contentMsgs = jsonMsg.getJSONArray("content");
                        JSONObject contentMsg = contentMsgs.getJSONObject(0);
                        JSONObject contentTextMsg = contentMsg.getJSONObject("text");
                        String resp = contentTextMsg.getString("value").toString();

                        resp = resp.replaceAll("\\【.*?\\】", "");
                        resp = resp.replaceAll("\\*\\*", "");
                        txtEstado.setText("Chat ID: " + ThreadID);
                        myList.add(new Mensaje(resp,"A"));
                        adapatorMensajes.notifyData(myList);
                        recyclerView.scrollToPosition(myList.size()-1);

                        lottieCirculo.setAnimation(R.raw.lineasabajo);
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "3300");
                        textToSpeech.speak(resp, TextToSpeech.QUEUE_FLUSH, params, "3300");
                        hVerificadorSpeaking.postDelayed(rVerificadorSpeaking, 100);

                    } catch (JSONException e) {
                        txtEstado.setText("Error LastMessage: " + e.toString());

                    }
                },
                error -> {
                    txtEstado.setText("Error LastMessage: " + error.toString());
                    fabPlay.setEnabled(true);
                    lottieCirculo.setAnimation(R.raw.circulo_ia);
                    lottieCirculo.setFrame(0);
                    lottieCirculo.pauseAnimation();
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return GlobalInfo.getAuthHearders(TokenOpenAI);
            }
        };

        requestQueue.add(getMessageRequest);
    }



    public void doQuestion(String Pregunta) {
        fabPlay.setEnabled(false);
        lottieCirculo.setAnimation(R.raw.circulo_ia);
        lottieCirculo.playAnimation();
        createMessage(Pregunta);
    }
}