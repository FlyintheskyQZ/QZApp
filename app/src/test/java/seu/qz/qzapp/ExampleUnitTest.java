package seu.qz.qzapp;

import android.app.Application;
import android.os.Environment;

import com.google.gson.Gson;

import org.junit.Test;

import seu.qz.qzapp.utils.PropertyUtil;
import seu.qz.qzapp.utils.StringFormatUtil;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRegularExpression(){

        String date = "{\"istrue\":true}";
        Gson gson = new Gson();
        IsTrue isTrue = gson.fromJson(date, IsTrue.class);
        System.out.println(date);
        System.out.println(isTrue.isIstrue());

    }

    @Test
    public void testFile(){



    }


}