package step.learning.android_spd_222;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;
    private double variable_1;
    private double variable_2;
    private double solution;
    private String operation;
    private String percent = "";
    private String signum;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState); // потрібно
        outState.putCharSequence( "tvResult", tvResult.getText() );
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
    }
    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        При зміні конфігурації пристрою (поворотах, змінах налаштувань, тощо) відбувається перезапуск активності
        при цьому подаються події життєвого циклу
        onSaveInstanceState - при виході з активності перед перезапуском
        onRestoreInstanceState - при відновлені активності після перезапуску
        До обробників передається Bundle, що є сховищем, яке дозволяє зберігати та відновлювати дані.
        Також збережений Bundle передається до onCreate, що дозволяє визначити чи це перший запуск чи перезапуск через зміну конфігурації.
        */

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*
        Циклом перебрати ресурсні кнопки calc_btn_{i} та для кожної з них поставити обробник onDigitButtonClick
        */
        tvHistory = findViewById( R.id.calc_tv_history );
        tvResult = findViewById( R.id.calc_tv_result );
        if(savedInstanceState == null) {
            tvResult.setText("0");      // немає збереженого стану -> перший запуск -> проставляємо ноль
            tvHistory.setText("");
        }
        for (int i = 0; i < 10; i++ ) {
            findViewById(                               // на заміну R.id.calc_btn_0 приходить наступний вираз
                    getResources().getIdentifier(       // R
                                    "calc_btn_" + i,    // .calc_btn_0
                                    "id",               //.id
                                    getPackageName()
                            )
            ).setOnClickListener( this::onDigitButtonClick );
        }
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::onInverseClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::onSquareClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::onPercentClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::onCeClick);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::onCClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::onSqrtClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onDivideClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onMultiplyClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::onMinusClick);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::onPlusClick);
        findViewById(R.id.calc_btn_positive_negative).setOnClickListener(this::onPosNegClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::onCommaClick);
        findViewById(R.id.calc_btn_equals).setOnClickListener(this::onEqualsClick);

        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this) {
            @Override
            //public void onSwipeBottom() {}
            //public void onSwipeTop() {}
            //public void onSwipeRight() {}
            public void onSwipeLeft() {
                //Toast.makeText(CalcActivity.this, "Left", Toast.LENGTH_SHORT).show();
                String result = tvResult.getText().toString();
                if (( result.length() >= 1 ) && ( !result.equals("0") ) ) {
                    result = result.substring(0, result.length()-1);
                    if (result.length() == 0) { result = "0"; }
                    tvResult.setText( result );
                }
            }
        });
    }
    private void onInverseClick(View view) {
        String result = tvResult.getText().toString();
        String strHistory = "";//tvHistory.getText().toString();
        double x = Double.parseDouble(result);
        if(x==0) {
            Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
            return;
        }
        strHistory += "1/(" + String.valueOf( x ) + ")";
        x = 1.0 / x;
        String str = ( x == (int) x ) ? String.valueOf( (int) x ) : String.valueOf( x );
        if(str.length()>13) {
            str = str.substring(0, 13);
        }
        tvHistory.setText( strHistory );
        tvResult.setText( str );
    }    // Нужно поработать с историей
    private void onSquareClick(View view) {
        String result = tvResult.getText().toString();
        String strHistory = tvHistory.getText().toString();
        double x = Double.parseDouble(result);
        strHistory = "sqt(" + String.valueOf(x) + ")";
        x *= x;
        String str = ( x == (int) x ) ? String.valueOf( (int) x ) : String.valueOf( x );
        if(str.length()>13) {
            str = str.substring(0, 13);
        }
        tvHistory.setText( strHistory );
        tvResult.setText( str );
    }
    private void onCClick(View view) {
        tvHistory.setText( "" );
        tvResult.setText( "0" );
    }
    private void onCeClick(View view) {
        tvHistory.setText( "" );
        tvResult.setText( "0" );
    }     // пока то же самое что и onCClick, нужно подредактировать
    private void onBackspaceClick(View view) {
        String result = tvResult.getText().toString();
        if (( result.length() >= 1 ) && ( !result.equals("0") ) ) {
            result = result.substring(0, result.length()-1);
            if (result.length() == 0) { result = "0"; }
            tvResult.setText( result );
        }
    }
    private void onSqrtClick(View view) {
        String result = tvResult.getText().toString();
        String strHistory = tvHistory.getText().toString();
        double x = Double.parseDouble(result);
        strHistory = "\u221A" + (( x == (int) x ) ? String.valueOf( (int) x ) : String.valueOf( x ));
        x = Math.sqrt(x);
        String str = ( x == (int) x ) ? String.valueOf( (int) x ) : String.valueOf( x );
        if(str.length()>13) {
            str = str.substring(0, 13);
        }
        tvHistory.setText( strHistory );
        tvResult.setText( str );
    }
    private void onPercentClick(View view) {
        percent = "%";
        this.onEqualsClick(view);
    }
    private void onDivideClick(View view) {
        String result = tvResult.getText().toString();
        variable_1 = Double.parseDouble(result);
        operation = "\u00F7";
        String strHistory = result + operation;
        tvHistory.setText( strHistory );
        tvResult.setText( "0" );
    }
    private void onMultiplyClick(View view) {
        String result = tvResult.getText().toString();
        variable_1 = Double.parseDouble(result);
        operation = "\u00D7";
        String strHistory = result + operation;
        tvHistory.setText( strHistory );
        tvResult.setText( "0" );
    }
    private void onMinusClick(View view) {
        String result = tvResult.getText().toString();
        variable_1 = Double.parseDouble(result);
        operation = "\u2212";
        String strHistory = result + operation;
        tvHistory.setText( strHistory );
        tvResult.setText( "0" );
    }
    private void onPlusClick(View view) {
        String result = tvResult.getText().toString();
        variable_1 = Double.parseDouble(result);
        operation = "\u002B";
        String strHistory = result + operation;
        tvHistory.setText( strHistory );
        tvResult.setText( "0" );
    }
    private void onPosNegClick(View view) {
        String result = tvResult.getText().toString();

        if (result.length() > 9) {
            Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.equals("0")) {
            return;
        }
        if(result.indexOf("-") >= 0) {
            result = result.substring(1);
        }
        else {
            result = "-" + result;
        }
        tvResult.setText( result );
    }
    private void onCommaClick(View view) {
        String result = tvResult.getText().toString();
        if (result.length() > 8) {
            Toast.makeText(this, R.string.calc_limit_exceeded_comma, Toast.LENGTH_SHORT).show();
            return;
        }
        if(result.contains(".")) {
            Toast.makeText(this, R.string.calc_comma, Toast.LENGTH_SHORT).show();
            return;
        }
        result += ".";
        tvResult.setText( result );
    }
    private void onEqualsClick(View view) {
        String result = tvResult.getText().toString();
        variable_2 = Double.parseDouble(result);
        String strHistory = tvHistory.getText().toString();
        strHistory += String.valueOf(variable_2);
        if (percent.equals("%")) {
            variable_2 = variable_2 / variable_1;
            strHistory += "%";
        }
        switch (operation) {
            case "\u00F7":  // деление
                if( variable_2 == 0 ) {
                    Toast.makeText(this, R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
                    return;
                }
                solution = variable_1 / variable_2;
                tvResult.setText( String.valueOf( ( solution == (int) solution ) ? String.valueOf( (int) solution ) : String.valueOf( solution ) ) );
                break;
            case "\u00D7":  // умножение
                solution = variable_1 * variable_2;
                tvResult.setText( String.valueOf( ( solution == (int) solution ) ? String.valueOf( (int) solution ) : String.valueOf( solution ) ) );
                break;
            case "\u2212":  // вичетание
                solution = variable_1 - variable_2;
                tvResult.setText( String.valueOf( ( solution == (int) solution ) ? String.valueOf( (int) solution ) : String.valueOf( solution ) ) );
                break;
            case "\u002B":  // сложение
                solution = variable_1 + variable_2;
                tvResult.setText( String.valueOf( ( solution == (int) solution ) ? String.valueOf( (int) solution ) : String.valueOf( solution ) ) );
                break;
        }
        variable_1 = 0;
        variable_2 = 0;
        solution = 0;
        percent = "";
    }
    private void onDigitButtonClick(View view) {
        String result = tvResult.getText().toString();
        if (result.length() >= 10) {
            Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
            return;
        }
        if(result.equals("0")) {
            result = "";
        }
        result += ((Button)view).getText();
        tvResult.setText( result );
    }
}