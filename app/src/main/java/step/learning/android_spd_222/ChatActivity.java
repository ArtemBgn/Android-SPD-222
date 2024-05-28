package step.learning.android_spd_222;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.android_spd_222.orm.ChatMessage;
import step.learning.android_spd_222.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private final byte[] buffer = new byte[8096];
    // паралельні запити до кількох ресурсів не працюють, виконується лише один
    // це обмежує вибір виконавчого сервісу.
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText etNik;
    private EditText etMessage;
    private ScrollView chatScroller;
    private LinearLayout container;
    private MediaPlayer newMessageSound;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final Handler handler = new Handler();
    private boolean wasSoundApplication = true;
    private ImageView onOffSound;
    private Animation newMessageAnimation;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        // Заважає адаптуватися під екранну клавіатуру
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        newMessageAnimation = AnimationUtils.loadAnimation(this, R.anim.arc2);

        updateChat();
/*
        urlToImageView(
                "https://cdn-icons-png.flaticon.com/512/5962/5962463.png",
                findViewById(R.id.chat_iv_logo));
//*/
        etNik = findViewById( R.id.chat_et_nik ) ;
        etMessage = findViewById( R.id.chat_et_message ) ;
        chatScroller = findViewById( R.id.chat_scroller ) ;
        container = findViewById( R.id.chat_container ) ;
        newMessageSound = MediaPlayer.create( this, R.raw.pickup );

        onOffSound = findViewById(R.id.chat_iv_logo);

        onOffSound.setImageResource(R.drawable.sosound);
        onOffSound.setOnClickListener( this::onSoundOnOffTouch );

        findViewById(R.id.chat_btn_send).setOnClickListener( this::onSendClick );
        container.setOnClickListener( (v) -> hideSoftInput() );
    }

    private void hideSoftInput() {
        // клавіатура з'являється автоматично через фокус вводу - відповідно прибрати її - це прибрати фокус
        // Шукаємо елемент, що має фокус введення
        View focusedView = getCurrentFocus();
        if(focusedView != null) {
            // Запитуємо систему щодо засобів управління клавіатурою
            InputMethodManager manager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE );
            // прибираємо клавіатуру з фокусованого елементу
            manager.hideSoftInputFromWindow( focusedView.getWindowToken(), 0 );
            // прибираємо фокус з елементу
            focusedView.clearFocus();
        }
    }
    private void displayChatMessages( boolean wasNewMessage ) {
        if( !wasNewMessage ) return;
        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my);
        Drawable incomingBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_incoming);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams msgParams2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(0, 15, 10, 15);

        runOnUiThread( ()
                -> {
            LinearLayout container = findViewById(R.id.chat_container);
            msgParams.gravity = Gravity.END;
            msgParams2.gravity = Gravity.START;
            for( ChatMessage message : this.chatMessages ) {
                if(message.getView() != null) { // вже показано
                    continue;
                }
                TextView tv = new TextView(this);
                tv.setText(Html.fromHtml( "<b><u><i>" + message.getAuthor() + "</i></u></b>" + ": " + message.getText() + "<br />" + message.getMoment() ));

                if(
                        etNik.getText().toString().equals(message.getAuthor())
                        //( Integer.valueOf(message.getId()) % 2 ) == 0
                ) {
                    tv.setBackground( myBackground );   // заміна для вхідних
                    tv.setGravity(Gravity.END);         // заміна для вхідних
                    tv.setLayoutParams( msgParams );
                    //wasNewMessageSound = true;
                }
                else {
                    tv.setBackground( incomingBackground );
                    tv.setGravity(Gravity.START);
                    tv.setLayoutParams( msgParams2 );
                }
                tv.setPadding(15, 5, 15, 5);
                //tv.setLayoutParams( msgParams );
                //message.setView( tv );
                container.addView( tv );
                message.setView( tv );
            }
            // Асинхронність Андроід призводить до того,
            // що на момент подачі команди, не всі представлення додані до лайнерконтейнера вже сформовані
            // отже прокрутка діятиме лише на поточне наповнення контейнера
            //chatScroller.fullScroll( View.FOCUS_DOWN );
            chatScroller.post(  //передача дії, яка виконується після поточної черги
                    () -> chatScroller.fullScroll( View.FOCUS_DOWN )
            );
            /*
            LinearLayout[] contMessage = new LinearLayout[20];
            for (int i = 0; i < contMessage.length; i++) {
                for( ChatMessage message : this.chatMessages ) {
                    TextView tv = new TextView(this);
                    tv.setText( message.getAuthor() + ": " + message.getText() + "\n" );

                    if(( Integer.valueOf(message.getId()) % 2) == 0) {
                        tv.setBackground( myBackground );   // заміна для вхідних
                        tv.setGravity(Gravity.END);         // заміна для вхідних
                        msgParams.gravity = Gravity.END;
                    }
                    else {
                        tv.setBackground( incomingBackground );
                        tv.setGravity(Gravity.START);
                        msgParams.gravity = Gravity.START;
                    }
                    tv.setPadding(15, 5, 15, 5);
                    tv.setLayoutParams( msgParams );
                    contMessage[i].addView( tv );
                }
                container.addView( contMessage[i] );
            }*/
        } );
    }
    private boolean processChatResponse(String response) {
        boolean wasNewMessage = false;
        boolean wasNewMessageSound = false;
        boolean isFirstProcess = this.chatMessages.isEmpty();
        try {
            ChatResponse chatResponse = ChatResponse.fromJsonString( response );
            for( ChatMessage message : chatResponse.getData() ) {
                if( this.chatMessages.stream().noneMatch(
                        m -> m.getId().equals( message.getId() ) ) ) {
                    // немає жодного повідомлення з таким id, як у message - це нове повідомлення
                    this.chatMessages.add( message );
                    wasNewMessage = true;
                }
                if( !this.chatMessages.stream().noneMatch(
                        m -> m.getAuthor().equals( etNik.getText() ) ) ) {
                    wasNewMessageSound = true;
                }
            }
            if( isFirstProcess ) {
                this.chatMessages.sort( Comparator.comparing( ChatMessage::getMoment ) );
            }
            else if( wasNewMessage && wasNewMessageSound && wasSoundApplication ) {
                newMessageSound.start();
                onOffSound.startAnimation( newMessageAnimation );
                wasNewMessageSound = false;
            }
        }
        catch ( IllegalArgumentException ex ) {
            Log.e("ChatActivity::loadChat()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() );
        }
        return wasNewMessage;
    }
    private void onSendClick( View v ) {
        String author = etNik.getText().toString();
        String message = etMessage.getText().toString();
        if( author.isEmpty() ) {
            Toast.makeText(this, "Заповніть 'Нік'", Toast.LENGTH_SHORT).show();
            return;
        }
        if( message.isEmpty() ) {
            Toast.makeText(this, "Введіть повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor( author );
        chatMessage.setText( message );

        CompletableFuture
                .runAsync( () -> sendChatMessage( chatMessage ), executorService );
    }
    private void sendChatMessage( ChatMessage chatMessage ) {
        /*
        Необхідно сформувати POST-запит на URL чату та передати дані форми з полями
        аuthor та msg з відповідними значеннями з chatMessage/
        - заголовок Content-Type: application/x-www-form-urlencoding
        - тіло у вигляді: author=TheAuthor&msg=The%20Messsage

        Д.з.:
        - Після першого надсилання заблокувати зміну поля "Ник"
         */
        try {
            // 1. Готуємо підключення та налаштовуємо його
            URL url = new URL( CHAT_URL );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 ); // Не ділити на чанки (фрагмемти)
            connection.setDoOutput( true ); // запис у підключення - передача тіла
            connection.setDoInput( true ); // читання - одержання тіла відповіді від сервера
            connection.setRequestMethod( "POST" );
            // Заголовки у connection задаються через setRequestProperty
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            // 2. Запис тіла (DoOutput)
            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format(
                    "author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name() ),
                    URLEncoder.encode(chatMessage.getText(), StandardCharsets.UTF_8.name() )
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ) );

            // 3. Надсилаємо - "виштовхуємо" буфур
            connectionOutput.flush();
            // 3.1 Звільняємо ресурс
            connectionOutput.close();

            // 4. Одержуємо відповідь
            int statusCode = connection.getResponseCode();
            // у разі успіху сервер передає статус 201 і не має тіло
            // якщо помилка, то статус інший т є тіло з описом помилки
            if(statusCode == 201) {
                // якщо потрібно тіло відповіді, то воно у потоці .getInputStream()
                // запускаємо оновлення чату
                updateChat();
            }
            else {
                // при помилцці тіло таке ж, але воно вилучається через .getErrorStream()
                InputStream connectionInput = connection.getErrorStream();
                body = readString( connectionInput );
                connectionInput.close();
                Log.e( "sendChatMessage", body );
            }

            // 5. Закриваємо підключення
            connection.disconnect();
            etMessage.setText( "" );
            etNik.setFocusable(false);
            etNik.setLongClickable(false);
        }
        catch (Exception ex) {
            Log.e("sendChatMessage", ex.getMessage() );
        }
    }
    private String loadChat() {
        try ( InputStream chatStream = new URL( CHAT_URL ).openStream() ) {
            return readString( chatStream );
        }
        catch ( Exception ex ) {
            Log.e("ChatActivity::loadChat()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() );
        }
        return null;
    }
    private void updateChat() {
        if( executorService.isShutdown() ) return;
        CompletableFuture
                .supplyAsync( this::loadChat, executorService )
                .thenApplyAsync( this::processChatResponse )
                .thenAcceptAsync( this::displayChatMessages );
        handler.postDelayed( this::updateChat, 1000);
    }
    private String readString( InputStream stream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while ( (len = stream.read( buffer )) != -1 ) {
            byteBuilder.write( buffer, 0, len);
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }
    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }

    private void onSoundOnOffTouch( View v ) {
        if (wasSoundApplication) {
            onOffSound.setImageResource(R.drawable.nososound);
            wasSoundApplication = false;
        }
        else {
            onOffSound.setImageResource(R.drawable.sosound);
            wasSoundApplication = true;
        }
        onOffSound.startAnimation( newMessageAnimation );
    }
    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture
        .supplyAsync( () -> {
                try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
                    return BitmapFactory.decodeStream( is );
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, executorService )
        .thenAccept( imageView::setImageBitmap );
    }

}
/*
...
Читання даних з потоку має особливості:
    - мульти-байтове кодування (різні символи мають різну байтову довжину). Це
        формує вимогу одержати всі дані в бінарному вигляді, потім декодувати
        як рядок (замість одержання фрагментів даних і їх перетворення)
    - запити до мережі не можуть виконуватись з основного потоку.
        Це спричиняє виняток - (android.os.NetworkOnMainThreadException)
        Варіанти рішень
        = запустити в окремому потоці
            + простіше та наочніше
            - складність завершення різних потоків, особливо якщо їх багато.
        = запустити у фоновому виконавці
            + централізоване завершення
            - не забути завершеннz
    - для того щоб застосунок міг звертатися до мережі йому потрібні дозволи.
        без них виняток ( Permission denied (missing INTERNET permission?) )
        Дозволи зазначаються у маніфесті
        <uses-permission android:name="android.permission.INTERNET"/>
    - Необхідність запуску мережевих запитів в окремих потоках часто призводить
        до того, що з них обмежено доступ до елементів UI
        ( Only the original thread that created a view hierarchy can touch its views.
        Expected: main Calling: pool-3-thread-1 )
        Перехід до UI потоку здійснюється або викликом runOnUiTher
        або преходом до синхронного режиму


 */