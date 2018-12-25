package wang.jason.annotationprocessortool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import wang.jason.lib.PsiTableHeaderAnnotation;

import static wang.jason.lib.PsiTableHeaderAnnotation.HEXADECIMAL;

public class Main2Activity extends AppCompatActivity {
    @PsiTableHeaderAnnotation(name = "table",codeForamt = HEXADECIMAL)
    public String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}
