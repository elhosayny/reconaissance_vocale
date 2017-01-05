package ma.estc.recovoice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.android.AIConfiguration;
import ai.api.AIListener;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class Main extends AppCompatActivity implements AIListener{

    private ImageButton mButton;
    private TextView mTextView;
    private AIService mAiService;

    final private int CAMERA = 1;
    final private int GALERIE = 2;
    final private int Contact = 3;
    final private int CALC = 4;
    final private int ALARM = 5;
    final private int CALENDRIER=6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(Main.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(Main.this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(Main.this,Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(Main.this,Manifest.permission.INTERNET))
            {
                Toast.makeText(this,"we need permission to record voice and to use internet",Toast.LENGTH_LONG);

            }

            ActivityCompat.requestPermissions(Main.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
            ActivityCompat.requestPermissions(Main.this,new String[]{Manifest.permission.INTERNET},2);
        }

        final AIConfiguration config = new AIConfiguration("84e5501f7ff249188e73fbb33524bfe3",AIConfiguration.SupportedLanguages.French,AIConfiguration.RecognitionEngine.System);
        mAiService = AIService.getService(this,config);
        mAiService.setListener(this);
        mButton = (ImageButton) findViewById(R.id.recordBtn);
        mTextView = (TextView)findViewById(R.id.resultTextView);
        mButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    mButton.setImageResource(R.drawable.icone_clicked);
                    mAiService.startListening();
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                {
                    mButton.setImageResource(R.drawable.icone);
                    mAiService.stopListening();
                }
                return false;
            }
        });
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        String word = result.getResolvedQuery().toString();
        mTextView.setText(word);
        switch (getInterName(word))
        {
            case CAMERA :
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(camera);
                break;
            case ALARM :
                try {
                    Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
                    alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(alarm);
                }catch (Exception e){
                    Toast.makeText(Main.this,"Probleme de compatibilité",Toast.LENGTH_LONG).show();
                }
                break;
            case Contact :
                Intent contact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivity(contact);
                break;


        }

    }

    @Override
    public void onError(AIError error) {
        mTextView.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
    public boolean inList(String word,String[] list)
    {
        for(String str : list)
        {
            if(str.equals(word)) return true;
        }
        return false;
    }
    public int getInterName(String word)
    {
        String[] camera = {"camera","cameras","caméras","caméra"};
        String[] calcule = {"calcule","calculatrices","calculatrice"};
        String[] calendrier = {"calendrier","agenda"};
        String[] galerie = {"photo","image","video","galerie"};
        String[] alarm = {"alarm","alarmes","alarme","horloge","horloges"};
        String[] contact = {"contact","numéro","numéros","répertoires","répertoire","repertoire","repertoires"};
        if(inList(word,camera)) return CAMERA;
        else if (inList(word,calcule)) return CALC;
        else if (inList(word,calendrier)) return CALENDRIER;
        else if (inList(word,galerie))return GALERIE;
        else if (inList(word,alarm)) return ALARM;
        else if (inList(word,contact)) return Contact;
        return 0;
    }
}
